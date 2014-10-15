package com.infinity.asynctask;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

public class HttpJSONParser extends HttpParser{
	
	
	public Object parseToClass(JSONObject json, Object classObj){
		
		 Gson gson = new Gson(); // Or use new GsonBuilder().create();
		// MyType target = new MyType();
		 //String json = gson.toJson(target); // serializes target to Json
		 Object classObject = gson.fromJson(json.toString(), classObj.getClass()); // deserializes json into target2
		
		return classObject;
		
	}

	
	public JSONObject parseToJSON(Object classObj){
		 JSONObject jsonObj; 
		 String jsonString; 
		 
		 Gson gson = new Gson(); // Or use new GsonBuilder().create();
		 jsonString = gson.toJson(classObj); 
		 try {
			jsonObj = new JSONObject(jsonString);
			return jsonObj; 
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
			
		
		return null;
		
	}
}
