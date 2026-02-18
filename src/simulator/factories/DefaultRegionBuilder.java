package simulator.factories;

import org.json.JSONObject;

import simulator.model.DefaultRegion;

public class DefaultRegionBuilder extends Builder<DefaultRegion>{

	public DefaultRegionBuilder() {
		super("default", );
	}

	@Override
	protected DefaultRegion createInstance(JSONObject data) {
		return new DefaultRegion();
	}

}
