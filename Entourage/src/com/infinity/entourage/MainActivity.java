package com.infinity.entourage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;

public class MainActivity extends Activity {
	EditText displayMessage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		displayMessage = (EditText) findViewById(R.id.displayMsg);

		// Get the message from the intent
		Intent intent = getIntent();
		String message = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE);

		// Display Message
		displayMessage.setText(message);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
