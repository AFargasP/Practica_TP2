package simulator.model;

public class DefaultRegion extends Region {
	
	// se usan en DefaultRegion
	final static double FOOD_EAT_RATE_HERBS = 60.0;
	final static double FOOD_SHORTAGE_TH_HERBS = 5.0;
	final static double FOOD_SHORTAGE_EXP_HERBS = 2.0;
	
	@Override
	public double getFood(AnimalInfo a, double dt) {
		if(a.getDiet() == Diet.CARNIVORE) return 0.0;
		else return FOOD_EAT_RATE_HERBS*Math.exp(-Math.max(0, herbivoresInRegion()-FOOD_SHORTAGE_TH_HERBS)*FOOD_SHORTAGE_EXP_HERBS)*dt;
	}

	@Override
	public void update(double dt) {
		// TODO Auto-generated method stub
		
	}
}
