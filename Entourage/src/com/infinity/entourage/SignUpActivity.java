package com.infinity.entourage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import com.infinity.asynctask.HttpClientJSONPOST;
import com.infinity.asynctask.HttpJSONParser;
import com.infinity.entourage.R;

import controllers.EntourageUser;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignUpActivity extends Activity {

	EditText editTextUserName, editTextPassword, editTextConfirmPassword, editTextCity, editTextState, editTextEmail;
	Button btnCreateAccount;
	HttpJSONParser parser; 
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);

		// Get References of Views
		editTextUserName = (EditText) findViewById(R.id.editTextUserName);
		editTextPassword = (EditText) findViewById(R.id.editTextPassword);
		editTextConfirmPassword = (EditText) findViewById(R.id.editTextConfirmPassword);
		editTextCity = (EditText) findViewById(R.id.editTextCity); 
		editTextState = (EditText) findViewById(R.id.editTextState); 
		editTextEmail = (EditText) findViewById(R.id.editTextEmail); 

		btnCreateAccount = (Button) findViewById(R.id.buttonCreateAccount);
		btnCreateAccount.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
			
				String userName = editTextUserName.getText().toString();
				String password = editTextPassword.getText().toString();
				String confirmPassword = editTextConfirmPassword.getText()
						.toString();

				// check if any of the fields are vaccant
//				if (userName.equals("") || password.equals("")
//						|| confirmPassword.equals("")) {
//					Toast.makeText(getApplicationContext(), "Field Vaccant",
//							Toast.LENGTH_LONG).show();
//					return;
//				}
//				// check if both password matches
//				if (!password.equals(confirmPassword)) {
//					Toast.makeText(getApplicationContext(),
//							"Password Does Not Matches", Toast.LENGTH_LONG)
//							.show();
//					return;
//				} else {
					// Save the Data in Database
					Toast.makeText(getApplicationContext(),
							"Account Successfully Created ", Toast.LENGTH_LONG)
							.show();
					JSONObject jo = new JSONObject(); 
					try {
//						jo.put("userName", editTextUserName.getText().toString());
//						jo.put("password", editTextPassword.getText().toString()); 
//						jo.put("city", editTextCity.getText().toString() ); 
//						jo.put("state", editTextState.getText().toString()); 
//						jo.put("email", editTextEmail.getText().toString()); 
						jo.put("userName", "keon");
						jo.put("password", "mypass"); 
						jo.put("city", "Atl" ); 
						jo.put("state", "GA"); 
						jo.put("email", "keon@infinity.com"); 
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					String url = "http://192.168.1.15:9000/rest/signup"; 
					Log.d("Url", "connecting to : " + url); 
					Log.d("JSON", jo.toString()); 
					new SignupTask().execute(url, jo.toString()); 
					
					//send request 
				}

		//	}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private class SignupTask extends AsyncTask<String, Void, String> {

		String url; 
		String jsonStr; 
		JSONObject jsonObj; 
		Object classObject; 
		
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}

			@Override
			//params[0] url 
			//params[1] jsonobject
	
			protected String doInBackground(String... params) {
				
			 url = params[0]; 
			 Log.d("async url param", params[0]); 
			 
			try {
				jsonObj = new JSONObject(params[1]);
				Log.d("json object pass to HttpClientPOST", jsonObj.toString()); 
				HttpClientJSONPOST post = new HttpClientJSONPOST(url, jsonObj);
				
				//test for performance with android tools 
				jsonStr = post.executePOST(); 
				Log.d("HttpClientJSONPost response", jsonStr); 
				return jsonStr; 
				
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
				return null; 
				
			}// close doInBackground
			
			
			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				
				//check status code
				//if successful navigate to profile page 
				
				//JSONObject jo = parser.parseToJSON(result); 
				//EntourageUser user = (EntourageUser) parser.parseToClass(jo, EntourageUser.class);
				//editTextUserName.setText(user.getUserName()); 
				
			}
			
			
		}

}
