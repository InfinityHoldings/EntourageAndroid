package com.infinity.asynctask;

import java.io.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import controllers.EntourageUser;
import org.json.*;
import android.util.Log;

public class HttpClientPOST {
	static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";

	public static final int HTTP_TIMEOUT = 30 * 1000;
	// Single instance of our DefaultHttpClient
	private static DefaultHttpClient mHttpClient;

	// Constructor
	public HttpClientPOST() {
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

	public JSONObject HttpLoginTask(String url, EntourageUser eu)
			throws Exception {
		InputStream is = null;
		String result = "", json = "";
		try {
			// Create HttpClient
			HttpClient httpClient = getHttpClient();
			// Make POST request to URL
			HttpPost request = new HttpPost(url);
			// Build JSON Object
			JSONObject js = new JSONObject();
			js.accumulate("username", eu.getUserName());
			js.accumulate("password", eu.getPassword());
			Log.d("DEBUG : ", js.toString());
			// Convert JSON Object to String
			json = js.toString();
			// Set JSON to StringEntity
			StringEntity se = new StringEntity(json);
			se.setContentEncoding("UTF-8");
			// set request Entity
			request.setEntity(se);
			// Set headers to inform server about content type
			request.setHeader("Content-type", "application/json");
			request.setHeader("Accept", "application/json");

			// Execute POST request to URL
			HttpResponse response = httpClient.execute(request);
				is = response.getEntity().getContent();
				// Convert InputStream as String
				if (is != null)
					result = convertInputStreamToString(is);
				else
					result = "InputStream conversion failed!";
		} catch (Exception e) {
			Log.d("InputStream", e.getLocalizedMessage());
		}
		try {
			jObj = new JSONObject(result);
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}
		return jObj;
	}

	private static String convertInputStreamToString(InputStream is)
			throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = "", result = "";
		while ((line = br.readLine()) != null)
			result += line;
		is.close();
		return result;
		
	}
}