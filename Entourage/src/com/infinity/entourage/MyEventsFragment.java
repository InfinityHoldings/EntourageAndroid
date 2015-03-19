package com.infinity.entourage;

import org.json.JSONObject;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.*; 
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class MyEventsFragment extends Fragment{
	View mef; 
	JSONObject jsonRequestObj; 
	//shit you have rsvp'd for, shit you have created, shit you have 'maybe'd' , invitations 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		jsonRequestObj = new JSONObject();
//		Toolbar toolbar = (Toolbar) mef.findViewById(R.id.toptoolbar); 
//		setActionBar(toolbar); 
	
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState); 
		if(mef == null){
		mef = inflater.inflate(R.layout.fragment_myevents, container, false); 
		}else{
			container.removeAllViews();
		}
		
		SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs",getActivity().MODE_PRIVATE); 
		return mef;
		
	}

}
