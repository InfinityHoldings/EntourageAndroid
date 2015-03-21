package com.vector.views;

import java.util.List;

import com.vector.entourage.R;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
	private List<FeedItem> list; 
	
	public FeedAdapter(List<FeedItem> list){
		this.list = list; 
	}
	@Override
	public int getItemCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public void onBindViewHolder(FeedViewHolder fvh, int pos) {
		// TODO Auto-generated method stub
		FeedItem item = list.get(pos); 
		//fvh.icon.setBackgroundResource(R.drawable.owl3); 
		fvh.category.setText(item.category); 
		fvh.content.setText(item.content); 
		fvh.contentType.setText(item.contentType); 

	}

	@Override
	public FeedViewHolder onCreateViewHolder(ViewGroup v, int i) {
		View feedView = LayoutInflater.from(v.getContext()).inflate(R.layout.feed_view, v, false); 
		
		return new FeedViewHolder(feedView);
	}
	
	public static class FeedViewHolder extends RecyclerView.ViewHolder {

		TextView category;
		ImageView icon; 
		TextView contentType;
		TextView content; 
		
		public FeedViewHolder(View v) {
			super(v);
			category = (TextView) v.findViewById(R.id.category); 
			icon = (ImageView) v.findViewById(R.id.icon); 
			contentType = (TextView) v.findViewById(R.id.contentType); 
			content = (TextView) v.findViewById(R.id.content);
		}

	}
}