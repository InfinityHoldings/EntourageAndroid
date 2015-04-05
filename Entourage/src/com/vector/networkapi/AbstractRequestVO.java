package com.vector.networkapi;

import java.util.List;

import org.apache.http.NameValuePair;

public abstract class AbstractRequestVO {
	
	public static final String TAG = AbstractRequestVO.class.getCanonicalName(); 
	
	protected abstract String toJSON(); 
	
	public abstract List<NameValuePair> toHeaderList(); 
	
	

}
