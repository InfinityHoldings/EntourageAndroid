package com.vector.entourage;

import org.json.*;

import com.vector.asynctask.*;
import com.vector.utils.EntourageUtils;
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
	public final static String EXTRA_MESSAGE = "com.vector.Entourage.MESSAGE";
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
		SharedPreferences prefs = getSharedPreferences("UserPrefs",
				MODE_PRIVATE);
		boolean validated = prefs.getBoolean("validated", false);
		long expiration = prefs.getLong("expiration", 0);
		String sessionId = prefs.getString("sessionid", "");

		// temporary user reset
		// SharedPreferences.Editor editor = prefs.edit();
		//
		// editor.remove("sessionid");
		// editor.remove("username");
		// editor.putBoolean("validated", false);
		// editor.remove("expiration");
		// editor.commit();

		// if credentials have been validated on the server & the sessionid has
		// not expired
		if (validated && EntourageUtils.isSessionIdValid(expiration)
				&& !sessionId.equals("")) {
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
		}
	}

	class HttpAsyncTask extends AsyncTask<String, Void, JSONObject> {
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			pDialog = ProgressDialog.show(LoginActivity.this, "",
					"Validating user...", true);
		}

		protected JSONObject doInBackground(String... params) {
			HttpClientJSONPOST post = new HttpClientJSONPOST();
			try {
				eu = new EntourageUser();
				eu.setUserName(uname.getText().toString());
				eu.setPassword(pword.getText().toString());
				jobj = post.HttpLoginTask("http://10.0.0.10:9000/rest/login",
						eu);
			} catch (Exception e) {
				pDialog.dismiss();
				e.printStackTrace();
			}
			// Check log for JSON response
			Log.d("Login attempt", jobj.toString());
			return jobj;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (result == null | result.toString() == "")
				return;
			if (pDialog.isShowing())
				pDialog.dismiss();
			try {
				String status = jobj.getJSONObject("User").getString("status");
				String sessionId = jobj.getJSONObject("User").getString(
						"sessionid");
				String success = jobj.getJSONObject("User")
						.getString("success");
				long expiration = jobj.getJSONObject("User").getLong(
						"expiration");
				String userName = jobj.getJSONObject("User").getString(
						"username");
				Log.d("Login status response", status);
				Log.d("Login session id response ", sessionId);
				Log.d("Login response ", success);
				Log.d("Login response ", String.valueOf(expiration));
				if (success.equalsIgnoreCase("User Found")) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(LoginActivity.this,
									"Login Successful", Toast.LENGTH_SHORT)
									.show();
						}
					});
					SharedPreferences prefs = getSharedPreferences("UserPrefs",
							MODE_PRIVATE);
					SharedPreferences.Editor editor = prefs.edit();
					editor.putBoolean("validated", true);
					editor.putString("sessionid", sessionId);
					editor.putString("username", userName);
					editor.putLong("expiration", expiration);
					editor.commit();

					startActivity(new Intent(LoginActivity.this,
							EntourageMainActivity.class));
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

	@Override
	public void onStop() {
		super.onStop();
		// SharedPreferences prefs = getPreferences(PREFS_NAME, 0);
	}
}