package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Region implements Entity, FoodSupplier, RegionInfo {
	protected List<Animal> listaAnimales = new ArrayList<>();
	
	final void addAnimal(Animal a) {
		listaAnimales.add(a);
	}
	
	final void removeAnimal(Animal a) {
		listaAnimales.remove(a);
	}
	
	final List<Animal> getAnimals() {
		return Collections.unmodifiableList(listaAnimales);
	}
	
	public JSONObject asJSON() {
		JSONObject jsonListaAnimales = new JSONObject();
		JSONArray animalJson = new JSONArray();
		
		for(Animal a: listaAnimales) {
			animalJson.put(a.asJSON());
		}
		
		jsonListaAnimales.put("animals", animalJson);
		return jsonListaAnimales;
		
	}
	
	public int herbivoresInRegion() {
		int contHerbivores = 0;
		for(Animal a: listaAnimales) {
			if(a.getDiet() == Diet.HERBIVORE) contHerbivores++;
		}
		return contHerbivores;
	}
	
	@Override
	public double getFood(AnimalInfo a, double dt) {
		return 0.0;
	}
	
	@Override
	public void update(double dt) {}

}
