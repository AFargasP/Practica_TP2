package simulator.factories;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Vector2D;
import simulator.model.Animal;
import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;
import simulator.model.Sheep;

public class SheepBuilder extends Builder<Animal> {
	private Factory<SelectionStrategy> selectionStrategy;

	public SheepBuilder(Factory<SelectionStrategy> selectionStrategyFactory) {
		super("sheep", "Sheep");
		this.selectionStrategy = selectionStrategyFactory;
	}

	@Override
	protected Sheep createInstance(JSONObject data) {
		
		SelectionStrategy mateStrategy;
		if(data.has("mate_strategy")) mateStrategy = selectionStrategy.createInstance(data.getJSONObject("mate_strategy"));
		else mateStrategy = new SelectFirst();
		
		SelectionStrategy dangerStrategy;
		
		if(data.has("danger_strategy")) dangerStrategy = selectionStrategy.createInstance(data.getJSONObject("danger_strategy"));
		else dangerStrategy = new SelectFirst();
		
		Vector2D pos = null;
		
		if(data.has("pos")) {
			JSONObject jsonPos = data.getJSONObject("pos");
			JSONArray jsonX_range = jsonPos.getJSONArray("x_range");
			int initX = jsonX_range.getInt(0);
			int finalX = jsonX_range.getInt(1);
			
			JSONArray jsonY_range = jsonPos.getJSONArray("y_range");
			int initY = jsonY_range.getInt(0);
			int finalY = jsonY_range.getInt(1);
			
			pos = Vector2D.getPosAleatoria(initX, finalX, initY, finalY);
			
		}
		return new Sheep(mateStrategy, dangerStrategy, pos);
	}
}
