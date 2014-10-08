package com.infinity.entourage;

import org.json.*;
import com.infinity.asynctask.JSONParser;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import controllers.EntourageUser;

;

public class LoginActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.infinity.Entourage.MESSAGE";
	JSONParser jsonparser = new JSONParser();
	EditText uname, pword;
	Button btnSignIn;
	EntourageUser eu;
	JSONObject jobj = null;
	String name = "";
	String password = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		// get reference to views
		uname = (EditText) findViewById(R.id.editTextUserNameToLogin);
		pword = (EditText) findViewById(R.id.editTextPasswordToLogin);
		btnSignIn = (Button) findViewById(R.id.btnSignIn);
		btnSignIn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				switch (view.getId()) {
				case R.id.btnSignIn:
					if (!validate()) {
						Toast.makeText(getBaseContext(),
								"Invalid Username/Password!", Toast.LENGTH_LONG)
								.show();
					} else {
						new HttpAsyncTask().execute();
					}
				}
			}
		});
	}

	private boolean validate() {
		if (uname.getText().toString().trim().equals(""))
			return false;
		else if (pword.getText().toString().trim().equals(""))
			return false;
		else
			return true;
	}

	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			eu = new EntourageUser();
			eu.setUserName(uname.getText().toString());
			eu.setPassword(pword.getText().toString());
			try {
				jobj = jsonparser.HttpLoginTask(
						"http://192.168.1.3:9000/login", eu);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			// Check log for JSON response
			Log.d("Login attempt", jobj.toString());
			try {
				name = jobj.getString("username");
				password = jobj.getString("password");

			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}// close doInBackground

		@Override
		protected void onPostExecute(String result) {
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			String message = uname.getText().toString();
			intent.putExtra(EXTRA_MESSAGE, "Welcome " + message);
			startActivity(intent);
		}
	}
}
