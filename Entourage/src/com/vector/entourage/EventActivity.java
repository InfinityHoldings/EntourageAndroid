package com.vector.entourage;

import org.json.JSONObject;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TimePicker;

public class EventActivity extends ActionBarActivity {
	
	JSONObject json; 
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_events); 
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toptoolbar); 
		setSupportActionBar(toolbar); 
		//getActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(128, 0, 0, 0)));
		
		if (findViewById(R.id.eventsContainer) != null){
			
			//if we are being restored the container should already be occupied 
			if (savedInstanceState !=null){
				return; 
			}
			
			EventCrudFragment eventCrudFragment = new EventCrudFragment(); 
			MyEventsFragment myEventsFragment = new MyEventsFragment(); 
			
			//extras for editing an existing event 
			//eventCrudFragment.setArguments(getIntent().getExtras());
			getFragmentManager().beginTransaction().add(R.id.eventsContainer, myEventsFragment).commit(); 
			//getFragmentManager().beginTransaction().add(R.id.eventsContainer, eventCrudFragment).commit(); 
			
		}
		
	}
	
	
	
	
	
	
	public class EventTimePickerFrag extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// TODO Auto-generated method stub
			
		}
		
		

	}
	

}//End of class 
