package simulator.factories;

import org.json.JSONObject;

import simulator.model.DinamicSupplyRegion;
import simulator.model.Region;

public class DinamicSupplyRegionBuilder extends Builder<Region> {

	public DinamicSupplyRegionBuilder() {
		super("dynamic", "Dynamic supply region");
	}

	@Override
	protected DinamicSupplyRegion createInstance(JSONObject data) {
		double food = 100.0;
		if(data.has("food")) food = data.getDouble("food");
		double factor = 2.0;
		if(data.has("factor")) factor = data.getDouble("factor");
		return new DinamicSupplyRegion(food, factor);
	}

}
