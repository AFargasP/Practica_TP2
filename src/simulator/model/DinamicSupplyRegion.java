package simulator.model;

import simulator.misc.Utils;

public class DinamicSupplyRegion extends Region {
	// se usan en DynamicSupplyRegion
	final static double FOOD_EAT_RATE_HERBS = 60.0;
	final static double FOOD_SHORTAGE_TH_HERBS = 5.0;
	final static double FOOD_SHORTAGE_EXP_HERBS = 2.0;
	final static double INIT_FOOD = 100.0;
	final static double FACTOR = 2.0;
	
	private double food;
	private double factor;
	
	public DinamicSupplyRegion(double initialFood, double incrementFactor) {
		food = initialFood;
		factor = incrementFactor;
	}
	
	
	
	public double getFood(AnimalInfo a, double dt) {
		if(a.getDiet() == Diet.CARNIVORE) return 0.0;
		else {
			food -= FOOD_EAT_RATE_HERBS*Math.exp(-Math.max(0,herbivoresInRegion()-FOOD_SHORTAGE_TH_HERBS)*FACTOR)*dt;
			return FOOD_EAT_RATE_HERBS*Math.exp(-Math.max(0,herbivoresInRegion()-FOOD_SHORTAGE_TH_HERBS)*FACTOR)*dt;
		}
	}

	@Override
	public void update(double dt) {
		double x = Utils.RAND.nextDouble(0, 1);
		if(x >= 0.5) {
			food = (food*dt*factor);
		}
	}
}
