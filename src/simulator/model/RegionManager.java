package simulator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class RegionManager implements AnimalMapView{
	private int cols;
	private int rows;
	private int mapWidth;
	private int mapHeight;
	private int regionWidth;
	private int regionHeight;
	
	private Region[][] regions;
	private Map<Animal, Region> animalRegion;
	
	public RegionManager(int cols, int rows, int width, int height) {
		this.cols = cols;
		this.rows = rows;
		this.mapWidth = width;
		this.mapHeight = height;
		this.regionWidth = mapWidth/cols;
		this.regionHeight = mapHeight/rows;
		
		regions = new Region[rows][cols];
		
		for (int i = 0; i < rows; i++) { 
		    for (int j = 0; j < cols; j++) {
		        regions[i][j]  = new DefaultRegion();
		    }
		}
		
		this.animalRegion = new HashMap<Animal, Region>();
	}
	
	
	public void setRegion(int row, int col, Region r) {
		Region oldRegion = regions[row][col];
		List<Animal> animalsOldRegion = oldRegion.getAnimals();
		for(Animal a : animalsOldRegion) {
			r.addAnimal(a);
			animalRegion.replace(a, oldRegion, r);
		}
		regions[row][col] = r;
	}
	
	private Region regionDelAnimal(Vector2D v) {
		int x = (int)v.getX()/regionWidth;
		int y = (int)v.getY()/regionHeight;
		
		return regions[y][x];
	}
	
	private int regionXdelAnimal(double dist) {
		int x = (int)(dist/regionWidth);
		return x;
	}
	
	private int regionYdelAnimal(double dist) {
		int y = (int)(dist/regionHeight);
		return y;
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

	    Predicate<Animal> filterDistance =
	            a -> a.getPosition().distanceTo(e.getPosition()) < e.getSightRange();

	    double x = e.getPosition().getX();
	    double y = e.getPosition().getY();
	    double r = e.getSightRange();

	    int initX = (int) Utils.constrainValueInRange(regionXdelAnimal(x - r), 0, cols - 1);
	    int finalX = (int) Utils.constrainValueInRange(regionXdelAnimal(x + r), 0, cols - 1);
	    int initY = (int) Utils.constrainValueInRange(regionYdelAnimal(y - r), 0, rows - 1);
	    int finalY = (int) Utils.constrainValueInRange(regionYdelAnimal(y + r), 0, rows - 1);

	    for (int i = initY; i <= finalY; i++) {
	        for (int j = initX; j <= finalX; j++) {

	            animalsInRange.addAll(
	                regions[i][j].getAnimals()
	                    .stream()
	                    .filter(filter)
	                    .filter(filterDistance)
	                    .collect(Collectors.toList())
	            );
	        }
	    }

	    return animalsInRange;
	}
	
	public JSONObject asJSON() {
		JSONObject jsonRegions = new JSONObject();
		JSONArray jsonArrayRegiones = new JSONArray();
		
		for (int i = 0; i < rows; i++) { 
		    for (int j = 0; j < cols; j++) {
		    	JSONObject jsonRegion = new JSONObject();
		    	jsonRegion.put("row", i);
				jsonRegion.put("col", j);
				jsonRegion.put("data", regions[i][j].asJSON());
				
				jsonArrayRegiones.put(jsonRegion);
				
		    }
		}
		
		jsonRegions.put("regions", jsonArrayRegiones);
		return jsonRegions;
	}


}
