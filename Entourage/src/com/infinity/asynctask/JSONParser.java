package com.infinity.asynctask;

import java.io.*;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import controllers.EntourageUser;
import org.json.*;
import android.util.Log;

public class JSONParser {
	static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";

	public static final int HTTP_TIMEOUT = 30 * 1000;
	// Single instance of our DefaultHttpClient
	private static DefaultHttpClient mHttpClient;

	// Constructor
	public JSONParser() {
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

	public JSONObject getJSONFromUrl(String url) {
		// Making HTTP request
		try {
			// defaultHttpClient
			HttpClient httpClient = getHttpClient();
			// make GET request to the given URL
			HttpResponse httpResponse = httpClient.execute(new HttpGet(url));
			// receive response as inputStream
			is = httpResponse.getEntity().getContent();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			json = sb.toString();
		} catch (Exception e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}
		// try parse the string to a JSON object
		try {
			jObj = new JSONObject(json);
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}
		// return JSON String
		return jObj;
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