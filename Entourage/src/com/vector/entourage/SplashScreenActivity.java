package com.vector.entourage;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

public class SplashScreenActivity extends Activity {

	// Splash screen timer
	private static int SPLASH_TIME_OUT = 2000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acivity_splash);
	}

	@Override
	protected void onStart() {
		super.onStart();
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... voids) {
				long startTime = System.currentTimeMillis();
				// Intents.startSensors(getApplication());
				long sleepTime = sleepTime(startTime);
				sleep(sleepTime);
				return null;
			}

			private long sleepTime(long startTime) {
				long elapsed = System.currentTimeMillis() - startTime;
				return Math.max(SPLASH_TIME_OUT - elapsed, 0);
			}

			private void sleep(long sleepTime) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					Thread.interrupted();
				}
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				startActivity(new Intent(SplashScreenActivity.this,
						CameraActivity.class));
			}
		}.execute();
	}
}