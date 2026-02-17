package simulator.model;

import java.util.List;

public class SelectYoungest implements SelectionStrategy {
	
	

	@Override
	public Animal select(Animal a, List<Animal> as) {
		Animal masJoven = as.getFirst();
		double edad = as.getFirst().getAge();
		for(int i = 1; i < as.size(); i++) {
			if(as.get(i).getAge() < edad) {
				masJoven = as.get(i);
				edad = masJoven.getAge();
			}
		}
		return masJoven;
	}

}
