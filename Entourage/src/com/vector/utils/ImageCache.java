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
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build.VERSION_CODES;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//This class handles disk and memory caching of bitmaps in conjunction with the ImageWorker class and its subclasses.
public class ImageCache {
	private static final String TAG = "ImageCache";

	final static int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

	// Use 1/8th of the available memory for this memory cache.
	final static int cacheSize = maxMemory / 8;

	// Default memory cache size in kilobytes
	private static final int DEFAULT_MEM_CACHE_SIZE = cacheSize;

	// Default disk cache size in bytes
	private static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB

	// Compression settings when writing images to disk cache
	private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;
	private static final int DEFAULT_COMPRESS_QUALITY = 70;
	private static final int DISK_CACHE_INDEX = 0;

	// Constants to easily toggle various caches
	private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
	private static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
	private static final boolean DEFAULT_INIT_DISK_CACHE_ON_CREATE = false;

	private DiskLruCache mDiskLruCache;
	private LruCache<String, BitmapDrawable> mMemoryCache;
	private ImageCacheParams mCacheParams;
	private final Object mDiskCacheLock = new Object();
	private boolean mDiskCacheStarting = true;

	private Set<SoftReference<Bitmap>> mReusableBitmaps;

	// Create a new ImageCache object using the specified parameters.
	private ImageCache(ImageCacheParams cacheParams) {
		init(cacheParams);
	}

	// Return an ImageCache} instance. A RetainFragment is used to retain the
	// ImageCache object across configuration changes such as a change in device
	// orientation.
	public static ImageCache getInstance(FragmentManager fragmentManager,
			ImageCacheParams cacheParams) {

		// Search for, or create an instance of the non-UI RetainFragment
		Log.i(TAG, "Creating an instance of the non-UI RetainFragment");
		final RetainFragment mRetainFragment = findOrCreateRetainFragment(fragmentManager);

		// See if we already have an ImageCache stored in RetainFragment
		ImageCache imageCache = (ImageCache) mRetainFragment.getObject();

		// No existing ImageCache, create one and store it in RetainFragment
		if (imageCache == null) {
			imageCache = new ImageCache(cacheParams);
			Log.i(TAG, "Creating Image Cache " + imageCache);
			mRetainFragment.setObject(imageCache);
		}
		return imageCache;
	}

	// Initialize the cache, providing all parameters.
	private void init(ImageCacheParams cacheParams) {
		mCacheParams = cacheParams;

		// Set up memory cache
		if (mCacheParams.memoryCacheEnabled) {
			Log.i(TAG, "Memory cache created (size = "
					+ mCacheParams.memCacheSize + ")");

			// If we're running on Honeycomb or newer, create a set of reusable
			// bitmaps that can be populated into the inBitmap field of
			// BitmapFactory.Options
			if (Utils.hasHoneycomb()) {
				mReusableBitmaps = Collections
						.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
			}

			mMemoryCache = new LruCache<String, BitmapDrawable>(
					mCacheParams.memCacheSize) {

				// Notify the removed entry that is no longer being cached.
				@Override
				protected void entryRemoved(boolean evicted, String key,
						BitmapDrawable oldValue, BitmapDrawable newValue) {
					if (RecyclingBitmapDrawable.class.isInstance(oldValue)) {
						// The removed entry is a recycling drawable, so notify
						// it that it has been removed from the memory cache
						((RecyclingBitmapDrawable) oldValue).setIsCached(false);
					} else {
						// The removed entry is a standard BitmapDrawable
						if (Utils.hasHoneycomb()) {
							mReusableBitmaps.add(new SoftReference<Bitmap>(
									oldValue.getBitmap()));
						}
					}
				}

				// Measure item size in kilobytes.
				@Override
				protected int sizeOf(String key, BitmapDrawable value) {
					final int bitmapSize = getBitmapSize(value) / 1024;
					return bitmapSize == 0 ? 1 : bitmapSize;
				}
			};
		}

		// By default the disk cache is not initialized here as it should be
		// initialized on a separate thread due to disk access.
		if (cacheParams.initDiskCacheOnCreate) {
			Log.i(TAG, "Initializing Disk Cache");
			initDiskCache();
		}
	}

