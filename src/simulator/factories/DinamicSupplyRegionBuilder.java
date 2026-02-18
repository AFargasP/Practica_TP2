package simulator.factories;

import org.json.JSONObject;

import simulator.model.DinamicSupplyRegion;

public class DinamicSupplyRegionBuilder extends Builder<DinamicSupplyRegion> {

	public DinamicSupplyRegionBuilder() {
		super("dynamic", );
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
