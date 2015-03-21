package com.vector.async;

public enum Routes {
	
	LOGOUT("http://10.0.0.6:9000/rest/logout"), 
	LOGIN("http://10.0.0.6:9000/rest/login"), 
	UPDATEUSER("http://10.0.0.6:9000/rest/updateUser"); 
	
	private final String route; 
	
	private Routes(String url){
		this.route = url; 
	}
	
	public String getUrl(){
		return this.route; 
	}

}
