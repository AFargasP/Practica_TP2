package simulator.factories;

import org.json.JSONObject;

import simulator.model.SelectFirst;

public class SelectFirstBuilder extends Builder<SelectFirst>{

	public SelectFirstBuilder() {
		super("first", "SelectFirst");
	}

	@Override
	protected SelectFirst createInstance(JSONObject data) {
		
		return new SelectFirst();
	}

}
