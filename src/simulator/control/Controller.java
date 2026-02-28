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
	
	private Simulator sim;
	
	public Controller(Simulator sim) {
		this.sim = sim;
	}
	
	public void loadData(JSONObject data) {
		// AÑADIR REGIONES SI LAS HAY
		if(data.has("regions")) {
			JSONArray jsonRegions = data.getJSONArray("regions");
			for (int i = 0; i < jsonRegions.length(); i++) {   //hasta que no quede nada en el json
				
				JSONObject infoRegion = jsonRegions.getJSONObject(i); //hacemos la region un jsonObject
				
				JSONArray jsonRows = infoRegion.getJSONArray("row"); //sacamos un jsonArray de las filas
				int initRows = jsonRows.getInt(0);
				int finalRows = jsonRows.getInt(1);
				
				JSONArray jsonCols = infoRegion.getJSONArray("col"); //lo mismo que las filas pero con columnas
				int initCols = jsonCols.getInt(0);
				int finalCols = jsonCols.getInt(1);
				
				JSONObject jsonRegion = infoRegion.getJSONObject("spec"); //json q se le pasa a setRegion en simulador
				
				for (int j = initRows; j <= finalRows; j++) {
					for (int k = initCols; k <= finalCols; k++) {
						sim.setRegion(j, k, jsonRegion);					//construye una region a partir del json
					}	
				}
			}	
		}
		
		// AÑADIR ANIMALES
		JSONArray jsonAnimals = data.getJSONArray("animals");
		
		for (int i = 0; i < jsonAnimals.length(); i++) {		// hasta que se acabe el json de animales

			JSONObject infoAnimal = jsonAnimals.getJSONObject(i);	//saca un json object de cada tipo de animal
			int n = infoAnimal.getInt("amount");					//numero de animales de ese tipo
			JSONObject jsonAnimal = infoAnimal.getJSONObject("spec");   //json que se le va a pasar a addAnimal en simulador
			
			for (int j = 0; j < n; j++) {
				sim.addAnimal(jsonAnimal);					//añade el animal a partir del json
			}	
		}
	}
	
	private List<ObjInfo> toAnimalsInfo(List<? extends AnimalInfo> animals) {
		List<ObjInfo> ol = new ArrayList<>(animals.size());
		for (AnimalInfo a : animals) {
			ol.add(new ObjInfo(a.getGeneticCode(), (int) a.getPosition().getX(), (int) a.getPosition().getY(), (int)Math.round(a.getAge()) + 2)); //Controla el tamaño en funcion de la edad
		}
		return ol;
	}
	
	public void run(double t, double dt, boolean sv, OutputStream out) {
		JSONObject jsonOut = new JSONObject();
		JSONObject jsonIn = new JSONObject();
		JSONObject jsonRun = new JSONObject();
		SimpleObjectViewer view = null;
		
		
		if (sv) {
			MapInfo m = sim.getMapInfo();
			view = new SimpleObjectViewer("[ECOSYSTEM]", m.getWidth(), m.getHeight(), m.getCols(), m.getRows());
			view.update(toAnimalsInfo(sim.getAnimals()), sim.getTime(), dt);
		}
		
		jsonIn = sim.asJSON();					//json de entrada de los objetos
		while(sim.getTime() <= t) {
			sim.advance(dt);
			if (sv) {
				view.update(toAnimalsInfo(sim.getAnimals()), sim.getTime(), dt);
			}
		}
		jsonOut = sim.asJSON();					//json de salida de los objetos
		
		jsonRun.put("in", jsonIn);
		jsonRun.put("out", jsonOut);
		
		PrintStream p = new PrintStream(out);
		
		p.println(jsonRun.toString(3));
		view.close();

	}
}