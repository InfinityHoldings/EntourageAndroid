package com.infinity.entourage;

import org.json.*;
import com.infinity.asynctask.JSONParser;
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

;

public class LoginActivity extends Activity implements OnClickListener {
	public final static String EXTRA_MESSAGE = "com.infinity.Entourage.MESSAGE";
	// Progress Dialog
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
		uname = (EditText) findViewById(R.id.editTextUserNameToLogin);
		pword = (EditText) findViewById(R.id.editTextPasswordToLogin);
		btnSignIn = (Button) findViewById(R.id.btnSignIn);
		btnRegister = (Button) findViewById(R.id.btnSignUP);
		btnSignIn.setOnClickListener(this);
		btnRegister.setOnClickListener(this);
	}

	@Override
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

	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			pDialog = new ProgressDialog(LoginActivity.this);
			pDialog.setMessage("Logging in...");
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			JSONParser jsonparser = new JSONParser();
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
			super.onPostExecute(result);
			// Dismiss the progress dialog
			if (pDialog.isShowing())
				pDialog.dismiss();
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			String message = uname.getText().toString();
			intent.putExtra(EXTRA_MESSAGE, "Welcome " + message);
			startActivity(intent);
		}
	}
}
