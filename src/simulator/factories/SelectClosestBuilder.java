package simulator.factories;

import org.json.JSONObject;

import simulator.model.SelectClosest;

public class SelectClosestBuilder extends Builder<SelectClosest> {

	public SelectClosestBuilder() {
		super("closest", "SelectClosest");
	}

	@Override
	protected SelectClosest createInstance(JSONObject data) {
		return new SelectClosest();
	}

}
