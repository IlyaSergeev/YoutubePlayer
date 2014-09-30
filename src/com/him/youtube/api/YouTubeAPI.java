package com.him.youtube.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class YouTubeAPI {
	
	public static final String URL_GET_VIDEO_INFO = "http://www.youtube.com/get_video_info?&video_id=VIDEO_ID"; 
	
	/**
	 * Calculate the YouTube URL to load the video.  Includes retrieving a token that YouTube
	 * requires to play the video.
	 * 
	 * @param youTubeFmt quality of the video.  17=low, 18=high
	 * @param fallback whether to fallback to lower quality in case the supplied quality is not available
	 * @param youTubeVideoId the id of the video
	 * @return the url string that will retrieve the video
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws UnsupportedEncodingException
	 */
	public static String getYouTubeUrl(int youTubeFmt, boolean fallback, String youTubeVideoId) throws IOException, ClientProtocolException, UnsupportedEncodingException, YoutubeAPIException {

		String uriStr = null;
		HttpClient client = new DefaultHttpClient();
		
		String uri = URL_GET_VIDEO_INFO.replaceFirst("VIDEO_ID", youTubeVideoId);
		HttpGet request = new HttpGet(uri);
		HttpResponse response = client.execute(request);
			
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		response.getEntity().writeTo(output);
		String infoStr = new String(output.toString("UTF-8"));
		
		String[] args = infoStr.split("&");
		Map<String, String> argMap = new HashMap<String, String>();
		for (int i=0; i<args.length; i++) {
			String[] argValArr = args[i].split("=");
			if (argValArr != null && argValArr.length >= 2) {
				argMap.put(argValArr[0], URLDecoder.decode(argValArr[1], "UTF-8"));
			}
		}
		
		if ("fail".equals(argMap.get("status")))
		{
			throw new YoutubeAPIException(argMap.get("errorcode"), argMap.get("reason"));
		}
		
		//Available formats for the video
		String fmtList = URLDecoder.decode(argMap.get("fmt_list"), "UTF-8");
		ArrayList<VideoFormat> formats = new ArrayList<VideoFormat>();
		if (fmtList != null){
			String formatStrs[] = fmtList.split(",");
			for(String formatStr : formatStrs){
				VideoFormat format = new VideoFormat(formatStr);
				formats.add(format);
			}
		}
		
		//Populate the list of streams for the video
		String streamList = argMap.get("url_encoded_fmt_stream_map");
		if (streamList != null){
			String streamStrs[] = streamList.split(",");
			ArrayList<VideoStream> streams = new ArrayList<VideoStream>();
			for(String streamStr : streamStrs){
				VideoStream stream = new VideoStream(streamStr);
				streams.add(stream);
			}	
			
			//Search for the given format in the list of video formats
			// if it is there, select the corresponding stream
			// otherwise if fallback is requested, check for next lower format
			
			VideoFormat searchFormat = new VideoFormat(youTubeFmt);
			while (!formats.contains(searchFormat) && fallback){
				int oldId = searchFormat.getId();
				int newId = YouTubeFMTQuality.getPreviousSupportedFormat(oldId);
				
				if(oldId == newId)
					break;
				
				searchFormat = new VideoFormat(newId);
			}
			
			int index = formats.indexOf(searchFormat);
			if (index >= 0){
				VideoStream searchStream = streams.get(index);
				uriStr = searchStream.getUrl();
			}
		}		

		return uriStr;
	}
}
