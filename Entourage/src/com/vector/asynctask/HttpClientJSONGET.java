package com.vector.asynctask;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.HttpStatus;
import ch.boye.httpclientandroidlib.StatusLine;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.config.RequestConfig;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.conn.HttpClientConnectionManager;
import ch.boye.httpclientandroidlib.impl.client.CloseableHttpClient;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import ch.boye.httpclientandroidlib.impl.conn.PoolingHttpClientConnectionManager;

import android.util.Log;

public class HttpClientJSONGET {

	public static final int HTTP_TIMEOUT = 30 * 1000;
	private static final int REGISTRATION_TIMEOUT = 30 * 1000;

	// Single instance of our DefaultHttpClient
	private static RequestConfig requestConfig;
	private static InputStream is = null;
	HttpResponse httpResponse;
	private String content = null;

	public HttpClientJSONGET() {
	}

	private static RequestConfig getRequestConfig() {
		if (requestConfig == null) {
			requestConfig = RequestConfig.custom()
					.setSocketTimeout(REGISTRATION_TIMEOUT)
					.setConnectTimeout(HTTP_TIMEOUT).build();
		}
		return requestConfig;
	}

	public String getJSONImageFromUrl(String url) {
		// Making HTTP request
		try {
			HttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
			// defaultHttpClient
			CloseableHttpClient httpClient = HttpClientBuilder.create()
					.setDefaultRequestConfig(getRequestConfig())
					.setConnectionManager(poolingConnManager).build();
			// make GET request to the given URL
			httpResponse = httpClient.execute(new HttpGet(url));
			// receive response as inputStream
			StatusLine statusLine = httpResponse.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				is = httpResponse.getEntity().getContent();
				// Convert InputStream as String
				if (is != null)
					content = convertInputStreamToString(is);
				else
					content = "InputStream conversion failed!";
			} else {
				Log.e("Failure: ", "Failed to load metadata");
			}
		} catch (Exception e) {
			Log.d("InputStream", e.getLocalizedMessage());
		}
		return content;
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
