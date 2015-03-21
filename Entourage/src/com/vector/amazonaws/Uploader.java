package com.vector.amazonaws;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.vector.utils.SharedPreferencesCompat;
import com.vector.utils.SharedPreferencesUtils;

public class Uploader {

	private static final long MIN_DEFAULT_PART_SIZE = 5 * 1024 * 1024;

	private static final String TAG = "Entourage";
	private static final String PREFS_NAME = "entourage_prefs";
	private static final String PREFS_UPLOAD_ID = "_uploadId";
	private static final String PREFS_ETAGS = "_etags";
	private static final String PREFS_ETAG_SEP = "~~";

	private AmazonS3Client amazonS3;
	private String s3Bucket;
	private String s3key;
	private File file;

	private SharedPreferences prefs;
	private long partSize = MIN_DEFAULT_PART_SIZE;
	private long bytesUploaded = 0;
	private boolean userInterrupted = false;
	private boolean userAborted = false;
	private UploadProgressListener progressListener;

	public Uploader(Context context, AmazonS3Client amazonS3, String s3Bucket,
			String s3key, File file) {
		this.amazonS3 = amazonS3;
		this.s3key = s3key;
		this.s3Bucket = s3Bucket;
		this.file = file;
		prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

	// Initiate a multi-part file upload to Amazon S3 && return the URL of a
	// successfully uploaded file
	public String start() {
		List<PartETag> partETags = new ArrayList<PartETag>();
		final long contentLength = file.length();
		long filePosition = 0;
		int startPartNumber = 1;

		userInterrupted = false;
		userAborted = false;
		bytesUploaded = 0;

		// Check if we can resume an incomplete upload
		String uploadId = getCachedUploadId();

		if (uploadId != null) {
			// We can resume the download
			Log.i(TAG, "resuming upload for " + uploadId);

			// Get the cached etags
			List<PartETag> cachedEtags = getCachedPartEtags();
			partETags.addAll(cachedEtags);

			// Calculate the start position for resume
			startPartNumber = cachedEtags.size() + 1;
			filePosition = (startPartNumber - 1) * partSize;
			bytesUploaded = filePosition;

			Log.i(TAG, "Resuming at part " + startPartNumber + " position "
					+ filePosition);
		} else {
			// Initiate a new multi-part upload
			Log.i(TAG, "Initiating new upload");

			InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(
					s3Bucket, s3key);
			configureInitiateRequest(initRequest);
			InitiateMultipartUploadResult initResponse = amazonS3
					.initiateMultipartUpload(initRequest);
			uploadId = initResponse.getUploadId();
		}

		final AbortMultipartUploadRequest abortRequest = new AbortMultipartUploadRequest(
				s3Bucket, s3key, uploadId);
		for (int k = startPartNumber; filePosition < contentLength; k++) {
			long thisPartSize = Math.min(partSize,
					(contentLength - filePosition));

			Log.i(TAG, "Starting file part " + k + " with size " + thisPartSize);

			UploadPartRequest uploadRequest = new UploadPartRequest()
					.withBucketName(s3Bucket).withKey(s3key)
					.withUploadId(uploadId).withPartNumber(k)
					.withFileOffset(filePosition).withFile(file)
					.withPartSize(thisPartSize);

			ProgressListener s3progressListener = new ProgressListener() {
				@Override
				public void progressChanged(ProgressEvent progressEvent) {
					// Bail out if user cancelled
					if (userInterrupted) {
						amazonS3.shutdown();
						throw new UploadInterruptedException("User interrupted");
					} else if (userAborted) {
						// Aborted requests cannot be resumed, so clear any
						// cached etags
						clearProgressCache();
						amazonS3.abortMultipartUpload(abortRequest);
						amazonS3.shutdown();
					}

					bytesUploaded += progressEvent.getBytesTransferred();

					// Broadcast progress
					float fpercent = ((bytesUploaded * 100) / contentLength);
					int percent = Math.round(fpercent);
					if (progressListener != null) {
						progressListener.progressChanged(progressEvent,
								bytesUploaded, percent);
					}
				}
			};

			uploadRequest.setGeneralProgressListener(s3progressListener);
			UploadPartResult result = amazonS3.uploadPart(uploadRequest);
			partETags.add(result.getPartETag());

			// Cache the part progress for this upload
			if (k == 1) {
				initProgressCache(uploadId);
			}
			// Store part etag
			cachePartEtag(result);
			filePosition += thisPartSize;
		}

		CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(
				s3Bucket, s3key, uploadId, partETags);

		CompleteMultipartUploadResult result = amazonS3
				.completeMultipartUpload(compRequest);
		bytesUploaded = 0;

		Log.i(TAG, "Upload complete for " + uploadId);

		clearProgressCache();
		return result.getLocation();
	}

	private String getCachedUploadId() {
		return prefs.getString(s3key + PREFS_UPLOAD_ID, null);
	}

	private List<PartETag> getCachedPartEtags() {
		List<PartETag> result = new ArrayList<PartETag>();
		// Get the cached etags
		ArrayList<String> etags = SharedPreferencesUtils.getStringArrayPref(
				prefs, s3key + PREFS_ETAGS);
		for (String etagString : etags) {
			String partNum = etagString.substring(0,
					etagString.indexOf(PREFS_ETAG_SEP));
			String partTag = etagString
					.substring(etagString.indexOf(PREFS_ETAG_SEP) + 2,
							etagString.length());

			PartETag etag = new PartETag(Integer.parseInt(partNum), partTag);
			result.add(etag);
		}
		return result;
	}

	private void cachePartEtag(UploadPartResult result) {
		String serialEtag = result.getPartETag().getPartNumber()
				+ PREFS_ETAG_SEP + result.getPartETag().getETag();
		ArrayList<String> etags = SharedPreferencesUtils.getStringArrayPref(
				prefs, s3key + PREFS_ETAGS);
		etags.add(serialEtag);
		SharedPreferencesUtils.setStringArrayPref(prefs, s3key + PREFS_ETAGS,
				etags);
	}

	private void initProgressCache(String uploadId) {
		// Store uploadID
		Editor edit = prefs.edit().putString(s3key + PREFS_UPLOAD_ID, uploadId);
		SharedPreferencesCompat.apply(edit);
		// Create empty etag array
		ArrayList<String> etags = new ArrayList<String>();
		SharedPreferencesUtils.setStringArrayPref(prefs, s3key + PREFS_ETAGS,
				etags);
	}

	private void clearProgressCache() {
		// Clear the cached uploadId and etags
		Editor edit = prefs.edit();
		edit.remove(s3key + PREFS_UPLOAD_ID);
		edit.remove(s3key + PREFS_ETAGS);
		SharedPreferencesCompat.apply(edit);
	}

	public void interrupt() {
		userInterrupted = true;
	}

	public void abort() {
		userAborted = true;
	}

	protected void configureInitiateRequest(
			InitiateMultipartUploadRequest initRequest) {
		initRequest.setCannedACL(CannedAccessControlList.PublicRead);
	}

	public void setPrefs(SharedPreferences prefs) {
		this.prefs = prefs;
	}

	public long getPartSize() {
		return partSize;
	}

	public void setPartSize(long partSize) {
		if (partSize < MIN_DEFAULT_PART_SIZE) {
			throw new IllegalStateException(
					"Part size is less than S3 minimum of "
							+ MIN_DEFAULT_PART_SIZE);
		} else {
			this.partSize = partSize;
		}
	}

	public void setProgressListener(UploadProgressListener progressListener) {
		this.progressListener = progressListener;
	}

	public interface UploadProgressListener {
		public void progressChanged(ProgressEvent progressEvent,
				long bytesUploaded, int percentUploaded);
	}
}
