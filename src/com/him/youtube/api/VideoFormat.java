package com.him.youtube.api;

/**
 * Format in the "fmt_list" parameter 
 */
public class VideoFormat {

	protected int mId;
	
	public VideoFormat(String pFormatString){
		String lFormatVars[] = pFormatString.split("/");
		mId = Integer.parseInt(lFormatVars[0]);
	}

	public VideoFormat(int pId){
		this.mId = pId;
	}
	
	public int getId(){
		return mId;
	}

	@Override
	public boolean equals(Object pObject) {
		if(!(pObject instanceof VideoFormat)){
			return false;
		}
		return ((VideoFormat)pObject).mId == mId;
	}
}
