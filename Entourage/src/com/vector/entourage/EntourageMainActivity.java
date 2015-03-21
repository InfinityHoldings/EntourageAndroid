package com.vector.entourage;

import java.util.ArrayList;
import java.util.List;

import views.FeedAdapter;
import views.FeedItem;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class EntourageMainActivity extends Activity {

	private RecyclerView mRecyclerList;
	private RecyclerView.Adapter mAdapter;
	private RecyclerView.LayoutManager mLayoutManager;
	private CardView mCardView; 
	

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_entourage_main);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(128, 0, 0, 0)));
		
		mCardView = (CardView) findViewById(R.id.city_card); 
		mCardView.setPreventCornerOverlap(false);
		mCardView.setContentPadding(0, 0, 0, 0);
		mRecyclerList = (RecyclerView) findViewById(R.id.feed); 
		mRecyclerList.setHasFixedSize(true); 
		
		LinearLayoutManager llm = new LinearLayoutManager(this); 
		llm.setOrientation(LinearLayoutManager.VERTICAL); 
		
		mRecyclerList.setLayoutManager(llm); 
		List<FeedItem> staticFeed = new ArrayList<FeedItem>(); 
		FeedItem item1 = new FeedItem(); 
		item1.category = "Entourage Member"; 
		item1.content = "Keon is attending ___"; 
		item1.contentType = "Emember update"; 
		
		FeedItem item2 = new FeedItem(); 
		item2.category = "Entourage Member"; 
		item2.content = "Keon is attending ___"; 
		item2.contentType = "Emember update"; 
		
		FeedItem item3 = new FeedItem(); 
		item3.category = "Entourage Member"; 
		item3.content = "Keon is attending ___"; 
		item3.contentType = "Emember update"; 
		
		FeedItem item4 = new FeedItem(); 
		item4.category = "Entourage Member"; 
		item4.content = "Keon is attending ___"; 
		item4.contentType = "Emember update"; 
		
		FeedItem item5 = new FeedItem(); 
		item5.category = "Entourage Member"; 
		item5.content = "Keon is attending ___"; 
		item5.contentType = "Emember update"; 
		
		FeedItem item6 = new FeedItem(); 
		item6.category = "Entourage Member"; 
		item6.content = "Keon is attending ___"; 
		item6.contentType = "Emember update"; 
		
		staticFeed.add(item6); 	
		staticFeed.add(item5); 	
		staticFeed.add(item4); 	
		staticFeed.add(item3); 
		staticFeed.add(item2); 
		staticFeed.add(item1); 
		
		mRecyclerList.setAdapter(new FeedAdapter(staticFeed)); 
		
		//Footer Buttons 
		
		Button homeButton = (Button) findViewById(R.id.home_button); 
		
		final Intent homeIntent = new Intent(getBaseContext(), EntourageMainActivity.class); 
		homeButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(homeIntent); 
			}
		});
		Button eventsButton = (Button) findViewById(R.id.events_button); 
		final Intent eventsIntent = new Intent(getBaseContext(), EventActivity.class); 
		eventsButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(eventsIntent); 
			}
		});
		Button entourageButton = (Button) findViewById(R.id.entourage_button); 
		final Intent entourageIntent = new Intent(getBaseContext(), EntourageMainActivity.class); 
		entourageButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(entourageIntent); 
			}
		});
		
		Button profileButton = (Button) findViewById(R.id.profile_button);
		final Intent profileIntent = new Intent(getBaseContext(), UserActivity.class); 
		profileButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(profileIntent); 
			}
		});
		

	}

	

}
