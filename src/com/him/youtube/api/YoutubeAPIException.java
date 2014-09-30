package com.him.youtube.api;

public class YoutubeAPIException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String mErrorCodeString;
	private final String mReason;
	
	public YoutubeAPIException(String errorCodeString, String reason)
	{
		super();
		mErrorCodeString = errorCodeString;
		mReason = reason;
	}

	public String getErrorCodeString()
	{
		return mErrorCodeString;
	}

	public String getReason()
	{
		return mReason;
	}
	
}
