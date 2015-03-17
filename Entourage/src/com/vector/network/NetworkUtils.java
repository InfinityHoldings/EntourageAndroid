package com.vector.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {

	// Checks to see if user is connected to wifi or 3g
	public static boolean isConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = null;
		if (connectivityManager != null) {
			networkInfo = connectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (!networkInfo.isAvailable()) {
				networkInfo = connectivityManager
						.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			}
		}
		return networkInfo == null ? false : networkInfo.isConnected();
	}
}
