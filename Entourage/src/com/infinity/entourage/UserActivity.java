package com.infinity.entourage;

import controllers.EntourageUser;
import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

public class UserActivity extends Activity {
	
	EditText fullName; 
	EditText userName; 
	EditText city; 
	EditText state; 
	EditText email; 
	EditText password; 
	EditText join; 
	
	EntourageUser user; 
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.user_crud); 
		
		
	}
	
	
}
