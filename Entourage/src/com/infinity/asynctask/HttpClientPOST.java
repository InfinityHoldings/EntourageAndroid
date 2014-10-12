package com.infinity.asynctask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

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

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.infinity.entourage.LoginActivity;
import com.infinity.entourage.MainActivity;

import controllers.EntourageUser;



public class HttpClientPOST extends AsyncTask<String, Void, String> {
	
	static InputStream is = null;
	static JSONObject jsonObj = null;
	static String json = "", result = "";
	private HttpJSONParser parser; 
	Class<?> myclass; 
	Object classObject;
	
	

	public static final int HTTP_TIMEOUT = 30 * 1000;
	// Single instance of our DefaultHttpClient
	private static DefaultHttpClient mHttpClient;
	
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		//params[0] url 
		//params[1] jsonobject
		//params[2] class type 
		protected String doInBackground(String... params) {
			
			String url = params[0]; 
			json = params[1]; 
			try {
				jsonObj = new JSONObject(json);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
			 
			
			try {
				// Create HttpClient
				HttpClient httpClient = getHttpClient();
				// Make POST request to URL
				HttpPost request = new HttpPost(url);
			
				// Set JSON to StringEntity
				StringEntity se = new StringEntity(json);
				se.setContentEncoding("utf-8");
				// set request Entity
				request.setEntity(se);
				// Set headers to inform server about content type
				request.setHeader("Content-type", "application/json");
				request.setHeader("Accept", "application/json");

				// Execute POST request to URL
				HttpResponse response = httpClient.execute(request);
				StatusLine stats = response.getStatusLine();
				int statusCode = stats.getStatusCode();
				if (statusCode == 200) {
					is = response.getEntity().getContent();
					// Convert InputStream as String
					if (is != null)
						result = convertInputStreamToString(is);
					else
						result = "InputStream conversion failed!";
				} else {
					Log.e("Failure: ", "Failed to login");
				}
			} catch (Exception e) {
				Log.d("InputStream", e.getLocalizedMessage());
			}
			try {
				jsonObj = new JSONObject(result);
			} catch (JSONException e) {
				Log.e("JSON Parser", "Error parsing data " + e.toString());
			}
			
			//return jsonObj;
			
//			//change this to an enum 
//			if(params[2] == "JSON"){
//				parser = new HttpJSONParser(); 
//			}
			
			parser = new HttpJSONParser(); 
			myclass = params[2].getClass(); 
			classObject = parser.parse(jsonObj,  myclass); 
			
			return null;
			
		}// close doInBackground
		
		
	

		private static String convertInputStreamToString(InputStream is)
				throws IOException {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = "", result = "";
			while ((line = br.readLine()) != null)
				result += line;
			is.close();
			return result;
		}
		
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
		}
		
		private static HttpClient getHttpClient() {
			if (mHttpClient == null) {
				mHttpClient = new DefaultHttpClient();
				final HttpParams params = mHttpClient.getParams();
				HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);
				HttpConnectionParams.setSoTimeout(params, HTTP_TIMEOUT);
				ConnManagerParams.setTimeout(params, HTTP_TIMEOUT);
			}
			return mHttpClient;
		}
	}



