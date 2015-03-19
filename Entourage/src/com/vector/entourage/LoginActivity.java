package com.vector.entourage;

import org.json.*;

import com.vector.asynctask.HttpClientJSONPOST;
import util.EntourageUtils;

import com.infinity.asynctask.HttpClientJSONPOST;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import models.EntourageUser;

public class LoginActivity extends Activity implements OnClickListener {
	public final static String EXTRA_MESSAGE = "com.infinity.Entourage.MESSAGE";
	private ProgressDialog pDialog;

	EditText uname, pword;
	Button btnSignIn, btnRegister;
	EntourageUser eu;
	JSONObject jobj = null;
	String name = "";
	String password = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		// get reference to views
		
		SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE); 
		boolean validated = prefs.getBoolean("validated", false); 
		long expiration = prefs.getLong("expiration", 0); 
		String sessionId = prefs.getString("sessionid", ""); 
		
		//temporary user reset 
//		SharedPreferences.Editor editor = prefs.edit(); 
//		
//		editor.remove("sessionid"); 
//		editor.remove("username"); 
//		editor.putBoolean("validated", false); 
//		editor.remove("expiration"); 
//		editor.commit(); 
		
		//if credentials have been validated on the server & the sessionid has not expired :-->
		if(validated && EntourageUtils.isSessionIdValid(expiration) && !sessionId.equals("")){
			Intent intent = new Intent(this, EntourageMainActivity.class);
			startActivity(intent);
		}
		
		
		
		
		uname = (EditText) findViewById(R.id.editTextUserNameToLogin);
		pword = (EditText) findViewById(R.id.editTextPasswordToLogin);
		btnSignIn = (Button) findViewById(R.id.btnSignIn);
		btnRegister = (Button) findViewById(R.id.btnSignUP);
		btnSignIn.setOnClickListener(this);
		btnRegister.setOnClickListener(this);
	}
	
	@Override
	public void onStop(){
		super.onStop();
		//SharedPreferences prefs = getPreferences(PREFS_NAME, 0); 
	}

	@Override
	public void onClick(View view) {
		// Determine which button was pressed.
		switch (view.getId()) {
		case R.id.btnSignIn:
			if (!validate()) {
				Toast.makeText(getBaseContext(), "Invalid Username/Password!",
						Toast.LENGTH_LONG).show();
			} else {
				new HttpAsyncTask().execute();
			}
			break;
		case R.id.btnSignUP:
			startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
			break;
		default:
			break;
		// determine which button was pressed:
		
		if(view.getId() == R.id.btnSignIn){
//			if (!validate()) {
//				Toast.makeText(getBaseContext(), "Invalid Username/Password!",
//						Toast.LENGTH_LONG).show();
//			} else {
				
				JSONObject json = new JSONObject();
				String username = uname.getText().toString(); 
				String password = pword.getText().toString(); 
				
				 try {
					json.put("username", username);
					json.put("password", password);
						
				} catch (JSONException e) {
					e.printStackTrace();
				} 
				
				 String url = "http://10.0.0.6:9000/rest/login";
				 //String url = "http://172.20.10.4:9000/rest/login";
				new HttpLoginTask().execute(url, json.toString());
//			}
		}
		
	if(view.getId() == R.id.btnSignUP){
			Intent i = new Intent(this, SignUpActivity.class);
			startActivity(i);
		}
	}

	private class HttpAsyncTask extends AsyncTask<String, Void, JSONObject> {
		@Override
//	private boolean validate() {
//		if (uname.getText().toString().trim().equals(""))
//			return false;
//		else if (pword.getText().toString().trim().equals(""))
//			return false;
//		else
//			return true;
//	}

	private class HttpLoginTask extends AsyncTask<String, Void, String> {
		
		String url; 
		String jsonStr; 
		JSONObject jsonObj; 
		
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			pDialog = ProgressDialog.show(LoginActivity.this, "",
					"Validating user...", true);
			pDialog = new ProgressDialog(LoginActivity.this);
			pDialog.setMessage("Please wait...");
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			HttpClientJSONPOST post = new HttpClientJSONPOST();
			eu = new EntourageUser();
			eu.setUserName(uname.getText().toString());
			eu.setPassword(pword.getText().toString());
		protected String doInBackground(String... params) {
			//CircularImageView = cv = new CircularImageView();
			
			try {
				jsonObj = new JSONObject(params[1]); 
				url = params[0]; 
				HttpClientJSONPOST post = new HttpClientJSONPOST(url, jsonObj);
				
				jsonStr = post.executePOST(); 
				
				return jsonStr; 
			} catch (JSONException e) {
				e.printStackTrace();
				jobj = post.HttpLoginTask("http://10.0.0.10:9000/rest/login",
						eu);
			} catch (Exception e) {
				pDialog.dismiss();
				e.printStackTrace();
			}
			// Check log for JSON response
			Log.d("Login attempt", jobj.toString());
			return jobj;
			Log.d("Login attempt", jsonStr.toString());
			
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			
			super.onPostExecute(result);
			if (result == null | result == "") return; 
			if (pDialog.isShowing())
				pDialog.dismiss();
			try {
				JSONObject jsonResponseObj = new JSONObject(result); 
				String status = jsonResponseObj.getJSONObject("User").getString("status");
				String sessionId = jsonResponseObj.getJSONObject("User").getString("sessionid"); 
				String success = jsonResponseObj.getJSONObject("User").getString("success"); 
				long expiration = jsonResponseObj.getJSONObject("User").getLong("expiration"); 
				String userName = jsonResponseObj.getJSONObject("User").getString("username"); 
				Log.d("Login status response", status);
				Log.d("Login session id response ", sessionId); 
				Log.d("Login success response ", success); 
				Log.d("Login success response ", String.valueOf(expiration)); 
				if (success.equals("0")) {
					Toast.makeText(getBaseContext(), "Login Successful",
							Toast.LENGTH_LONG).show();
					SharedPreferences prefs = getSharedPreferences("UserPrefs",MODE_PRIVATE); 
					SharedPreferences.Editor editor = prefs.edit(); 
					editor.putBoolean("validated", true);
					editor.putString("sessionid", sessionId); 
					editor.putString("username",userName ); 
					editor.putLong("expiration", expiration); 
					editor.commit(); 
				Intent intent = new Intent(getBaseContext(), EntourageMainActivity.class);
				startActivity(intent);
					
				} else 
					Toast.makeText(getBaseContext(), "Invalid credentials... Please try again", Toast.LENGTH_LONG).show(); 
				
					
				String success = jobj.getJSONObject("User").getString("status");
				Log.d("Login response", success);

				if (success.equalsIgnoreCase("User Found")) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(LoginActivity.this, "Login Success",
									Toast.LENGTH_SHORT).show();
						}
					});

					startActivity(new Intent(LoginActivity.this,
							ImageGridActivity.class));
				} else
					showAlert();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		public void showAlert() {
			LoginActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							LoginActivity.this);
					builder.setTitle("Login Error.");
					builder.setMessage("User Not Found.")
							.setCancelable(false)
							.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog, int id) {
										}
									});
					AlertDialog alert = builder.create();
					alert.show();
				}
			});
		}
	}

	private boolean validate() {
		if (uname.getText().toString().trim().equals(""))
			return false;
		else if (pword.getText().toString().trim().equals(""))
			return false;
		else
			return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}