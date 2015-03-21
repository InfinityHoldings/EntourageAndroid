package com.vector.async;

import org.json.JSONException;
import org.json.JSONObject;

import com.vector.entourage.LoginActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

public class EntourageAsyncTask extends AsyncTask<String, Integer, String>{

	public interface AsyncTaskCompleteListener{
	
		public void onTaskComplete(String result); 
	}
	
	Context ctx; 
	AsyncTaskCompleteListener listener; 
	
	JSONObject jsonRequestObject; 
	JSONObject jsonResponseObject; 
	String jsonResponseString; 
	String url; 
	
	public EntourageAsyncTask(Context ctx,AsyncTaskCompleteListener listener ){
		this.ctx = ctx; 
		this.listener = listener; 
	}

	
	@Override
	protected String doInBackground(String... params) {
		// TODO Auto-generated method stub
		
		url = params[0]; 
		
		try {
			jsonRequestObject = new JSONObject( params[1]);
			HttpClientJSONPOST post = new HttpClientJSONPOST(url, jsonRequestObject); 
			jsonResponseString = post.executePOST(); 
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return jsonResponseString;
	}
	
	@Override
	public void onPostExecute(String result){
		super.onPostExecute(result);
		
		try {
			jsonResponseObject = new JSONObject(result);
			listener.onTaskComplete(result);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	
	
	
	
}

	


