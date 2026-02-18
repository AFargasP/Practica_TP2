package simulator.factories;

import org.json.JSONObject;

import simulator.model.SelectYoungest;

public class SelectYoungestBuilder extends Builder<SelectYoungest>{

	public SelectYoungestBuilder() {
		super("youngest", "SelectYoungest");
	}

	@Override
	protected SelectYoungest createInstance(JSONObject data) {
		return new SelectYoungest();
	}

}
