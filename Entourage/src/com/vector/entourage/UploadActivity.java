package com.vector.entourage;

import com.vector.entourage.R;
import com.vector.entourage.LoginActivity.HttpAsyncTask;
import com.vector.service.UploadService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class UploadActivity extends Activity {
	public static final String ARG_FILE_PATH = "";
	public String filePath = "";

	private ProgressBar progressBar;
	private TextView txtPercentage;
	private ImageView imgPreview;
	private VideoView vidPreview;
	private Button btnUpload;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);
		txtPercentage = (TextView) findViewById(R.id.txtPercentage);
		btnUpload = (Button) findViewById(R.id.btnUpload);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		imgPreview = (ImageView) findViewById(R.id.imgPreview);
		vidPreview = (VideoView) findViewById(R.id.videoPreview);

		// Changing action bar background color
		getActionBar().setBackgroundDrawable(
				new ColorDrawable(Color.parseColor(getResources().getString(
						R.color.action_bar))));

		// Receiving the data from previous activity
		Intent i = getIntent();

		// image or video path that is captured in previous activity
		filePath = i.getStringExtra(ARG_FILE_PATH);

		// boolean flag to identify the media type, image or video
		boolean isImage = i.getBooleanExtra("isImage", true);

		if (filePath != null) {
			// Displaying the image or video on the screen
			previewMedia(isImage);
		} else {
			Toast.makeText(getApplicationContext(),
					"Sorry, file path is missing!", Toast.LENGTH_LONG).show();
		}
		btnUpload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Upload();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		IntentFilter f = new IntentFilter();
		f.addAction(UploadService.UPLOAD_STATE_CHANGED_ACTION);
		registerReceiver(uploadStateReceiver, f);
	}

	@Override
	protected void onStop() {
		unregisterReceiver(uploadStateReceiver);
		super.onStop();
	}

	public void Upload() {
		// initiate the upload
		Intent intent = new Intent(this, UploadService.class);
		intent.putExtra(UploadService.ARG_FILE_PATH, filePath);
		startService(intent);
	}

	// Displaying captured image/video on the screen
	private void previewMedia(boolean isImage) {
		// Checking whether captured media is image or video
		if (isImage) {
			imgPreview.setVisibility(View.VISIBLE);
			vidPreview.setVisibility(View.GONE);
			// bimatp factory
			BitmapFactory.Options options = new BitmapFactory.Options();

			// down sizing image as it throws OutOfMemory Exception for larger
			// images
			options.inSampleSize = 8;

			final Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

			imgPreview.setImageBitmap(bitmap);
		} else {
			imgPreview.setVisibility(View.GONE);
			vidPreview.setVisibility(View.VISIBLE);
			vidPreview.setVideoPath(filePath);
			// start playing
			vidPreview.start();
		}
	}

	private BroadcastReceiver uploadStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle b = intent.getExtras();

			// status.setText(b.getString(UploadService.MSG_EXTRA));
			int percent = b.getInt(UploadService.PERCENT_EXTRA);
			// updating percentage value
			txtPercentage.setText(String.valueOf(percent + "%"));
			progressBar.setIndeterminate(percent < 0);
			progressBar.setProgress(percent);
		}
	};
}
