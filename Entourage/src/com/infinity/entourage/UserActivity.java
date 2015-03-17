package com.infinity.entourage;

import org.json.JSONException;
import org.json.JSONObject;

import com.infinity.asynctask.EntourageAsyncTask;
import com.infinity.asynctask.HttpClientJSONPOST;
import com.infinity.asynctask.Routes;

import controllers.EntourageUser;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class UserActivity extends Activity implements EntourageAsyncTask.AsyncTaskCompleteListener{

	EditText fullName;
	EditText userName;
	EditText city;
	EditText state;
	EditText email;
	EditText password;
	EditText join;

	EntourageUser user;
		
	String url; 
	JSONObject jsonRequestObj; 

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		//get pref handle to sessionid 
		
		jsonRequestObj = new JSONObject(); 
		
		SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE); 
		String sessionId = prefs.getString("sessionid", ""); 
		String userName = prefs.getString("username", ""); 
		try {
			jsonRequestObj.put("sessionid", sessionId);
			jsonRequestObj.put("username", userName);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		Button logout = (Button) findViewById(R.id.btnLogout);
		 

		logout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				url = Routes.LOGOUT.getUrl(); 
				//executeTask(Routes.LOGOUT.getUrl(), jsonRequestObj); 
				new EntourageAsyncTask(getBaseContext(), UserActivity.this).execute(url,jsonRequestObj.toString() );
			}
		});

		Button update = (Button) findViewById(R.id.btnUpdateUser);

		update.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

			}
		});

	}

	@Override
	public void onTaskComplete(String result) {
		// TODO Auto-generated method stub
		
		//compare the value of the outer json object 
		JSONObject jsonResponseObj;
		try {
			jsonResponseObj = new JSONObject(result);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		//or we could test for the returned root
		//that or disable all buttons so the url cant be changed in between requests
		if (url.equals(Routes.LOGOUT.getUrl())){
			
			SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE); 
			SharedPreferences.Editor editor = prefs.edit(); 
			
			editor.remove("sessionid"); 
			editor.remove("username"); 
			editor.putBoolean("validated", false); 
			editor.remove("expiration"); 
			editor.commit(); 
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
		}
		
		if (url.equals(Routes.UPDATEUSER.getUrl())){
			Intent intent = new Intent(this, EntourageMainActivity.class);
			startActivity(intent);
		}
		
		//if .equal("logout") 
		//clear the preferences 
		//back to login screen 
	
		
		
	}
	
	//I wrapped the call to the asynctask in this method because i couldnt get a proper handle to EntourageActivity.this
//	public void executeTask(String url, JSONObject json){
//		new EntourageAsyncTask(getBaseContext(), this).execute();
//	}
	
//	public class UserHttpAsyncTask extends AsyncTask<String, Void, String>{

//		JSONObject jsonRequestObject; 
//		JSONObject jsonResponseObject; 
//		String jsonResponseString; 
//		String url; 
//	
//		
//		@Override
//		protected String doInBackground(String... params) {
//			// TODO Auto-generated method stub
//			
//			url = params[0]; 
//			try {
//				jsonRequestObject = new JSONObject( params[1]);
//				HttpClientJSONPOST post = new HttpClientJSONPOST(url, jsonRequestObject); 
//				jsonResponseString = post.executePOST(); 
//				
//				
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} 
//			return jsonResponseString;
//		}
//		
//		@Override
//		public void onPostExecute(String result){
//			super.onPostExecute(result);
//			
//			try {
//				jsonResponseObject = new JSONObject(result);
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} 
//			
//			try {
//				String success = jsonResponseObject.getJSONObject("User").getString("success");
//				String success = jsonResponseObj.getJSONObject("User").getString("success"); 
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} 
//			
//			Intent intent = new Intent(getBaseContext(), LoginActivity.class);
//			startActivity(intent);
//			
//		}
//		
//		
//		
//		
//	}

}
