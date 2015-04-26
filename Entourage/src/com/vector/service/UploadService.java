package com.vector.service;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import models.S3Amazon;

import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.event.ProgressEvent;
import com.vector.amazonaws.UploadInterruptedException;
import com.vector.amazonaws.Uploader;
import com.vector.amazonaws.Uploader.UploadProgressListener;
import com.vector.asynctask.HttpClientJSONPOST;
import com.vector.entourage.Config;
import com.vector.entourage.Constants;
import com.vector.entourage.R;
import com.vector.entourage.UploadActivity;
import com.vector.utils.CognitoUtil;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class UploadService extends IntentService {

	public static final String ARG_FILE_PATH = "filePath";
	public static final String UPLOAD_STATE_CHANGED_ACTION = "com.vector.entourage.UPLOAD_STATE_CHANGED_ACTION";
	public static final String UPLOAD_CANCELLED_ACTION = "com.vector.entourage.UPLOAD_CANCELLED_ACTION";
	public static final String S3KEY_EXTRA = "s3key";
	public static final String PERCENT_EXTRA = "percent";
	public static final String MSG_EXTRA = "msg";

	private static final int NOTIFY_ID_UPLOAD = 1337;

	private AmazonS3Client s3Client;
	private BroadcastReceiver uploadCancelReceiver;
	private Uploader uploader;
	String filePath;
	String s3ObjectKey = "";

	private NotificationManager nm;

	S3Amazon s3;
	JSONObject jobj = null;

	public UploadService() {
		super("Entourage");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if ((Constants.AWS_ACCOUNT_ID != null)
				&& (Constants.COGNITO_POOL_ID != null)) {
			s3Client = CognitoUtil.getS3Client(getApplicationContext());
			nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			if (!CognitoUtil.doesBucketExist()) {
				Log.i("Creating S3 Bucket: ", Constants.BUCKET_NAME);
				CognitoUtil.createBucket();
			} else
				return;
			Log.i("Using S3 Bucket: ", Constants.BUCKET_NAME);
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		filePath = intent.getStringExtra(ARG_FILE_PATH);
		File fileToUpload = new File(filePath);
		s3ObjectKey = md5(filePath);

		final String msg = "Uploading " + s3ObjectKey + "...";

		// Create a new uploader for this file
		uploader = new Uploader(this, s3Client, Constants.BUCKET_NAME,
				s3ObjectKey + "/" + filePath, fileToUpload);

		// listen for progress updates and broadcast/notify them appropriately
		uploader.setProgressListener(new UploadProgressListener() {
			@Override
			public void progressChanged(ProgressEvent progressEvent,
					long bytesUploaded, int percentUploaded) {

				Notification notification = buildNotification(msg,
						percentUploaded);
				nm.notify(NOTIFY_ID_UPLOAD, notification);
				broadcastState(s3ObjectKey, percentUploaded, msg);
			}
		});

		// broadcast/notify that our upload is starting
		Notification notification = buildNotification(msg, 0);
		nm.notify(NOTIFY_ID_UPLOAD, notification);
		broadcastState(s3ObjectKey, 0, msg);

		try {
			if (s3Client == null) {
				Log.e("S3Client", "Could not save because amazonS3 was null");
				throw new RuntimeException("Could not save");
			} else {
				String s3Location = uploader.start(); // initiate the upload
				broadcastState(s3ObjectKey, -1,
						"File successfully uploaded to " + s3Location);
			}
			new HttpMediaUploadTask().execute();
		} catch (UploadInterruptedException uie) {
			broadcastState(s3ObjectKey, -1, "User interrupted");
		} catch (Exception e) {
			e.printStackTrace();
			broadcastState(s3ObjectKey, -1, "Error: " + e.getMessage());
		}
	}

	public void onStart() {
		IntentFilter f = new IntentFilter();
		f.addAction(UPLOAD_CANCELLED_ACTION);
		uploadCancelReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (uploader != null) {
					uploader.interrupt();
				}
			}
		};
		registerReceiver(uploadCancelReceiver, f);
	}

	public void onStop() {
		if (uploadCancelReceiver != null)
			unregisterReceiver(uploadCancelReceiver);
	}

	private void broadcastState(String s3key, int percent, String msg) {
		Intent intent = new Intent(UPLOAD_STATE_CHANGED_ACTION);
		Bundle b = new Bundle();
		b.putString(S3KEY_EXTRA, s3key);
		b.putInt(PERCENT_EXTRA, percent);
		b.putString(MSG_EXTRA, msg);
		intent.putExtras(b);
		sendBroadcast(intent);
	}

	private Notification buildNotification(String msg, int progress) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				this);
		builder.setWhen(System.currentTimeMillis());
		builder.setTicker(msg);
		builder.setContentTitle(getString(R.string.app_name));
		builder.setContentText(msg);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setOngoing(true);
		builder.setProgress(100, progress, false);

		Intent notificationIntent = new Intent(this, UploadActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		builder.setContentIntent(contentIntent);

		return builder.build();
	}

	private String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++)
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	private class HttpMediaUploadTask extends
			AsyncTask<String, Void, JSONObject> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			HttpClientJSONPOST post = new HttpClientJSONPOST();
			s3 = new S3Amazon();
			s3.setUid(s3ObjectKey.toString());
			s3.setPath(filePath);
			s3.setBucket(Constants.BUCKET_NAME);
			s3.setLocation(s3.getUid() + "/" + s3.getPath());
			try {
				jobj = post.HttpUploadMediaData(Config.FILE_UPLOAD_URL, s3);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// Check log for JSON response
			Log.d("Meta load attempt", jobj.toString());
			return jobj;
		}

		@Override
		protected void onPostExecute(JSONObject json) {
			super.onPostExecute(json);
			try {
				String success = jobj.getJSONObject("Load").getString("status");
				Log.d("Login response", success);

				if (success.equalsIgnoreCase("Media Loaded")) {
					Toast.makeText(UploadService.this, "load Success",
							Toast.LENGTH_SHORT).show();
				} else
					Toast.makeText(UploadService.this, "load unsuccessful",
							Toast.LENGTH_SHORT).show();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}