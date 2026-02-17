package simulator.model;

import java.util.List;

public class SelectClosest implements SelectionStrategy{

	@Override
	public Animal select(Animal a, List<Animal> as) {
		Animal masCerca = as.getFirst();
		double distancia = a.getPosition().distanceTo(as.getFirst().getPosition());
		for(int i = 1; i < as.size(); i++) {
			if(a.getPosition().distanceTo(as.get(i).getPosition()) < distancia) {
				masCerca = as.get(i);
				distancia = a.getPosition().distanceTo(as.get(i).getPosition());
			}
		}
		return masCerca;
	}

}
