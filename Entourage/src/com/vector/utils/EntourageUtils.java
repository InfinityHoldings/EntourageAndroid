package com.vector.utils;

import java.util.Calendar;

public class EntourageUtils {
	 
	public static boolean isSessionIdValid(long expiration){
		long now = Calendar.getInstance().getTimeInMillis(); 
		if(now > expiration){
			return false; 
		} else {
			return true; 
		}
	}
}
