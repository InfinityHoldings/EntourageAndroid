package com.vector.asynctask;

import java.io.*;
import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.StatusLine;
import ch.boye.httpclientandroidlib.client.config.RequestConfig;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.conn.HttpClientConnectionManager;
import ch.boye.httpclientandroidlib.entity.StringEntity;
import ch.boye.httpclientandroidlib.entity.mime.HttpMultipartMode;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntityBuilder;
import ch.boye.httpclientandroidlib.entity.mime.content.ByteArrayBody;
import ch.boye.httpclientandroidlib.impl.client.CloseableHttpClient;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import ch.boye.httpclientandroidlib.impl.conn.PoolingHttpClientConnectionManager;
import ch.boye.httpclientandroidlib.protocol.BasicHttpContext;
import ch.boye.httpclientandroidlib.protocol.HttpContext;
import org.json.*;

import models.EntourageUser;
import models.S3Amazon;
import android.graphics.Bitmap;
import android.util.Log;

public class HttpClientJSONPOST {
	JSONObject jsonObj;
	String json = "", result = "", url = "";
	private static InputStream is = null;
	private static final int HTTP_TIMEOUT = 90 * 10000;
	private static final int REGISTRATION_TIMEOUT = 3 * 1000;

	// Single instance of our DefaultHttpClient
	private static RequestConfig requestConfig;

	public HttpClientJSONPOST() {
	}

	public HttpClientJSONPOST(String url, JSONObject jsonObj) {
		this.url = url;
		this.jsonObj = jsonObj;
	}

	private static RequestConfig getRequestConfig() {
		if (requestConfig == null) {
			requestConfig = RequestConfig.custom()
					.setSocketTimeout(REGISTRATION_TIMEOUT)
					.setConnectTimeout(HTTP_TIMEOUT).build();
		}
		return requestConfig;
	}

	public String HttpImageUploadTask(String url, Bitmap bm, String fileName) {
		try {
			HttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
			// DefaultHttpClient
			CloseableHttpClient httpClient = HttpClientBuilder.create()
					.setDefaultRequestConfig(getRequestConfig())
					.setConnectionManager(poolingConnManager).build();
			HttpContext localContext = new BasicHttpContext();
			HttpPost httpPost = new HttpPost(url);

			String boundary = "-------------" + System.currentTimeMillis();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
			byte[] imageBytes = baos.toByteArray();
			Log.d("Image Bytes::", imageBytes.toString());

			httpPost.setHeader("Content-type", "multipart/form-data; boundary="
					+ boundary);
			// StringBody sbOwner = new StringBody(EntourageUser.uid ,
			// ContentType.TEXT_PLAIN);
			// StringBody sbGroup = new StringBody("group",
			// ContentType.TEXT_PLAIN);

			HttpEntity entity = MultipartEntityBuilder
					.create()
					.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
					.setBoundary(boundary)
					.addPart("uploaded",
							new ByteArrayBody(imageBytes, fileName)).build();
			// .addTextBody("photoCaption", caption)
			httpPost.setEntity(entity);
			HttpResponse response = httpClient.execute(httpPost, localContext);
			StatusLine stats = response.getStatusLine();
			Log.d("Http Status::", String.valueOf(stats));
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
		return result;
	}

	public String HttpSignUpTask() {
		try {
			// Create HttpClient
			HttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
			// defaultHttpClient
			CloseableHttpClient httpClient = HttpClientBuilder.create()
					.setDefaultRequestConfig(getRequestConfig())
					.setConnectionManager(poolingConnManager).build();
			// Make POST request to URL
			HttpPost request = new HttpPost(url);
			// Set JSON to StringEntity
			StringEntity se = new StringEntity(jsonObj.toString());
			se.setContentEncoding("utf-8");
			// Set request Entity)
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
		} catch (Exception e) {
			Log.d("InputStream", e.getLocalizedMessage());
		}
		return result;
	}

	public JSONObject HttpUploadMediaData(String url, S3Amazon s3)
			throws Exception {
		try {
			// Create HttpClient
			HttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
			// defaultHttpClient
			CloseableHttpClient httpClient = HttpClientBuilder.create()
					.setDefaultRequestConfig(getRequestConfig())
					.setConnectionManager(poolingConnManager).build();
			// Make POST request to URL
			HttpPost request = new HttpPost(url);
			// Build JSON Object
			JSONObject js = new JSONObject();
			js.accumulate("id", s3.getUid());
			js.accumulate("bucket", s3.getBucket());
			js.accumulate("location", s3.getLocation());
			Log.d("DEBUG : ", js.toString());
			// Convert JSON Object to String
			json = js.toString();
			// Set JSON to StringEntity
			StringEntity se = new StringEntity(json);
			se.setContentEncoding("utf-8");
			// Set request Entity
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
				Log.e("Failure: ", "Failed to load metadata");
			}
		} catch (Exception e) {
			Log.d("InputStream", e.getLocalizedMessage());
			e.printStackTrace();
		}
		try {
			jsonObj = new JSONObject(result);
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}
		return jsonObj;
	}

	public JSONObject HttpLoginTask(String url, EntourageUser eu)
			throws Exception {
		try {
			// Create HttpClient
			HttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
			// defaultHttpClient
			CloseableHttpClient httpClient = HttpClientBuilder.create()
					.setDefaultRequestConfig(getRequestConfig())
					.setConnectionManager(poolingConnManager).build();
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
		BufferedReader br = new BufferedReader(new InputStreamReader(is,
				"UTF-8"));
		String line = "", result = "";
		while ((line = br.readLine()) != null)
			result += line;
		is.close();
		return result;
	}
}
