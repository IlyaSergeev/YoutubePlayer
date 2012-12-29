package com.him.youtube.api;

import java.util.HashMap;

/**
 * Video value object, initialized by String url encoded params
 */
public class VideoStream {
	
	protected String mUrl;
	
	public VideoStream (String params) {
		
		String[] args = params.split("&");
		HashMap<String, String> argMap = new HashMap<String, String>();
		
		for (int i=0; i<args.length; i++){
			String[] argStrArr = args[i].split("=");
			if (argStrArr != null && argStrArr.length >= 2) {
				argMap.put(argStrArr[0], argStrArr[1]);
			}
		}
		mUrl = argMap.get("url") + "&signature=" + argMap.get("sig");
	}
	
	public String getUrl() {
		return mUrl;
	}
}