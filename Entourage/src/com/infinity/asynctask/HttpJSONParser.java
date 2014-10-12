package com.infinity.asynctask;

import org.json.JSONObject;

import com.google.gson.Gson;

public class HttpJSONParser extends HttpParser {

	public Object parse(JSONObject json, Class<?> myclass) {
		Gson gson = new Gson();
		Object classObject = gson.fromJson(json.toString(), myclass);
		return classObject;
	}
}
