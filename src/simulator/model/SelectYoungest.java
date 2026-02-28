package simulator.model;

import java.util.List;

public class SelectYoungest implements SelectionStrategy {
	
	

	@Override
	public Animal select(Animal a, List<Animal> as) {
		double edad = 20;
		Animal masJoven = null;
		for(int i = 0; i < as.size(); i++) {
			if(as.get(i).getAge() < edad) {
				masJoven = as.get(i);
				edad = masJoven.getAge();
			}
		}
		return masJoven;
	}

}
