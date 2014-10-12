package com.infinity.asynctask;

import java.io.InputStream;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.infinity.entourage.LoginActivity;
import com.infinity.entourage.MainActivity;

import controllers.EntourageUser;

public class HttpClientGET extends AsyncTask<String, Void, String> {
	
	static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";

	public static final int HTTP_TIMEOUT = 30 * 1000;
	// Single instance of our DefaultHttpClient
	private static DefaultHttpClient mHttpClient;
	
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
						"http://10.0.0.6:9000/login", eu);
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
