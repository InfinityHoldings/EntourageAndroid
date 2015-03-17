package com.vector.asynctask;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class HttpUrlConnectionPOST extends AsyncTask<String, Void, String> {
	// Test Animation

	Context context;

	@Override
	protected String doInBackground(String... urls) {
		// TODO Auto-generated method stub
		InputStream is = null;
		// Only display the first 500 characters of the retrieved
		// web page content.
		int len = 500;

		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("username", urls[1]));
		params.add(new BasicNameValuePair("first_name", urls[2]));

		// ip at d crib
		String login_url = "http://192.168.1.15:9000/rest/loginbones";

		// ip at inno
		// String login_url = "http://192.168.1.151:9000/rest/loginbones";

		Log.d("Networking", "Preparing to connect to : " + urls[0]);
		try {
			URL url = new URL(login_url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(5000 /* milliseconds */);
			conn.setConnectTimeout(5000 /* milliseconds */);
			// conn.setFixedLengthStreamingMode(contentLength)
			conn.setRequestProperty("Content-type",
					"application/json; charset=UTF-8");
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);

			Log.d("URLS[]", "urls0" + urls[0]);
			Log.d("URLS[]", "urls0" + urls[1]);
			Log.d("URLS[]", "urls0" + urls[2]);

			JSONObject loginObj = new JSONObject();
			try {
				loginObj.put("username", urls[1]);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Starts the query
			conn.connect();

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					os, "UTF-8"));

			writer.write(loginObj.toString());
			Log.d("JSON", "loginOBJ" + loginObj.toString() + loginObj);

			os.close();
			int response = conn.getResponseCode();
			Log.d("Networking", "The response is: " + response);
			is = conn.getInputStream();

			// Convert the InputStream into a string
			String contentAsString = readIt(is, len);
			return contentAsString;

			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	protected void onPostExecute() {

		Log.d("HTTP", "onPostExecute");

	}

	public String readIt(InputStream stream, int len) throws IOException,
			UnsupportedEncodingException {
		Reader reader = null;
		reader = new InputStreamReader(stream, "UTF-8");
		char[] buffer = new char[len];
		reader.read(buffer);
		return new String(buffer);
	}

}
