package simulator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.json.JSONObject;

import simulator.misc.Vector2D;

public class RegionManager implements AnimalMapView{
	private int cols;
	private int rows;
	private int mapWidth;
	private int mapHeight;
	private int regionWidth;
	private int regionHeight;
	
	private Region[][] regions = new Region[rows][cols];
	private Map<Animal, Region> animalRegion;
	
	public RegionManager(int cols, int rows, int width, int height) {
		this.cols = cols;
		this.rows = rows;
		this.mapWidth = width;
		this.mapHeight = height;
		this.regionWidth = mapWidth/cols;
		this.mapHeight = mapHeight/rows;
		
		for (int i = 0; i < rows; i++) { 
		    for (int j = 0; j < cols; j++) {
		        regions[i][j]  = new DefaultRegion();
		    }
		}
		
		this.animalRegion = new HashMap<>();
	}
	
	private int regionXdelAnimal(Animal a) {
		int x = (int) (a.getPosgetX()/regionWidth);
.		
	}
	
	public void setRegion(int row, int col, Region r) {
		Region oldRegion = regions[col][row];
		List<Animal> animalsOldRegion = oldRegion.getAnimals();
		for(Animal a : animalsOldRegion) {
			r.addAnimal(a);
			animalRegion.replace(a, oldRegion, r);
		}
		regions[col][row] = r;
	}
	
	private Region regionDelAnimal(Vector2D v) {
		int x = (int) (v.getX()/regionWidth);
		int y = (int) (v.getY()/regionHeight);
		return regions[x+1][y+1];
	}
	
	public void registerAnimal(Animal a) {
		if(animalRegion.containsKey(a)) {
			throw new IllegalArgumentException("Este animal ya existe");
		}
		a.init(this);
		Region regionDelAnimal = regionDelAnimal(a.getPosition());
		regionDelAnimal.addAnimal(a);
		animalRegion.put(a, regionDelAnimal);
	}
	
	public void unregisterAnimal(Animal a) {
		Region oldRegion = animalRegion.get(a);
		oldRegion.removeAnimal(a);
		animalRegion.remove(a);
	}
	
	public void updateAnimalRegion(Animal a) {
		Region regCorrecta = regionDelAnimal(a.getPosition());
		Region regIncorrecta = animalRegion.get(a);
		if(regCorrecta != regIncorrecta) {
			regIncorrecta.removeAnimal(a);
			regCorrecta.addAnimal(a);
			animalRegion.replace(a, regIncorrecta, regCorrecta);
			
		}
	}
	
	@Override
	public double getFood(AnimalInfo a, double dt) {
		Region reg = animalRegion.get(a);
		return reg.getFood(a, dt);
	}
	
	public void updateAllRegions(double dt) {
		for (int i = 0; i < rows; i++) { 
		    for (int j = 0; j < cols; j++) {
		        regions[i][j].update(dt);
		    }
		}
	}
	
	@Override
	public int getCols() {
		return this.cols;
	}

	@Override
	public int getRows() {
		return this.rows;
	}

	@Override
	public int getWidth() {
		return this.mapWidth;
	}

	@Override
	public int getHeight() {
		return this.mapHeight;
	}

	@Override
	public int getRegionWidth() {
		return this.regionWidth;
	}

	@Override
	public int getRegionHeight() {
		return this.regionHeight;
	}

	@Override
	public List<Animal> getAnimalsInRange(Animal e, Predicate<Animal> filter) {
		List<Animal> animalsInRange = new ArrayList<>();
		Predicate<Animal> filterDistance = animal -> (animal.getPosition().distanceTo(e.getPosition()) < animal.getSightRange());
		
		for (int i = 0; i < rows; i++) { 
		    for (int j = 0; j < cols; j++) {
		    	double distanciaX = Math.abs(e.getPosition().getX() - regionWidth);
		    	double distanciaY = Math.abs(e.getPosition().getY() - regionHeight);
		    	if(distanciaX < e.getSightRange() || distanciaY < e.getSightRange()) {
		    		animalsInRange = regions[i][j].getAnimals().stream().filter(filter).filter(filterDistance).collect(Collectors.toList());
		    	}
		    }
		}
		return animalsInRange;
	}
	
	public JSONObject asJSON() {
		JSONObject jsonRegion = new JSONObject();
		JSONObject jsonRegions = new JSONObject();
		
		for (int i = 0; i < rows; i++) { 
		    for (int j = 0; j < cols; j++) {
		    	jsonRegion.put("row", i);
				jsonRegion.put("col", j);
				jsonRegion.put("data", regions[i][j].asJSON());
				
		    }
		}
		jsonRegions.put("regions", jsonRegion);
		return jsonRegions;
	}


}
