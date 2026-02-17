package simulator.control;

import org.json.JSONObject;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import simulator.model.AnimalInfo;
import simulator.model.MapInfo;
import simulator.model.Simulator;
import simulator.view.SimpleObjectViewer;
import simulator.view.SimpleObjectViewer.ObjInfo;

public class Controller {
	
	Simulator sim;
	
	public Controller(Simulator sim) {
		this.sim = sim;
	}
	
	
	public void loadData(JSONObject data) {
		//AÑADIR REGIONES SI LAS HAY
		if(data.has("regions")) {
			JSONArray jsonRegions = data.getJSONArray("regions");
			for (int i = 0; i < jsonRegions.length(); i++) {
				
				JSONArray jsonRows = jsonRegions.getJSONArray(i).getJSONArray(0);
				int initRows = jsonRows.getInt(0);
				int finalRows = jsonRows.getInt(1);
				
				JSONArray jsonCols = jsonRegions.getJSONArray(i).getJSONArray(1);
				int initCols = jsonCols.getInt(0);
				int finalCols = jsonCols.getInt(1);
				
				JSONObject jsonRegion = jsonRegions.getJSONObject(i).getJSONObject("spec");
				
				for (int j = initRows; j < finalRows; j++) {
					for (int k = initCols; k < finalCols; k++) {
						sim.setRegion(j, k, jsonRegion);;
					}	
				}
			}	
		}
		
		//AÑADIR ANIMALES
		JSONArray jsonAnimals = data.getJSONArray("animals");
		
		for (int i = 0; i < jsonAnimals.length(); i++) {
			
			int n = jsonAnimals.getJSONObject(i).getInt("amount");
			JSONObject jsonAnimal = jsonAnimals.getJSONObject(i).getJSONObject("spec");
			
			for (int j = 0; j < n; j++) {
				sim.addAnimal(jsonAnimal);
			}	
		}
	}
	
	private List<ObjInfo> toAnimalsInfo(List<? extends AnimalInfo> animals) {
		List<ObjInfo> ol = new ArrayList<>(animals.size());
		for (AnimalInfo a : animals)
			ol.add(new ObjInfo(a.getGeneticCode(), (int) a.getPosition().getX(), (int) a.getPosition().getY(),8));
		return ol;
	}
	
	
	public void run(double t, double dt, boolean sv, OutputStream out) {
		SimpleObjectViewer view = null;  
		if (sv) {  
		   MapInfo m = sim.getMapInfo();  
		   view = new SimpleObjectViewer("[ECOSYSTEM]", m.getWidth(), m.getHeight(), m.getCols(), m.getRows());  
		   view.update(toAnimalsInfo(sim.getAnimals()), sim.getTime(), dt);  
		}
		
		JSONObject jsonRun = new JSONObject();
		jsonRun.put("in", sim.asJSON());
		
		while(sim.getTime() > t) {
			sim.advance(dt);
			if (sv) view.update(toAnimalsInfo(sim.getAnimals()), sim.getTime(), dt);
		}
		
		jsonRun.put("out", sim.asJSON());
		
		PrintStream p = new PrintStream(out);
		p.print(jsonRun);

	}
}
