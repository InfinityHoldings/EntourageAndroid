package com.vector.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.vector.entourage.Constants;
import android.os.AsyncTask;

/* 
 * This class just handles getting the client since we don't need to have more than
 * one per application
 */
public class CognitoUtil {
	private static final String TAG = "S3Client";
	private static AmazonS3Client sS3Client;
	private static CognitoCachingCredentialsProvider sCredProvider;

	public static CognitoCachingCredentialsProvider getCredProvider(
			Context context) {
		if (sCredProvider == null) {
			sCredProvider = new CognitoCachingCredentialsProvider(context,
					Constants.AWS_ACCOUNT_ID, Constants.COGNITO_POOL_ID,
					Constants.COGNITO_ROLE_UNAUTH, Constants.COGNITO_ROLE_AUTH,
					Regions.US_EAST_1);
		}
		return sCredProvider;
	}

	public static String getPrefix(Context context) {
		return getCredProvider(context).getIdentityId() + "/";
	}

	public static AmazonS3Client getS3Client(Context context) {
		if (sS3Client == null) {
			sS3Client = new AmazonS3Client(getCredProvider(context));
			Log.i(TAG, "S3Client initialized");
		}
		return sS3Client;
	}

	public static String getFileName(String path) {
		return path.substring(path.lastIndexOf("/") + 1);
	}

	public static boolean doesBucketExist() {
		new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					return sS3Client.doesBucketExist(Constants.BUCKET_NAME
							.toLowerCase(Locale.US));
				} catch (Exception exc) {
					return false;
				}
			}
		}.execute();
		return false;
	}

	public static void createBucket() {
		if (!doesBucketExist()) {
			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					sS3Client.createBucket(Constants.BUCKET_NAME
							.toLowerCase(Locale.US));
					Log.i(TAG, Constants.BUCKET_NAME + " created");
					return null;
				}

			}.execute();
		}
	}

	public static void deleteBucket() {
		new AsyncTask<Void, Void, Void>(){

			@Override
			protected Void doInBackground(Void... params) {
				String name = Constants.BUCKET_NAME.toLowerCase(Locale.US);
				List<S3ObjectSummary> objData = sS3Client.listObjects(name)
						.getObjectSummaries();
				if (objData.size() > 0) {
					DeleteObjectsRequest emptyBucket = new DeleteObjectsRequest(name);
					List<KeyVersion> keyList = new ArrayList<KeyVersion>();
					for (S3ObjectSummary summary : objData) {
						keyList.add(new KeyVersion(summary.getKey()));
					}
					emptyBucket.withKeys(keyList);
					sS3Client.deleteObjects(emptyBucket);
					Log.i(TAG, Constants.BUCKET_NAME + " deleted");
				}
				sS3Client.deleteBucket(name);
				return null;
			}
		}.execute();
	}
}