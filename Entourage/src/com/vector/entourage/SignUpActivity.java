package com.vector.entourage;

import org.json.JSONException;
import org.json.JSONObject;

import com.vector.asynctask.HttpClientJSONPOST;
import com.vector.asynctask.HttpJSONParser;

import com.infinity.asynctask.HttpClientJSONPOST;
import com.infinity.asynctask.HttpJSONParser;
import com.infinity.entourage.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignUpActivity extends Activity {
	private ProgressDialog pDialog;
	EditText editTextUserName, editTextPassword, editTextConfirmPassword,
			editTextCity, editTextState, editTextEmail;
	Button btnCreateAccount;
	HttpJSONParser parser;
	JSONObject jobj = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		// Get references of Views
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
				// check if any of the fields are vacant
				if (userName.equals("") || password.equals("")
						|| confirmPassword.equals("")) {
					Toast.makeText(getApplicationContext(), "Field Vacant",
							Toast.LENGTH_LONG).show();
					return;
				} // check if both password matches
				if (!password.equals(confirmPassword)) {
					Toast.makeText(getApplicationContext(),
							"Password doesn't match", Toast.LENGTH_LONG).show();
					return;
				} else {
					// Save the Data in Database
					JSONObject jobj = new JSONObject();
					try {
						
						jobj.put("username", editTextUserName.getText().toString());
						jobj.put("password", editTextPassword.getText().toString()); 
						jobj.put("city", editTextCity.getText().toString() ); 
						jobj.put("state", editTextState.getText().toString()); 
						jobj.put("email", editTextEmail.getText().toString()); 
//						jobj.put("username", "keon");
//						jobj.put("password", "mypass"); 
//						jobj.put("city", "Atl" ); 
//						jobj.put("state", "GA"); 
//						jobj.put("email", "keon@infinity.com"); 
						
					
					} catch (JSONException e) {
						e.printStackTrace();
					}
					String url = "http://10.0.0.6:9000/rest/signUp";
					Log.d("Url", "Connecting to : " + url);
					Log.d("JSON", jobj.toString());
					// send request
					new HttpSignupTask().execute(url, jobj.toString());
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private class HttpSignupTask extends AsyncTask<String, Void, String> {
		String url;
		String jsonStr;
		JSONObject jsonObj;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			pDialog = ProgressDialog.show(SignUpActivity.this, "",
					"Creating Account...", true);
		}

		@Override
		protected String doInBackground(String... params) {
			url = params[0];
			Log.d("Async url param", url);

			try {
				jsonObj = new JSONObject(params[1]);
				Log.d("JSON object passed to HttpClientPOST",
						jsonObj.toString());
				HttpClientJSONPOST post = new HttpClientJSONPOST(url, jsonObj);
				jsonStr = post.HttpSignUpTask();
				jsonStr = post.executePOST();
				Log.d("HttpClientJSONPost response", jsonStr);
				return jsonStr;
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return jsonStr;
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			 
			super.onPostExecute(result);
			if (pDialog.isShowing())
				pDialog.dismiss();
			try {
				jobj = new JSONObject(result);
				String exists = jobj.getJSONObject("UserExist").getString(
						"status");
			if (result == null | result == "") return;
			pDialog.dismiss(); 

				if (exists.equals("User Already Exist")) {
					showAlert();
				} else
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(SignUpActivity.this,
									"Account Created", Toast.LENGTH_SHORT)
									.show();
						}
					});
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

			// JSONObject job = parser.parseToJSON(result);
			// EntourageUser user = (EntourageUser) parser.parseToClass(jo,
			// EntourageUser.class);
			// editTextUserName.setText(user.getUserName());
			
			try {
				JSONObject response = new JSONObject(result);
				int success = response.getInt("success"); 
				
				if (success == 0){
					Intent home = new Intent(getApplicationContext(), LoginActivity.class); 
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			
			
			//"Username is taken.. be more original! lol"
			Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show(); 
		public void showAlert() {
			SignUpActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							SignUpActivity.this);
					builder.setTitle("Error...");
					builder.setMessage("User Already Exist.")
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

}
