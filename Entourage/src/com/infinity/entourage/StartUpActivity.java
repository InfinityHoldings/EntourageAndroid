package com.infinity.entourage;

import com.infinity.entourage.R;
import android.app.*;
import android.content.Intent;
import android.os.*;
import android.view.View;
import android.widget.*;

public class StartUpActivity extends Activity {

	Button btnSignIn, btnSignUp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startup);

		// Get The Reference Of Buttons
		btnSignIn = (Button) findViewById(R.id.buttonSignIN);
		btnSignUp = (Button) findViewById(R.id.buttonSignUP);

		// Set OnClick Listener on SignUp button
		btnSignUp.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				// / Create Intent for SignUpActivity and Start The Activity
				Intent intent = new Intent(StartUpActivity.this, SignUpActivity.class);
				startActivity(intent);
			}
		});
		
		btnSignIn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v1) {
				// TODO Auto-generated method stub
				// / Create Intent for LogIn and Start The Activity
				Intent intent = new Intent(StartUpActivity.this, LoginActivity.class);
				startActivity(intent);
			}
		});
	}
}
