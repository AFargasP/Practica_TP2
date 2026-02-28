package simulator.model;


import org.json.JSONObject;
import simulator.misc.Utils;
import simulator.misc.Vector2D;

public abstract class Animal implements AnimalInfo, Entity {
	
	// se usan en Animal y subclases
	final static double INIT_ENERGY = 100.0;
	final static double MUTATION_TOLERANCE = 0.2;
	final static double NEARBY_FACTOR = 60.0;
	final static double COLLISION_RANGE = 8;
	final static double HUNGER_DECAY_EXP_FACTOR = 0.007;
	final static double MAX_ENERGY = 100.0;
	final static double MAX_DESIRE = 100.0;
	
	
	//ATRIBUTOS
	protected String geneticCode;
	protected Diet diet;
	protected State state;
	protected Vector2D pos;
	protected Vector2D dest;
	protected double energy;
	protected double speed;
	protected double age;
	protected double desire;
	protected double sightRange;
	protected Animal mateTarget;
	protected Animal baby;
	protected AnimalMapView regionMngr;
	private SelectionStrategy mateStrategy;
	
	
	protected Animal(String geneticCode, Diet diet, double sightRange,
			double initSpeed, SelectionStrategy mateStrategy, Vector2D pos) {
		
		if(geneticCode.isEmpty() || sightRange < 0 || initSpeed < 0 || mateStrategy == null) {
			throw new IllegalArgumentException("Los parametros no son validos");
		}
		
		this.geneticCode = geneticCode;
		this.diet = diet;
		this.sightRange = sightRange;
		this.pos = pos;
		this.mateStrategy = mateStrategy;
		this.speed = Utils.getRandomizedParameter(initSpeed, 0.1);
		this.state = State.NORMAL;
		this.energy = MAX_ENERGY;
		this.desire = 0.0;
		this.dest = null;
		this.mateTarget= null;
		this.baby = null;
		this.regionMngr = null;
		
	}
	
	protected Animal(Animal p1, Animal p2) {
		this.state = State.NORMAL;
		this.energy = (p1.energy + p2.energy)/2;
		this.desire = 0.0;
		this.dest = null;
		this.mateTarget= null;
		this.baby = null;
		this.regionMngr = null;
		this.geneticCode = p1.getGeneticCode();
		this.diet = p1.getDiet();
		this.mateStrategy = p2.mateStrategy;
		this.sightRange = Utils.getRandomizedParameter((p1.sightRange+p2.getSightRange())/2, MUTATION_TOLERANCE);
		this.pos = p1.getPosition().plus(Vector2D.getRandomVector(-1,1).scale(NEARBY_FACTOR*
				(Utils.RAND.nextGaussian()+1)));
		this.speed = Utils.getRandomizedParameter((p1.getSpeed()+p2.getSpeed())/2, MUTATION_TOLERANCE);
	}
	
	protected void selectMateTarget() {
		mateTarget = mateStrategy.select(this, regionMngr.getAnimalsInRange(this, animal -> (animal.getGeneticCode() == this.getGeneticCode() && !animal.equals(this))));
	}
	
	protected void setDesire(double d) {
		this.desire = d;
	}

	
	// GETTERS
	@Override
	public void update(double dt) {
		// TODO Auto-generated method stub
	}

	@Override
	public Vector2D getPosition() {
		return this.pos;
	}

	@Override
	public String getGeneticCode() {
		return this.geneticCode;
	}

	@Override
	public double getSpeed() {
		return this.speed;
	}

	@Override
	public double getSightRange() {
		return this.sightRange;
	}

	@Override
	public double getEnergy() {
		return this.energy;
	}

	@Override
	public double getAge() {
		return this.age;
	}

	@Override
	public Vector2D getDestination() { 
		return this.dest;
	}

	@Override
	public boolean isPregnant() {
		if(baby != null) return true;
		return false;
	}
	
	
	//METODOS NECESARIOS
	public void init(AnimalMapView regMngr) {
		this.regionMngr = regMngr;
		if(this.pos == null) {
			pos = Vector2D.getPosAleatoria(0.0, regionMngr.getWidth()-1, 0.0, regionMngr.getHeight()-1);
		} else {
			pos = pos.ajustaPosicion(regionMngr.getWidth()-1, regionMngr.getHeight()-1);
		}
		this.dest =  Vector2D.getPosAleatoria(0.0, regionMngr.getWidth()-1, 0.0, regionMngr.getHeight()-1);
	}

	public Animal deliverBaby() {
		Animal deliveredBaby = this.baby;
		this.baby = null;
		return deliveredBaby;
	}
	
	protected void move(double speed) {
		pos = pos.plus(dest.minus(pos).direction().scale(speed));
	}
	
	
	abstract protected void setNormalStateAction();
	abstract protected void setMateStateAction();
	abstract protected void setHungerStateAction();
	abstract protected void setDangerStateAction();
	abstract protected void setDeadStateAction();
	
	protected void setState(State state) {
	  	this.state = state;
	  	switch (state) {
	  	case NORMAL:
	  		setNormalStateAction();
	  		break;
	  	case HUNGER:
	        setHungerStateAction();
	        break;
	  	case MATE: 
	  		setMateStateAction();
	  		break;
	  	case DANGER:  
	  		setDangerStateAction();
	  		break;
	  	case DEAD: 
	  		setDeadStateAction();
	  		break;
	  	/*default: 
	  		throw new Exception("El estado no es valido");*/
	    }
	  }
	
	public JSONObject asJSON() {
		JSONObject jsonObject = new JSONObject();
		
		jsonObject.put("pos", this.pos);
		jsonObject.put("gcode", this.geneticCode);
		jsonObject.put("diet", this.diet);
		jsonObject.put("state", this.state);
				
		//System.out.println(jsonObject.toString(3));	//PARA SACARLO POR PANTALLA POR LINEAS SEPARADAS	
		
		return jsonObject;
	}
}
