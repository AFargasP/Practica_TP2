package simulator.model;

import java.util.List;

public class SelectClosest implements SelectionStrategy{

	@Override
	public Animal select(Animal a, List<Animal> as) {
		Animal masCerca = null;
		double distancia = 1000;
		for(int i = 0; i < as.size(); i++) {
			if(a.getPosition().distanceTo(as.get(i).getPosition()) < distancia) {
				masCerca = as.get(i);
				distancia = a.getPosition().distanceTo(as.get(i).getPosition());
			}
		}
		return masCerca;
	}

}
