package com.vector.entourage;



import com.vector.entourage.R;

import android.app.Activity;
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
		displayMessage.setText("Main Page");
	}

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
