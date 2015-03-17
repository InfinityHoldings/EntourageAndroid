package com.vector.asynctask;

import org.json.JSONException;
import org.json.JSONObject;
import com.google.gson.Gson;

public class HttpJSONParser {
	JSONObject jsonObj = null;
	String jsonString = "";

	public Object parseToClass(JSONObject json, Object classObj) {
		Gson gson = new Gson();
		Object classObject = gson
				.fromJson(json.toString(), classObj.getClass());
		return classObject;
	}

	public JSONObject parseToJSON(Object classObj) {
		Gson gson = new Gson();
		jsonString = gson.toJson(classObj);
		try {
			jsonObj = new JSONObject(jsonString);
			return jsonObj;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
