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

package com.vector.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import java.io.FileDescriptor;

// A simple subclass of ImageWorker that resizes images from resources given a target width height
public class ImageResizer extends ImageWorker {
	private static final String TAG = "ImageResizer";
	protected int mImageWidth;
	protected int mImageHeight;

	// Initialize providing both width and height.
	public ImageResizer(Context context, int imageWidth, int imageHeight) {
		super(context);
		setImageSize(imageWidth, imageHeight);
	}

	// Initialize providing a single target image size (used for both width and
	// height);
	public ImageResizer(Context context, int imageSize) {
		super(context);
		setImageSize(imageSize);
	}

	// Set the target image width and height.
	public void setImageSize(int width, int height) {
		mImageWidth = width;
		mImageHeight = height;
	}

	// Set the target image size (width and height will be the same).
	public void setImageSize(int size) {
		setImageSize(size, size);
	}

	// The main processing method. This happens in a background task.
	private Bitmap processBitmap(int resId) {
		Log.i(TAG, "processBitmap - " + resId);
		return decodeSampledBitmapFromResource(mResources, resId, mImageWidth,
				mImageHeight, getImageCache());
	}

	@Override
	protected Bitmap processBitmap(Object data) {
		return processBitmap(Integer.parseInt(String.valueOf(data)));
	}

	// Decode and sample down a bitmap from resources to the requested width and
	// height.
	public static Bitmap decodeSampledBitmapFromResource(Resources res,
			int resId, int reqWidth, int reqHeight, ImageCache cache) {
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// If we're running on Honeycomb or newer, try to use inBitmap
		if (ApiUtils.hasHoneycomb()) {
			addInBitmapOptions(options, cache);
		}

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	// Decode and sample down a bitmap from a file to the requested width and
	// height.
	public static Bitmap decodeSampledBitmapFromFile(String filename,
			int reqWidth, int reqHeight, ImageCache cache) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filename, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// If we're running on Honeycomb or newer, try to use inBitmap
		if (ApiUtils.hasHoneycomb()) {
			addInBitmapOptions(options, cache);
		}

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filename, options);
	}

	// Decode and sample down a bitmap from a file input stream to the requested
	// width and height.
	public static Bitmap decodeSampledBitmapFromDescriptor(
			FileDescriptor fileDescriptor, int reqWidth, int reqHeight,
			ImageCache cache) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		options.inScaled = false;
		options.inDither = false;
		options.inScreenDensity = 1;

		// If we're running on Honeycomb or newer, try to use inBitmap
		if (ApiUtils.hasHoneycomb()) {
			addInBitmapOptions(options, cache);
		}
		return BitmapFactory
				.decodeFileDescriptor(fileDescriptor, null, options);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void addInBitmapOptions(BitmapFactory.Options options,
			ImageCache cache) {
		options.inMutable = true;

		if (cache != null) {
			// Try and find a bitmap to use for inBitmap
			Bitmap inBitmap = cache.getBitmapFromReusableSet(options);

			if (inBitmap != null) {
				options.inBitmap = inBitmap;
			}
		}
	}

	// Calculate an inSampleSize for use in a Bitmap options.
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

}
