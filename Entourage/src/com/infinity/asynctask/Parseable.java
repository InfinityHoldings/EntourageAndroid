package com.infinity.asynctask;

import org.json.JSONObject;

public interface Parseable {
	
	public Object parse(JSONObject json, Class<?> classtype); 

}
