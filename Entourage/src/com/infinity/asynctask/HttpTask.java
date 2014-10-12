package com.infinity.asynctask;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

	public class HttpTask  extends AsyncTask<String, Void, String>{
		//Test Animation
		
		Context context;

		@Override
		protected String doInBackground(String... urls) {
			// TODO Auto-generated method stub
			 InputStream is = null;
			    // Only display the first 500 characters of the retrieved
			    // web page content.
			    int len = 500;
			    
			    String login_url = "http://10.0.0.56:9000/rest/loginbones"; 
			        
			    Log.d("Networking", "Preparing to connect to : " + urls[0]) ; 
			    try {
			        URL url = new URL(login_url);
			        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			        conn.setReadTimeout(5000 /* milliseconds */);
			        conn.setConnectTimeout(5000 /* milliseconds */);
			        conn.setRequestMethod("GET");
			        //conn.setDoOutput(true);
			        // Starts the query
			        conn.connect();
			        int response = conn.getResponseCode();
			        Log.d("Networking", "The response is: " + response);
			        is = conn.getInputStream();
			        
			        

			        // Convert the InputStream into a string
			        String contentAsString = readIt(is, len);
			        return contentAsString;
			        
			    // Makes sure that the InputStream is closed after the app is
			    // finished using it.
			    }catch (IOException e){
			    	e.printStackTrace(); 
			    }finally {
			    	try{
			    		if (is != null) {
				            is.close();
				        } 
			    	}catch (IOException e){
			    		e.printStackTrace(); 
			    	}
			    }
			    return null; 
		} 
		
	   
	   protected void onPostExecute(){
		   
		   Log.d("HTTP", "onPostExecute"); 
		   
		   
	   }
	   
	   public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
		    Reader reader = null;
		    reader = new InputStreamReader(stream, "UTF-8");        
		    char[] buffer = new char[len];
		    reader.read(buffer);
		    return new String(buffer);
		}


	
	


	
	
	    
	}
