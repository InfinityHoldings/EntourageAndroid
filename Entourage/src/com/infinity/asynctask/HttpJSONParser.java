package com.infinity.asynctask;

import org.json.JSONObject;

import com.google.gson.Gson;

public class HttpJSONParser extends HttpParser{
	
	
	public Object parse(JSONObject json, Class<?> myclass){
		
		 Gson gson = new Gson(); // Or use new GsonBuilder().create();
		// MyType target = new MyType();
		 //String json = gson.toJson(target); // serializes target to Json
		 Object classObject = gson.fromJson(json.toString(), myclass); // deserializes json into target2
		
		
		return classObject;
		
	}

}
