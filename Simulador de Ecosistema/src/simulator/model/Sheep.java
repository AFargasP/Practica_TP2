package simulator.model;

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
		dangerStrategy = p1.dangerStrategy;
		dangerSource = null;
	}

	public Sheep(SelectionStrategy mateStrategy, SelectionStrategy dangerStrategy,  Vector2D pos) {
		super(SHEEP_GENETIC_CODE, Diet.HERBIVORE,INIT_SIGHT_SHEEP,INIT_SPEED_SHEEP, mateStrategy, pos);
	}

	

	public void update(double dt) {
		if(state == State.DEAD) return;
		
		switch(state) {
		case NORMAL:
			if(this.pos.distanceTo(dest) < Animal.COLLISION_RANGE) {
				dest.getPosAleatoria(0.0, regionMngr.getWidth()-1, 0.0, regionMngr.getHeight()-1);
			}
			move(speed*dt*Math.exp((energy-100.0)*HUNGER_DECAY_EXP_FACTOR));
			energy -= FOOD_DROP_RATE_SHEEP*dt;
			ajustaAtributos(energy);
			if(dangerSource == null) {
				//buscaAnimalPeligroso
				if(desire > DESIRE_THRESHOLD_SHEEP) state = State.MATE;
			}else {
				state = State.DANGER;
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
			} else {
				move(speed*dt*Math.exp((energy-100.0)*HUNGER_DECAY_EXP_FACTOR));
				//buscaranimalpeligroso;
				if(desire >= DESIRE_THRESHOLD_SHEEP) state = State.MATE;
				else state = State.NORMAL;
				
			}
			
			if(pos.distanceTo(dangerSource.getPosition()) > sightRange) {
				//buscaranimalpeligroso;
			}
			break;
		case MATE:
			if(mateTarget != null && (mateTarget.getState() == State.DEAD || 
				pos.distanceTo(mateTarget.getPosition()) > sightRange)) {
				mateTarget = null;
			}
			
			if(mateTarget == null) {
				if(buscarMateTarget()) {
					dest = mateTarget.getPosition();
					move(BOOST_FACTOR_SHEEP*speed*dt*Math.exp((energy-100.0)*HUNGER_DECAY_EXP_FACTOR));
					energy -= FOOD_DROP_RATE_SHEEP*FOOD_DROP_BOOST_FACTOR_SHEEP*dt;
					ajustaAtributos(energy);
					if(pos.distanceTo(mateTarget.getPosition()) < Animal.COLLISION_RANGE) {
						desire = 0.0;
						mateTarget.desire = 0.0;  // hacer setter
						
					}
				}else {
					move(speed*dt*Math.exp((energy-100.0)*HUNGER_DECAY_EXP_FACTOR));
				}
				
			}
			if(dangerSource == null) {
				//buscarAnimalPeligroso
				if(desire < DESIRE_THRESHOLD_SHEEP) state = State.NORMAL;
			}else {
				state = State.DANGER;
			}
			break;
		}
		age += dt;
		desire += DESIRE_INCREASE_RATE_SHEEP*dt;
		ajustaAtributos(desire);
		
		if(pos.isOut(regionMngr.getWidth()-1, regionMngr.getHeight()-1)) {
			pos.ajustaPosicion(regionMngr.getWidth()-1, regionMngr.getHeight()-1);
			state = State.NORMAL;
		}
		if(age > MAX_AGE_SHEEP || energy <= 0.0) {
			state = State.DEAD;
		}
	}

	@Override
	public Diet getDiet() {
		return this.diet;
	}

	@Override
	protected void setNormalStateAction() {
		
	}

	@Override
	protected void setMateStateAction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setHungerStateAction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setDangerStateAction() {
		
		
	}

	@Override
	protected void setDeadStateAction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public State getState() {
		return this.state;
	}
	

}
