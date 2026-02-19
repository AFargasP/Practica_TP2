package simulator.model;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Wolf extends Animal{
	
	 // se usan en Wolf
	final static String WOLF_GENETIC_CODE = "Wolf";
	final static double INIT_SIGHT_WOLF = 50;
	final static double INIT_SPEED_WOLF = 60;
	final static double BOOST_FACTOR_WOLF = 3.0;
	final static double MAX_AGE_WOLF = 14.0;
	final static double FOOD_THRSHOLD_WOLF = 50.0;
	final static double FOOD_DROP_BOOST_FACTOR_WOLF = 1.2;
	final static double FOOD_DROP_RATE_WOLF = 18.0;
	final static double FOOD_DROP_DESIRE_WOLF = 10.0;
	final static double FOOD_EAT_VALUE_WOLF = 50.0;
	final static double DESIRE_THRESHOLD_WOLF = 65.0;
	final static double DESIRE_INCREASE_RATE_WOLF = 30.0;
	final static double PREGNANT_PROBABILITY_WOLF = 0.75;
	
	private SelectionStrategy huntingStrategy;
	private Animal huntTarget;
	
	public Wolf(SelectionStrategy mateStrategy, SelectionStrategy huntingStrategy,  Vector2D pos) {
		super(WOLF_GENETIC_CODE, Diet.CARNIVORE, INIT_SIGHT_WOLF, INIT_SPEED_WOLF, mateStrategy, pos);
		this.huntingStrategy = huntingStrategy;
	}
	
	protected Wolf(Wolf p1, Animal p2) {
		super(p1, p2);
		this.huntingStrategy = p1.huntingStrategy;
		this.huntTarget = null;
	}
	
	
	private void selectHuntTarget() {
		huntTarget = huntingStrategy.select(this, regionMngr.getAnimalsInRange(this, animal -> animal.getGeneticCode() != this.getGeneticCode()));
	}
	
	public void update(double dt) {
		if(state == State.DEAD) return;
		
		switch(state) {
		case NORMAL: 
			if(this.pos.distanceTo(dest) < COLLISION_RANGE) {
				dest = Vector2D.getPosAleatoria(0.0, regionMngr.getWidth()-1, 0.0, regionMngr.getHeight()-1);
			}
			
			move(speed*dt*Math.exp((energy-100.0)*HUNGER_DECAY_EXP_FACTOR));
			this.energy -= FOOD_DROP_RATE_WOLF*dt;
			Utils.constrainValueInRange(energy, 0.0, 100.0);
			this.desire += DESIRE_INCREASE_RATE_WOLF*dt;
			Utils.constrainValueInRange(desire, 0.0, 100.0);
			
			if(energy < FOOD_THRSHOLD_WOLF) {
				setHungerStateAction();
			} else {
				if(desire > DESIRE_THRESHOLD_WOLF) {
					setMateStateAction();
				}
			}
			break;
			
		case HUNGER: 
			if((huntTarget.getState() == State.DEAD || pos.distanceTo(huntTarget.getPosition()) > sightRange)
					|| huntTarget == null) {
				selectHuntTarget();
			}
			
			if(huntTarget == null) move(speed*dt*Math.exp((energy-100.0)*HUNGER_DECAY_EXP_FACTOR));
			else {
				dest = huntTarget.getPosition();
				move(BOOST_FACTOR_WOLF*speed*dt*Math.exp((energy-100.0)*HUNGER_DECAY_EXP_FACTOR));
				this.energy -= FOOD_DROP_RATE_WOLF*dt*FOOD_DROP_BOOST_FACTOR_WOLF;
				Utils.constrainValueInRange(energy, 0.0, 100.0);
				this.desire += DESIRE_INCREASE_RATE_WOLF*dt;
				Utils.constrainValueInRange(desire, 0.0, 100.0);
				
				if(this.pos.distanceTo(huntTarget.getPosition()) < COLLISION_RANGE) {
					huntTarget.setDeadStateAction();
					huntTarget = null;
					energy += FOOD_EAT_VALUE_WOLF;
					Utils.constrainValueInRange(energy, 0.0, 100.0);;
				}		
			}
			if(energy > 50) {
				if(desire < DESIRE_THRESHOLD_WOLF) {
					setNormalStateAction();
				}else setMateStateAction();
			}
			break;
			
		case MATE:
			if(mateTarget != null && (mateTarget.getState() == State.DEAD || pos.distanceTo(mateTarget.getPosition()) > sightRange)) {
				mateTarget = null;
			}
			
			if(mateTarget == null) {
				selectMateTarget();
				if(mateTarget == null) {
					move(speed*dt*Math.exp((energy-100.0)*HUNGER_DECAY_EXP_FACTOR));
				}
			}else {
				dest = mateTarget.getPosition();
				move(BOOST_FACTOR_WOLF*speed*dt*Math.exp((energy-100.0)*HUNGER_DECAY_EXP_FACTOR));
				this.energy -= FOOD_DROP_RATE_WOLF*dt*FOOD_DROP_BOOST_FACTOR_WOLF;
				Utils.constrainValueInRange(energy, 0.0, 100.0);
				this.desire += DESIRE_INCREASE_RATE_WOLF*dt;
				Utils.constrainValueInRange(desire, 0.0, 100.0);
				
				if(this.pos.distanceTo(mateTarget.getPosition()) < COLLISION_RANGE) {
					mateTarget.setDesire(0.0);
					this.desire = 0.0;
					if(!this.isPregnant()) {
						double x = Utils.RAND.nextDouble(0, 1);
						if(x < PREGNANT_PROBABILITY_WOLF) {
							this.baby = new Wolf(this, mateTarget);
						}
					}			
					this.energy -= 10;
					Utils.constrainValueInRange(energy, 0.0, 100.0);;
					mateTarget = null;
				}
			}
			
			if(energy < FOOD_THRSHOLD_WOLF) {
				setHungerStateAction();
			}else {
				if(desire <  DESIRE_THRESHOLD_WOLF) {
					setNormalStateAction();
				}
			}
			break;
		}
		this.age += dt;
		
		if(pos.isOut(regionMngr.getWidth()-1, regionMngr.getHeight()-1)) { //Ajusta pos y pone estado a normal
			pos.ajustaPosicion(regionMngr.getWidth()-1, regionMngr.getHeight()-1);
			setNormalStateAction();
		}
		
		if(energy <= 0.0 || age > MAX_AGE_WOLF) {
			setDeadStateAction();
		}
		
		if(state != State.DEAD) { //Pide comida y la suma a la energia
			energy += regionMngr.getFood(this, dt); 
			Utils.constrainValueInRange(energy, 0.0, 100.0);
		}
	}

	@Override
	public State getState() {
		return this.state;
	}

	@Override
	public Diet getDiet() {
		return this.diet;
	}

	@Override
	protected void setNormalStateAction() {
		mateTarget = null;
		huntTarget = null;
	}

	@Override
	protected void setMateStateAction() {
		huntTarget = null;
	}

	@Override
	protected void setHungerStateAction() {
		mateTarget  = null;
	}

	@Override
	protected void setDangerStateAction() {}

	@Override
	protected void setDeadStateAction() {
		mateTarget = null;
		huntTarget = null;
	}

}
