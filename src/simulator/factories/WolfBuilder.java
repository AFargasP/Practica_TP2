package simulator.factories;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Vector2D;
import simulator.model.Animal;
import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;
import simulator.model.Sheep;
import simulator.model.Wolf;

public class WolfBuilder extends Builder<Animal> {
	private Factory<SelectionStrategy> selectionStrategy;


	public WolfBuilder(Factory<SelectionStrategy> selectionStrategyFactory) {
		super("wolf", "Wolf");
		this.selectionStrategy = selectionStrategyFactory;
		
		
	}

	@Override
	protected Wolf createInstance(JSONObject data) {
		SelectionStrategy mateStrategy;
		if(data.has("mate_strategy")) mateStrategy = selectionStrategy.createInstance(data.getJSONObject("mate_strategy"));
		else mateStrategy = new SelectFirst();
		
		SelectionStrategy huntStrategy;
		
		if(data.has("hunt_strategy")) huntStrategy = selectionStrategy.createInstance(data.getJSONObject("hunt_strategy"));
		else huntStrategy = new SelectFirst();
		
		Vector2D pos = null;
		
		if(data.has("pos")) {
			JSONArray jsonPos = data.getJSONArray("pos");
			JSONArray jsonX_range = jsonPos.getJSONArray(0);
			int initX = jsonX_range.getInt(0);
			int finalX = jsonX_range.getInt(1);
			
			JSONArray jsonY_range = jsonPos.getJSONArray(1);
			int initY = jsonY_range.getInt(0);
			int finalY = jsonY_range.getInt(1);
			
			pos = Vector2D.getPosAleatoria(initX, finalX, initY, finalY);
			
		}
		return new Wolf(mateStrategy, huntStrategy, pos);
	}
	

}
