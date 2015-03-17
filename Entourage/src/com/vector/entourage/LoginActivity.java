package com.vector.entourage;

import org.json.*;

import com.vector.asynctask.HttpClientJSONPOST;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
		// Get reference to views
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

	private class HttpAsyncTask extends AsyncTask<String, Void, JSONObject> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			pDialog = ProgressDialog.show(LoginActivity.this, "",
					"Validating user...", true);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			HttpClientJSONPOST post = new HttpClientJSONPOST();
			eu = new EntourageUser();
			eu.setUserName(uname.getText().toString());
			eu.setPassword(pword.getText().toString());
			try {
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
		protected void onPostExecute(JSONObject json) {
			super.onPostExecute(json);
			if (pDialog.isShowing())
				pDialog.dismiss();
			try {
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