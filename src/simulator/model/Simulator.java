package simulator.model;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import simulator.factories.Factory;

public class Simulator implements JSONable{
	Factory<Animal> animalFactory;
	Factory<Region> regionFactory;
	
	RegionManager regionManager;
	double actualTime;
	List<Animal> listaAnimalSimulation;
	
	public Simulator(int cols, int rows, int width, int height,   
            Factory<Animal> animalsFactory, Factory<Region> regionsFactory) {
		
		this.animalFactory = animalsFactory;
		this.regionFactory = regionsFactory;
		this.actualTime = 0.0;
		this.regionManager = new RegionManager(cols, rows, width, height);
		this.listaAnimalSimulation = new ArrayList<>();
	}
	
	private void setRegion(int row, int col, Region r) {
		regionManager.setRegion(row, col, r);
	}
	
	public void setRegion(int row, int col, JSONObject rJson) {
		Region reg = regionFactory.createInstance(rJson);
		setRegion(row, col, reg);
		
	}
	
	private void addAnimal(Animal a) {
		listaAnimalSimulation.add(a);
		regionManager.registerAnimal(a);
	}
	
	public void addAnimal(JSONObject aJson) {
		Animal a = animalFactory.createInstance(aJson);
		addAnimal(a);
	}
	
	public MapInfo getMapInfo() {
		return regionManager;
	}
	
	public List<? extends AnimalInfo> getAnimals() {
		return listaAnimalSimulation;
	}
	
	public double getTime() {
		return actualTime;
	}
	
	
	public void advance(double dt) {
		actualTime += dt;
		listaAnimalSimulation.removeIf(animal -> animal.getState()==State.DEAD);
		for(Animal a: listaAnimalSimulation) {
			a.update(dt);
			regionManager.updateAnimalRegion(a);
		}
		
		regionManager.updateAllRegions(dt);
		
		List<Animal> nuevos = new ArrayList<>();

		for (Animal a : listaAnimalSimulation) {
		    if (a.isPregnant()) {
		        nuevos.add(a.deliverBaby());
		    }
		}
		listaAnimalSimulation.addAll(nuevos);
	}
	
	public JSONObject asJSON() {
		JSONObject jsonSim = new JSONObject();
		
		jsonSim.put("time", actualTime);
		jsonSim.put("state", regionManager.asJSON());
		
		return jsonSim;
		
	}
	

}
