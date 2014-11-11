package com.infinity.entourage;

import org.json.*;

import com.infinity.asynctask.HttpClientJSONPOST;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import controllers.EntourageUser;

public class LoginActivity extends Activity implements OnClickListener {
	public final static String EXTRA_MESSAGE = "com.infinity.Entourage.MESSAGE";
	private ProgressDialog pDialog;

	EditText uname, pword;
	Button btnSignIn, btnRegister;
	EntourageUser eu;
	JSONObject jobj = null;
	String name = "";
	String password = "";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		// get reference to views
		uname = (EditText) findViewById(R.id.editTextUserNameToLogin);
		pword = (EditText) findViewById(R.id.editTextPasswordToLogin);
		btnSignIn = (Button) findViewById(R.id.btnSignIn);
		btnRegister = (Button) findViewById(R.id.btnSignUP);
		btnSignIn.setOnClickListener(this);
		btnRegister.setOnClickListener(this);
	}

	public void onClick(View view) {
		// determine which button was pressed:
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
			Intent i = new Intent(this, SignUpActivity.class);
			startActivity(i);
			break;
		default:
			break;
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

	private class HttpAsyncTask extends AsyncTask<String, Void, JSONObject> {
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			pDialog = new ProgressDialog(LoginActivity.this);
			pDialog.setMessage("Please wait...");
			pDialog.setCancelable(false);
			pDialog.show();
		}

		protected JSONObject doInBackground(String... params) {
			//CircularImageView = cv = new CircularImageView();
			
			HttpClientJSONPOST post = new HttpClientJSONPOST();
			eu = new EntourageUser();
			eu.setUserName(uname.getText().toString());
			eu.setPassword(pword.getText().toString());
			try {
				jobj = post.HttpLoginTask(
						"http://10.0.0.10:9000/rest/login", eu);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			// Check log for JSON response
			Log.d("Login attempt", jobj.toString());
			return jobj;
		}

		@Override
		protected void onPostExecute(JSONObject json) {
			super.onPostExecute(json);
			if (pDialog.isShowing())
				pDialog.dismiss();
			try {
				//String success = jobj.getJSONObject("User").getString("status");
				String success = jobj.getJSONObject("User").getString("status");
				Log.d("Login response", success);

				if (success.equals("1")) {
					Intent intent = new Intent(LoginActivity.this,
							MainActivity.class);
					startActivity(intent);
				} else
					Toast.makeText(getBaseContext(), "Invalid Username/Password!",
							Toast.LENGTH_LONG).show();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
