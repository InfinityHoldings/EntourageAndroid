/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vector.entourage;

import java.util.ArrayList;
import models.S3Amazon;
import org.json.JSONArray;
import org.json.JSONException;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.vector.asynctask.HttpClientJSONGET;
import com.vector.utils.ImageCache;
import com.vector.utils.ImageFetcher;
import com.vector.utils.ApiUtils;
import com.vector.widgets.PullToRefreshGridView;

/**
 * The main fragment that powers the ImageGridActivity screen. Fairly straight
 * forward GridView implementation with the key addition being the ImageWorker
 * class w/ImageCache to load children asynchronously, keeping the UI nice and
 * smooth and caching thumbnails for quick retrieval. The cache is retained over
 * configuration changes like orientation change so the images are populated
 * quickly if, for example, the user rotates the device.
 */
public class ImageGridFragment extends Fragment implements
		AdapterView.OnItemClickListener {
	private static final String TAG = ImageGridFragment.class.getSimpleName();
	private static final String IMAGE_CACHE_DIR = "thumbs";
	private int mImageThumbSize;
	private int mImageThumbSpacing;
	private ImageAdapter mAdapter;
	private ImageFetcher mImageFetcher;
	private PullToRefreshGridView mPullRefreshGridView;
	private ProgressDialog pDialog;

	ArrayList<String> imageList;
	private String result = null;
	private boolean error = false;
	GridView mGridView;
	View v;
	String url = "http://10.0.0.10:9000/rest/downloadImages";

	public ImageGridFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mImageThumbSize = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_spacing);

		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(
				getActivity(), IMAGE_CACHE_DIR);
		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
													// app memory

		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
		mImageFetcher.setLoadingImage(R.drawable.empty_photo);
		mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(),
				cacheParams);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.image_grid_fragment, container, false);
		// Retrieve the GridView
		mPullRefreshGridView = (PullToRefreshGridView) v
				.findViewById(R.id.pull_refresh_grid);
		// Get refreshable view from PullToRefreshGridView class.
		mGridView = mPullRefreshGridView.getRefreshableView();

		imageList = new ArrayList<String>();
		mAdapter = new ImageAdapter(getActivity());

		// create an ArrayAdaptar from the String Array
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(this);
		mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView,
					int scrollState) {
				// Pause fetcher to ensure smoother scrolling when flinging
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
					// Before Honeycomb pause image loading on scroll to help
					// with performance
					if (!ApiUtils.hasHoneycomb()) {
						mImageFetcher.setPauseWork(true);
					}
				} else {
					mImageFetcher.setPauseWork(false);
				}
			}

			@Override
			public void onScroll(AbsListView absListView, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				/*
				 * int lastInScreen = firstVisibleItem + visibleItemCount; if
				 * ((lastInScreen == totalItemCount)) { getImageUrl(url); }
				 */
			}
		});

		// This listener is used to get the final width of the GridView and then
		// calculate the
		// number of columns and the width of each column. The width of each
		// column is variable
		// as the GridView has stretchMode=columnWidth. The column width is used
		// to set the height
		// of each view so we get nice square thumbnails.
		mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@SuppressWarnings("deprecation")
					@TargetApi(VERSION_CODES.JELLY_BEAN)
					@Override
					public void onGlobalLayout() {
						if (mAdapter.getNumColumns() == 0) {
							final int numColumns = (int) Math.floor(mGridView
									.getWidth()
									/ (mImageThumbSize + mImageThumbSpacing));
							if (numColumns > 0) {
								final int columnWidth = (mGridView.getWidth() / numColumns)
										- mImageThumbSpacing;
								mAdapter.setNumColumns(numColumns);
								mAdapter.setItemHeight(columnWidth);
								if (BuildConfig.DEBUG) {
									Log.d(TAG,
											"onCreateView - numColumns set to "
													+ numColumns);
								}
								if (ApiUtils.hasJellyBean()) {
									mGridView.getViewTreeObserver()
											.removeOnGlobalLayoutListener(this);
								} else {
									mGridView.getViewTreeObserver()
											.removeGlobalOnLayoutListener(this);
								}
							}
						}
					}
				});
		getImageUrl(url);
		return v;
	}

	public void getImageUrl(String url) {
		Log.v("ImageGridFragment : getImages() ", url);
		new downloadImageTask().execute(url);
	}

	private class downloadImageTask extends AsyncTask<String, Void, String> {
		protected void onPreExecute() {
			super.onPreExecute(); // Showing progress
			pDialog = ProgressDialog.show(getActivity(), "Please wait...", "",
					true);
		}

		protected String doInBackground(String... url) {
			HttpClientJSONGET httpGet = new HttpClientJSONGET();
			try {
				result = httpGet.getJSONImageFromUrl(url[0]);
			} catch (Exception e) {
				pDialog.dismiss();
				error = true;
				e.printStackTrace();
			}
			return result;
		}

		protected void onCancelled() {
			pDialog.dismiss();
			Toast toast = Toast.makeText(getActivity(),
					"Error Connecting to Server", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP, 25, 400);
			toast.show();
		}

		protected void onPostExecute(String result) {
			pDialog.dismiss();
			Toast toast;
			if (error) {
				toast = Toast
						.makeText(getActivity(), result, Toast.LENGTH_LONG);
				toast.setGravity(Gravity.TOP, 25, 400);
				toast.show();
			} else {
				try {
					displayImageList(result);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void displayImageList(String response) throws JSONException {
		JSONArray imageArr = new JSONArray(response);
		try {
			Gson gson = new Gson();
			if (imageArr.length() == 0) {
				mGridView = (GridView) v.findViewById(R.id.pull_refresh_grid);
			} else {
				for (int i = 0; i < imageArr.length(); i++) {
					// get the image information from JSON Array
					String imageInfo = imageArr.get(i).toString();
					// create java object from the JSON object
					S3Amazon amazon = gson.fromJson(imageInfo, S3Amazon.class);
					// add to image array list
					imageList.add(amazon.location.toString());
				}
				mAdapter.notifyDataSetChanged();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mImageFetcher.setExitTasksEarly(false);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();
		mImageFetcher.setPauseWork(false);
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mImageFetcher.closeCache();
	}

	@TargetApi(VERSION_CODES.JELLY_BEAN)
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		final Intent i = new Intent(getActivity(), ImageDetailActivity.class);
		i.putExtra(ImageDetailActivity.EXTRA_IMAGE, (int) id);
		i.putStringArrayListExtra("Image_list", imageList);
		if (ApiUtils.hasJellyBean()) {
			// makeThumbnailScaleUpAnimation() looks kind of ugly here as the
			// loading spinner may
			// show plus the thumbnail image in GridView is cropped. so using
			// makeScaleUpAnimation() instead.
			ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v,
					0, 0, v.getWidth(), v.getHeight());
			getActivity().startActivity(i, options.toBundle());
		} else {
			startActivity(i);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main_activity_bar, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.clear_cache:
			mImageFetcher.clearCache();
			Toast.makeText(getActivity(), R.string.clear_cache_complete_toast,
					Toast.LENGTH_SHORT).show();
			return true;
		case R.id.action_add_image:
			 Intent intent = new Intent(getActivity(), MediaUploadActivity.class);
             startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * The main adapter that backs the GridView. This is fairly standard except
	 * the number of columns in the GridView is used to create a fake top row of
	 * empty views as we use a transparent ActionBar and don't want the real top
	 * row of images to start off covered by it.
	 */
	public class ImageAdapter extends BaseAdapter {

		private final Context mContext;
		private int mItemHeight = 0;
		private int mNumColumns = 0;
		private int mActionBarHeight = 0;
		private GridView.LayoutParams mImageViewLayoutParams;

		public ImageAdapter(Context context) {
			super();
			mContext = context;
			mImageViewLayoutParams = new GridView.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			// Calculate ActionBar height
			TypedValue tv = new TypedValue();
			if (context.getTheme().resolveAttribute(
					android.R.attr.actionBarSize, tv, true)) {
				mActionBarHeight = TypedValue.complexToDimensionPixelSize(
						tv.data, context.getResources().getDisplayMetrics());
			}
		}

		public int getArrayCount() {
			return imageList.size();
		}

		@Override
		public int getCount() {
			// If columns have yet to be determined, return no items
			if (getNumColumns() == 0) {
				return 0;
			}

			// Size + number of columns for top empty row
			return imageList.size() + mNumColumns;
		}

		@Override
		public Object getItem(int position) {
			return position < mNumColumns ? null : imageList.get(position
					- mNumColumns);
		}

		@Override
		public long getItemId(int position) {
			return position < mNumColumns ? 0 : position - mNumColumns;
		}

		@Override
		public int getViewTypeCount() {
			// Two types of views, the normal ImageView and the top row of empty
			// views
			return 2;
		}

		@Override
		public int getItemViewType(int position) {
			return (position < mNumColumns) ? 1 : 0;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup container) {
			// First check if this is the top row
			if (position < mNumColumns) {
				if (convertView == null) {
					convertView = new View(mContext);
				}
				// Set empty view with height of ActionBar
				convertView.setLayoutParams(new AbsListView.LayoutParams(
						LayoutParams.MATCH_PARENT, mActionBarHeight));
				return convertView;
			}

			// Now handle the main ImageView thumbnails
			ImageView imageView;
			if (convertView == null) { // if it's not recycled, instantiate and
										// initialize
				imageView = new RecyclingImageView(mContext);
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setLayoutParams(mImageViewLayoutParams);
			} else { // Otherwise re-use the converted view
				imageView = (ImageView) convertView;
			}

			// Check the height matches our calculated column width
			if (imageView.getLayoutParams().height != mItemHeight) {
				imageView.setLayoutParams(mImageViewLayoutParams);
			}

			// Finally load the image asynchronously into the ImageView, this
			// also takes care of
			// setting a placeholder image while the background thread runs
			mImageFetcher.loadImage(imageList.get(position - mNumColumns),
					imageView);
			return imageView;
		}

		/**
		 * Sets the item height. Useful for when we know the column width so the
		 * height can be set to match.
		 * 
		 * @param height
		 */
		public void setItemHeight(int height) {
			if (height == mItemHeight) {
				return;
			}
			mItemHeight = height;
			mImageViewLayoutParams = new GridView.LayoutParams(
					LayoutParams.MATCH_PARENT, mItemHeight);
			mImageFetcher.setImageSize(height);
			notifyDataSetChanged();
		}

		public void setNumColumns(int numColumns) {
			mNumColumns = numColumns;
		}

		public int getNumColumns() {
			return mNumColumns;
		}
	}
}