	// Initializes the disk cache.
	public void initDiskCache() {
		// Set up disk cache
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
				File diskCacheDir = mCacheParams.diskCacheDir;
				if (mCacheParams.diskCacheEnabled && diskCacheDir != null) {
					if (!diskCacheDir.exists()) {
						diskCacheDir.mkdirs();
					}
					if (getUsableSpace(diskCacheDir) > mCacheParams.diskCacheSize) {
						try {
							mDiskLruCache = DiskLruCache.open(diskCacheDir, 1,
									1, mCacheParams.diskCacheSize);
							Log.i(TAG, "Disk cache initialized " + diskCacheDir);
						} catch (final IOException e) {
							mCacheParams.diskCacheDir = null;
							Log.e(TAG, "initDiskCache - " + e);
						}
					}
				}
			}
			mDiskCacheStarting = false;
			mDiskCacheLock.notifyAll();
		}
	}

	// Adds a bitmap to both memory and disk cache.
	public void addBitmapToCache(String data, BitmapDrawable value) {
		if (data == null || value == null) {
			return;
		}

		// Add to memory cache
		if (mMemoryCache != null) {
			if (RecyclingBitmapDrawable.class.isInstance(value)) {
				// The removed entry is a recycling drawable, so notify it
				// that it has been added into the memory cache
				((RecyclingBitmapDrawable) value).setIsCached(true);
			}
			Log.i(TAG, "Cache image to memory " + data);
			mMemoryCache.put(data, value);
		}

		synchronized (mDiskCacheLock) {
			// Add to disk cache
			Log.i(TAG, "Cache image to Disk");
			if (mDiskLruCache != null) {
				final String key = hashKeyForDisk(data);
				OutputStream out = null;
				try {
					DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
					if (snapshot == null) {
						final DiskLruCache.Editor editor = mDiskLruCache
								.edit(key);
						if (editor != null) {
							out = editor.newOutputStream(DISK_CACHE_INDEX);
							value.getBitmap().compress(
									mCacheParams.compressFormat,
									mCacheParams.compressQuality, out);
							editor.commit();
							out.close();
						}
					} else {
						snapshot.getInputStream(DISK_CACHE_INDEX).close();
					}
				} catch (final IOException e) {
					Log.e(TAG, "addBitmapToCache - " + e);
				} catch (Exception e) {
					Log.e(TAG, "addBitmapToCache - " + e);
				} finally {
					try {
						if (out != null) {
							out.close();
						}
					} catch (IOException e) {
					}
				}
			}
		}
	}

	// Get from memory cache.
	public BitmapDrawable getBitmapFromMemCache(String data) {
		BitmapDrawable memValue = null;

		if (mMemoryCache != null) {
			memValue = mMemoryCache.get(data);
			Log.i(TAG, "Image loaded from memory " + memValue);
		}
		if (memValue != null) {
			Log.i(TAG, "Memory cache hit");
		}
		return memValue;
	}

	// Get from disk cache.
	public Bitmap getBitmapFromDiskCache(String data) {
		final String key = hashKeyForDisk(data);
		Bitmap bitmap = null;

		synchronized (mDiskCacheLock) {
			while (mDiskCacheStarting) {
				try {
					mDiskCacheLock.wait();
				} catch (InterruptedException e) {
				}
			}
			if (mDiskLruCache != null) {
				InputStream inputStream = null;
				try {
					Log.i(TAG, "Loaded image from Disk");
					final DiskLruCache.Snapshot snapshot = mDiskLruCache
							.get(key);
					if (snapshot != null) {
						Log.i(TAG, "Disk cache hit");
						inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
						if (inputStream != null) {
							FileDescriptor fd = ((FileInputStream) inputStream)
									.getFD();

							// Decode bitmap
							bitmap = ImageResizer
									.decodeSampledBitmapFromDescriptor(fd,
											Integer.MAX_VALUE,
											Integer.MAX_VALUE, this);
						}
					}
				} catch (final IOException e) {
					Log.e(TAG, "getBitmapFromDiskCache - " + e);
				} finally {
					try {
						if (inputStream != null) {
							inputStream.close();
						}
					} catch (IOException e) {
					}
				}
			}
			return bitmap;
		}
	}

	protected Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
		Bitmap bitmap = null;

		if (mReusableBitmaps != null && !mReusableBitmaps.isEmpty()) {
			synchronized (mReusableBitmaps) {
				final Iterator<SoftReference<Bitmap>> iterator = mReusableBitmaps
						.iterator();
				Bitmap item;

				while (iterator.hasNext()) {
					Log.i(TAG, "Loaded image from reusable set");
					item = iterator.next().get();

					if (null != item && item.isMutable()) {
						// Check to see it the item can be used for inBitmap
						if (canUseForInBitmap(item, options)) {
							bitmap = item;

							// Remove from reusable set so it can't be used
							// again
							iterator.remove();
							break;
						}
					} else {
						// Remove from the set if the reference has been cleared
						iterator.remove();
					}
				}
			}
		}
		return bitmap;
	}

	// Clears both the memory and disk cache associated with this ImageCache
	// object.
	public void clearCache() {
		if (mMemoryCache != null) {
			mMemoryCache.evictAll();
			Log.i(TAG, "Memory cache cleared");
		}

		synchronized (mDiskCacheLock) {
			mDiskCacheStarting = true;
			if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
				try {
					mDiskLruCache.delete();
					Log.i(TAG, "Disk cache cleared");
				} catch (IOException e) {
					Log.e(TAG, "clearCache - " + e);
				}
				mDiskLruCache = null;
				initDiskCache();
			}
		}
	}

	// Flushes the disk cache associated with this ImageCache object.
	public void flush() {
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache != null) {
				try {
					mDiskLruCache.flush();
					Log.i(TAG, "Disk cache flushed");
				} catch (IOException e) {
					Log.e(TAG, "flush - " + e);
				}
			}
		}
	}

	// Closes the disk cache associated with this ImageCache object.
	public void close() {
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache != null) {
				try {
					if (!mDiskLruCache.isClosed()) {
						mDiskLruCache.close();
						mDiskLruCache = null;
						Log.i(TAG, "Disk cache closed");
					}
				} catch (IOException e) {
					Log.e(TAG, "close - " + e);
				}
			}
		}
	}

	// A holder class that contains cache parameters.
	public static class ImageCacheParams {
		public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;
		public int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
		public File diskCacheDir;
		public CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
		public int compressQuality = DEFAULT_COMPRESS_QUALITY;
		public boolean memoryCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;
		public boolean diskCacheEnabled = DEFAULT_DISK_CACHE_ENABLED;
		public boolean initDiskCacheOnCreate = DEFAULT_INIT_DISK_CACHE_ON_CREATE;

		public ImageCacheParams(Context context, String diskCacheDirectoryName) {
			diskCacheDir = getDiskCacheDir(context, diskCacheDirectoryName);
		}

		// Sets the memory cache size based on a percentage of the max available
		public void setMemCacheSizePercent(float percent) {
			if (percent < 0.01f || percent > 0.8f) {
				throw new IllegalArgumentException(
						"setMemCacheSizePercent - percent must be "
								+ "between 0.01 and 0.8 (inclusive)");
			}
			memCacheSize = Math.round(percent
					* Runtime.getRuntime().maxMemory() / 1024);
		}
	}

	// Checks if if candidate can be used for bitmap.
	@TargetApi(VERSION_CODES.KITKAT)
	private static boolean canUseForInBitmap(Bitmap candidate,
			BitmapFactory.Options targetOptions) {
		if (!Utils.hasKitKat()) {
			// On earlier versions, the dimensions must match exactly and the
			// inSampleSize must be 1
			return candidate.getWidth() == targetOptions.outWidth
					&& candidate.getHeight() == targetOptions.outHeight
					&& targetOptions.inSampleSize == 1;
		}

		// re-use if the byte size of the new bitmap is smaller than the
		// reusable bitmap candidate allocation byte count.
		int width = targetOptions.outWidth / targetOptions.inSampleSize;
		int height = targetOptions.outHeight / targetOptions.inSampleSize;
		int byteCount = width * height
				* getBytesPerPixel(candidate.getConfig());
		return byteCount <= candidate.getAllocationByteCount();
	}

	// Return the byte usage per pixel of a bitmap based on its configuration.
	private static int getBytesPerPixel(Config config) {
		if (config == Config.ARGB_8888) {
			return 4;
		} else if (config == Config.RGB_565) {
			return 2;
		} else if (config == Config.ARGB_4444) {
			return 2;
		} else if (config == Config.ALPHA_8) {
			return 1;
		}
		return 1;
	}

	// Get a usable cache directory (external if available, internal otherwise).
	public static File getDiskCacheDir(Context context, String uniqueName) {
		// Check if media is mounted or storage is built-in,if so use external
		// else internal
		final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState()) || !isExternalStorageRemovable() ? getExternalCacheDir(
				context).getPath()
				: context.getCacheDir().getPath();

		File diskCacheDir = new File(cachePath + File.separator + uniqueName);
		Log.i(TAG, "Disk Cache Directory " + diskCacheDir);
		return diskCacheDir;
	}

	// A hashing method that changes a string (like a URL) into a hash.
	public static String hashKeyForDisk(String key) {
		String cacheKey;
		try {
			final MessageDigest mDigest = MessageDigest.getInstance("MD5");
			mDigest.update(key.getBytes());
			cacheKey = bytesToHexString(mDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			cacheKey = String.valueOf(key.hashCode());
		}
		return cacheKey;
	}

	private static String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	// Get the size in bytes of a bitmap in a BitmapDrawable.
	@TargetApi(VERSION_CODES.KITKAT)
	public static int getBitmapSize(BitmapDrawable value) {
		Bitmap bitmap = value.getBitmap();
		if (Utils.hasKitKat()) {
			return bitmap.getAllocationByteCount();
		}
		if (Utils.hasHoneycombMR1()) {
			return bitmap.getByteCount();
		}
		return bitmap.getRowBytes() * bitmap.getHeight();
	}

	// Check if external storage is built-in or removable.
	@TargetApi(VERSION_CODES.GINGERBREAD)
	public static boolean isExternalStorageRemovable() {
		if (Utils.hasGingerbread()) {
			return Environment.isExternalStorageRemovable();
		}
		return true;
	}

	// Get the external app cache directory.
	@TargetApi(VERSION_CODES.FROYO)
	public static File getExternalCacheDir(Context context) {
		if (Utils.hasFroyo()) {
			return context.getExternalCacheDir();
		}
		// Before Froyo we need to construct the external cache dir ourselves
		final String cacheDir = "/Android/data/" + context.getPackageName()
				+ "/cache/";
		File extCacheDir = new File(Environment.getExternalStorageDirectory()
				.getPath() + cacheDir);
		Log.i(TAG, "External Cache Directory " + extCacheDir);
		return extCacheDir;
	}

	// Check how much usable space is available at a given path.
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public static long getUsableSpace(File path) {
		if (Utils.hasGingerbread()) {
			return path.getUsableSpace();
		}
		final StatFs stats = new StatFs(path.getPath());
		return stats.getBlockSizeLong()
				* stats.getAvailableBlocksLong();
	}

	// Locate an existing instance of this Fragment or if not found, create and
	// add it using FragmentManager.
	private static RetainFragment findOrCreateRetainFragment(FragmentManager fm) {
		// Check to see if we have retained the worker fragment.
		RetainFragment mRetainFragment = (RetainFragment) fm
				.findFragmentByTag(TAG);

		// If not retained (or first time running), we need to create and add
		// it.
		if (mRetainFragment == null) {
			Log.i(TAG, "Creating Retain Fragment for the first time");
			mRetainFragment = new RetainFragment();
			fm.beginTransaction().add(mRetainFragment, TAG)
					.commitAllowingStateLoss();
		}
		return mRetainFragment;
	}

	// A simple non-UI Fragment that stores a single Object and is retained over
	// configuration changes. It will be used to retain the ImageCache object.
	public static class RetainFragment extends Fragment {
		private Object mObject;

		public RetainFragment() {
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			// Make sure this Fragment is retained over a configuration change
			setRetainInstance(true);
		}

		// Store a single object in this Fragment.
		public void setObject(Object object) {
			mObject = object;
		}

		// Get the stored object.
		public Object getObject() {
			return mObject;
		}
	}
}