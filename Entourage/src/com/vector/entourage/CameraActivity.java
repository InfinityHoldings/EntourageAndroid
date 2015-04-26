package com.vector.entourage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

@SuppressLint("SimpleDateFormat")
@SuppressWarnings("deprecation")
public class CameraActivity extends Activity {
	// LogCat tag
	private static final String TAG = CameraActivity.class.getSimpleName();

	ImageView image;
	Activity context;
	CameraPreview preview;
	Camera camera;
	Button exitButton;
	ImageView fotoButton;
	LinearLayout progressLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);

		context = this;

		fotoButton = (ImageView) findViewById(R.id.imageView_foto);
		exitButton = (Button) findViewById(R.id.button_exit);
		image = (ImageView) findViewById(R.id.imageView_photo);
		progressLayout = (LinearLayout) findViewById(R.id.progress_layout);

		preview = new CameraPreview(this,
				(SurfaceView) findViewById(R.id.CameraFragment));
		FrameLayout frame = (FrameLayout) findViewById(R.id.preview);
		frame.addView(preview);
		preview.setKeepScreenOn(true);
		fotoButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					takeFocusedPicture();
				} catch (Exception e) {

				}
				exitButton.setClickable(false);
				fotoButton.setClickable(false);
				progressLayout.setVisibility(View.VISIBLE);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (camera == null) {
			camera = Camera.open();
			camera.startPreview();
			camera.setErrorCallback(new ErrorCallback() {
				public void onError(int error, Camera mcamera) {

					camera.release();
					camera = Camera.open();
					Log.d("Camera died", "error camera");

				}
			});
		}
		if (camera != null) {
			if (Build.VERSION.SDK_INT >= 14)
				setCameraDisplayOrientation(context,
						CameraInfo.CAMERA_FACING_BACK, camera);
			preview.setCamera(camera);
		}
	}

	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri() {
		return Uri.fromFile(getOutputMediaFile());
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile() {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				Config.IMAGE_DIRECTORY_NAME);
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d(TAG, "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		mediaFile = new File(mediaStorageDir.getPath() + File.separator
				+ "IMG_" + timeStamp + ".jpg");

		return mediaFile;
	}

	private void setCameraDisplayOrientation(Activity activity, int cameraId,
			android.hardware.Camera camera) {
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}

	Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback() {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {

			try {
				camera.takePicture(mShutterCallback, null, mPicture);
			} catch (Exception e) {

			}

		}
	};

	Camera.ShutterCallback mShutterCallback = new ShutterCallback() {

		@Override
		public void onShutter() {

		}
	};

	public void takeFocusedPicture() {
		camera.autoFocus(mAutoFocusCallback);

	}

	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			// Log.d(TAG, "onPictureTaken - raw");
		}
	};

	PictureCallback mPicture = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {

			File pictureFile = getOutputMediaFile();
			if (pictureFile == null) {
				Log.d(TAG,
						"Error creating media file, check storage permissions: ");
				return;
			}

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.d(TAG, "Error accessing file: " + e.getMessage());
			}

			Bitmap realImage;
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 5;

			options.inPurgeable = true; // Tell to gc that whether it needs free
										// memory, the Bitmap can be cleared

			options.inInputShareable = true; // Which kind of reference will be
												// used to recover the Bitmap
												// data after being clear, when
												// it will be used in the future

			realImage = BitmapFactory.decodeByteArray(data, 0, data.length,
					options);
			ExifInterface exif = null;
			try {
				exif = new ExifInterface(pictureFile.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				Log.d("EXIF value",
						exif.getAttribute(ExifInterface.TAG_ORIENTATION));
				if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
						.equalsIgnoreCase("1")) {
					realImage = rotate(realImage, 90);
				} else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
						.equalsIgnoreCase("8")) {
					realImage = rotate(realImage, 90);
				} else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
						.equalsIgnoreCase("3")) {
					realImage = rotate(realImage, 90);
				} else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
						.equalsIgnoreCase("0")) {
					realImage = rotate(realImage, 90);
				}
			} catch (Exception e) {

			}

			image.setImageBitmap(realImage);

			fotoButton.setClickable(true);
			camera.startPreview();
			progressLayout.setVisibility(View.GONE);
			exitButton.setClickable(true);

		}
	};

	public static Bitmap rotate(Bitmap source, float angle) {
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(),
				source.getHeight(), matrix, false);
	}

}