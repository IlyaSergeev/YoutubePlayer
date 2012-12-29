package com.him.youtube.api;

/**
 * Youtube format quality of the video. 
 */
public class YouTubeFMTQuality {
	
	public static final int GPP3_LOW = 		13;		//3GPP (MPEG-4 encoded) Low quality 
	public static final int GPP3_MEDIUM = 	17;		//3GPP (MPEG-4 encoded) Medium quality 
	public static final int MP4_NORMAL = 	18;		//MP4  (H.264 encoded) Normal quality
	public static final int MP4_HIGH = 		22;		//MP4  (H.264 encoded) High quality
	public static final int MP4_HIGH1 = 	37;		//MP4  (H.264 encoded) High quality
		
	public static final int[] supported = {
		GPP3_LOW, 
		GPP3_MEDIUM, 
		MP4_NORMAL,
		MP4_HIGH,
		MP4_HIGH1
	};
	
	public static int getPreviousSupportedFormat(int fmtId) {
		int prevFmt = fmtId;
		for(int i = supported.length - 1; i >= 0; i--){
			if(fmtId == supported[i] && i > 0){
				prevFmt = supported[i-1];
			}			
		}
		return prevFmt;
	}
}
