package simulator.model;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Sheep extends Animal{
	
	// se usan en Sheep
	final static String SHEEP_GENETIC_CODE = "Sheep";
	final static double INIT_SIGHT_SHEEP = 40;
	final static double INIT_SPEED_SHEEP = 35;
	final static double BOOST_FACTOR_SHEEP = 2.0;
	final static double MAX_AGE_SHEEP = 8;
	final static double FOOD_DROP_BOOST_FACTOR_SHEEP = 1.2;
	final static double FOOD_DROP_RATE_SHEEP = 20.0;
	final static double DESIRE_THRESHOLD_SHEEP = 65.0;
	final static double DESIRE_INCREASE_RATE_SHEEP = 40.0;
	final static double PREGNANT_PROBABILITY_SHEEP = 0.9;
	
	private SelectionStrategy dangerStrategy;
	private Animal dangerSource;
	
	
	protected Sheep(Sheep p1, Animal p2) {
		super(p1, p2);
		this.dangerStrategy = p1.dangerStrategy;
		this.dangerSource = null;
	}

	public Sheep(SelectionStrategy mateStrategy, SelectionStrategy dangerStrategy,  Vector2D pos) {
		super(SHEEP_GENETIC_CODE, Diet.HERBIVORE, INIT_SIGHT_SHEEP, INIT_SPEED_SHEEP, mateStrategy, pos);
		this.dangerStrategy = dangerStrategy;
		
	}

	

	public void update(double dt) {
		if(state == State.DEAD) return;
		
		switch(state) {
		case NORMAL:
			if(this.pos.distanceTo(dest) < Animal.COLLISION_RANGE) {
				dest = Vector2D.getPosAleatoria(0.0, regionMngr.getWidth()-1, 0.0, regionMngr.getHeight()-1);
			}
			
			move(speed*dt*Math.exp((energy-100.0)*HUNGER_DECAY_EXP_FACTOR));
			energy -= FOOD_DROP_RATE_SHEEP*dt;
			ajustaAtributos(energy);
			desire += DESIRE_INCREASE_RATE_SHEEP*dt;
			ajustaAtributos(desire);
			
			if(dangerSource == null) {
				selectHuntOrDangerTarget();
			}
			
			if(dangerSource != null) {
				setDangerStateAction();
			} else {
				if(desire > DESIRE_THRESHOLD_SHEEP) setMateStateAction();
			}
			break;
			
		case DANGER: 
			if(dangerSource.getState() == State.DEAD && dangerSource != null) {
				dangerSource = null;
			}
			
			if(dangerSource != null) {
				pos = pos.plus(pos.minus(dangerSource.getPosition().direction()));
				move(BOOST_FACTOR_SHEEP*speed*dt*Math.exp((energy-100.0)*HUNGER_DECAY_EXP_FACTOR));
				energy -= FOOD_DROP_RATE_SHEEP*FOOD_DROP_BOOST_FACTOR_SHEEP*dt;
				ajustaAtributos(energy);
				desire += DESIRE_INCREASE_RATE_SHEEP*dt;
				ajustaAtributos(desire);
			} else {
				move(speed*dt*Math.exp((energy-100.0)*HUNGER_DECAY_EXP_FACTOR));
			}
			
			if(dangerSource == null || pos.distanceTo(dangerSource.getPosition()) > sightRange) {
				selectHuntOrDangerTarget();
			}
			
			if(dangerSource == null) {
				if(desire < DESIRE_THRESHOLD_SHEEP) {
					setNormalStateAction();
				}else {
					setMateStateAction();
				}
			}
			
			break;
			
		case MATE:
			if(mateTarget != null && (mateTarget.getState() == State.DEAD || 
				pos.distanceTo(mateTarget.getPosition()) > sightRange)) {
				mateTarget = null;
			}
			
			if(mateTarget == null) {
				selectMateTarget();
				if(mateTarget == null) {
					move(speed*dt*Math.exp((energy-100.0)*HUNGER_DECAY_EXP_FACTOR));
				}
			} else {
				dest = mateTarget.getPosition();
				move(BOOST_FACTOR_SHEEP*speed*dt*Math.exp((energy-100.0)*HUNGER_DECAY_EXP_FACTOR));
				energy -= FOOD_DROP_RATE_SHEEP*FOOD_DROP_BOOST_FACTOR_SHEEP*dt;
				ajustaAtributos(energy);
				desire += DESIRE_INCREASE_RATE_SHEEP*dt;
				ajustaAtributos(desire);
				if(pos.distanceTo(mateTarget.getPosition()) < COLLISION_RANGE) {
					desire = 0.0;
					mateTarget.desire = 0.0;  // hacer setter
					if(!this.isPregnant()) {
						double x = Utils.RAND.nextDouble(0, 1);
						if(x < PREGNANT_PROBABILITY_SHEEP) {
							this.baby = new Sheep(this, mateTarget);
						}
					}
					mateTarget = null;
				}
			}
			
			if(dangerSource == null) {
				selectHuntOrDangerTarget();
				if(dangerSource == null) {
					setDangerStateAction();
				} else {
					if(desire < DESIRE_THRESHOLD_SHEEP) {
						setNormalStateAction();
					}
				}
			} 
			
			
			break;
		}
		age += dt;
		
		
		if(pos.isOut(regionMngr.getWidth()-1, regionMngr.getHeight()-1)) { //Ajusta pos y pone estado a normal
			pos.ajustaPosicion(regionMngr.getWidth()-1, regionMngr.getHeight()-1);
			setNormalStateAction();
		}
		if(age > MAX_AGE_SHEEP || energy <= 0.0) { //La oveja se muere
			setDeadStateAction();
		}
		
		if(state != State.DEAD) { //Pide comida y la suma a la energia
			energy += regionMngr.getFood(this, dt); 
			ajustaAtributos(energy);
		}
	}
	

	@Override
	public Diet getDiet() {
		return this.diet;
	}

	@Override
	protected void setNormalStateAction() {
		mateTarget = null;
		dangerSource = null;
	}

	@Override
	protected void setMateStateAction() {
		dangerSource = null;
	}

	@Override
	protected void setHungerStateAction() {}

	@Override
	protected void setDangerStateAction() {
		mateTarget = null;
	}

	@Override
	protected void setDeadStateAction() {
		mateTarget = null;
		dangerSource = null;
	}

	@Override
	public State getState() {
		return this.state;
	}
	

}
