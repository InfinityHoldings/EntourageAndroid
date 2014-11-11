package com.infinity.asynctask;

import java.io.*;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.*;

import controllers.EntourageUser;
import android.util.Log;

public class HttpClientJSONPOST {
	JSONObject jsonObj = null;
	String json = "", result = "", url = "";
	private static InputStream is = null;
	public static final int HTTP_TIMEOUT = 30 * 10000;
	// Single instance of our DefaultHttpClient
	private static DefaultHttpClient mHttpClient;

	public HttpClientJSONPOST() {
	}

	public HttpClientJSONPOST(String url, JSONObject jsonObj) {
		this.url = url;
		this.jsonObj = jsonObj;
	}

	public String executePOST() {
		try {
			// Create HttpClient
			HttpClient httpClient = getHttpClient();
			// Make POST request to URL
			HttpPost request = new HttpPost(url);

			// Set JSON to StringEntity
			StringEntity se = new StringEntity(jsonObj.toString());
			se.setContentEncoding("utf-8");
			// set request Entity)
			request.setEntity(se);
			// Set headers to inform server about content type
			request.setHeader("Content-type", "application/json");
			request.setHeader("Accept", "application/json");
			// Execute POST request to URL
			HttpResponse response = httpClient.execute(request);
			StatusLine stats = response.getStatusLine();
			int statusCode = stats.getStatusCode();
			Log.d("Http Status::", String.valueOf(statusCode));
			// if (statusCode == 200) {
			is = response.getEntity().getContent();
			// Convert InputStream as String
			if (is != null)
				result = convertInputStreamToString(is);
			else
				result = "InputStream conversion failed!";
			// } else {
			// Log.e("Failure: ", "Failed to login");
			// }
		} catch (Exception e) {
			Log.d("InputStream", e.getLocalizedMessage());
			Log.d("E R R O R: ", e.toString()); 
		}
		return result;
	}

	public JSONObject HttpLoginTask(String url, EntourageUser eu)
			throws Exception {
		try {
			// Create HttpClient
			HttpClient httpClient = getHttpClient();
			// Make POST request to URL
			HttpPost request = new HttpPost(url);
			// Build JSON Object
			JSONObject js = new JSONObject();
			js.accumulate("username", eu.getUserName());
			js.accumulate("password", eu.getPassword());
			Log.d("DEBUGGGGG : ", js.toString());
			// Convert JSON Object to String
			json = js.toString();
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
		return jsonObj;
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
