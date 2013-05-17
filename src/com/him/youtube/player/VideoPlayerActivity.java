package com.him.youtube.player;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.him.youtube.api.YouTubeAPI;
import com.him.youtube.api.YouTubeFMTQuality;

public class VideoPlayerActivity extends Activity
{	
	public static final String VideoDidCompleteAction = "VideoDidCompleteAction";
	public static final String VideoPositionExtra = "VideoPositionExtra"; 
	
	public static final String VideoIdExtra = "VideoIdExtra";
	public static final String OnCompleteListenerExtra = "OnCompleteListenerExtra";

	private VideoView mVideoView;
	private ProgressBar mProgressBar;
	private String mVideoId;
	private MediaController mediaController;

	/** Background task on which all of the interaction with YouTube is done */
	protected QueryYouTubeTask mQueryYouTubeTask;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		initView();

		// set the flag to keep the screen ON so that the video can play without
		// the screen being turned off
		getWindow().setFlags(
				android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mVideoId = getIntent().getExtras().getString(VideoIdExtra);

		mQueryYouTubeTask = (QueryYouTubeTask) new QueryYouTubeTask()
				.execute(mVideoId);
	}

	protected void initView()
	{
		setContentView(R.layout.player);

		mVideoView = (VideoView) findViewById(R.id.videoView);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

		mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
		{

			@Override
			public void onPrepared(MediaPlayer pMp)
			{
				if (mQueryYouTubeTask != null
						&& mQueryYouTubeTask.isCancelled())
					return;
				VideoPlayerActivity.this.mProgressBar.setVisibility(View.GONE);
				// VideoPlayerActivity.this.mProgressMessage.setVisibility(View.GONE);
			}

		});

		// add listeners for finish of video
		mVideoView.setOnCompletionListener(new OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer pMp)
			{
				if (mQueryYouTubeTask != null
						&& mQueryYouTubeTask.isCancelled())
					return;
				VideoPlayerActivity.this.finish();
				
				Intent broadcast = new Intent(VideoDidCompleteAction);
				broadcast.putExtra(VideoPositionExtra, getIntent().getIntExtra(VideoPositionExtra, -1));
				sendBroadcast(broadcast);
			}
		});

		mediaController = new MediaController(VideoPlayerActivity.this);
		mVideoView.setMediaController(mediaController);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		if (mVideoView != null)
			mVideoView.stopPlayback();

		// clear the flag that keeps the screen ON
		getWindow().clearFlags(
				android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	/**
	 * Task to figure out details by calling out to YouTube GData API.
	 * 
	 */
	private class QueryYouTubeTask extends AsyncTask<String, String, Uri>
	{

		@Override
		protected Uri doInBackground(String... pParams)
		{
			String uriStr = null;
			int youTubeFmtQuality = YouTubeFMTQuality.GPP3_MEDIUM; // 3gpp
																	// medium
																	// quality,
																	// which
																	// should be
																	// fast
																	// enough to
																	// view over
																	// EDGE
																	// connection

			if (isCancelled())
				return null;

			try
			{
				WifiManager lWifiManager = (WifiManager) VideoPlayerActivity.this
						.getSystemService(Context.WIFI_SERVICE);
				TelephonyManager lTelephonyManager = (TelephonyManager) VideoPlayerActivity.this
						.getSystemService(Context.TELEPHONY_SERVICE);

				// if we have a fast connection (wifi or 3g), then we'll get a
				// high quality YouTube video
				if ((lWifiManager.isWifiEnabled()
						&& lWifiManager.getConnectionInfo() != null && lWifiManager
						.getConnectionInfo().getIpAddress() != 0)
						|| ((lTelephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS
								|| lTelephonyManager.getNetworkType() == 9 /* HSUPA */
								|| lTelephonyManager.getNetworkType() == 10 /* HSPA */
								|| lTelephonyManager.getNetworkType() == 8 /* HSDPA */
								|| lTelephonyManager.getNetworkType() == 5 /* EVDO_0 */|| lTelephonyManager
								.getNetworkType() == 6 /* EVDO A */)

						&& lTelephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED))
				{
					youTubeFmtQuality = YouTubeFMTQuality.MP4_NORMAL;
				}

				// request the actual URL of the video
				uriStr = YouTubeAPI.getYouTubeUrl(youTubeFmtQuality, true,
						mVideoId);

				if (isCancelled())
					return null;

			} catch (Exception e)
			{
				Log.e(this.getClass().getSimpleName(),
						"Error occurred while retrieving information from YouTube.",
						e);
			}

			if (uriStr != null)
				return Uri.parse(uriStr);
			else
				return null;
		}

		@Override
		protected void onPostExecute(Uri pResult)
		{
			super.onPostExecute(pResult);

			if (isCancelled())
				return;

			try
			{

				if (pResult == null)
					throw new RuntimeException("Invalid NULL Url.");

				mVideoView.setVideoURI(pResult);
				mProgressBar.setVisibility(View.VISIBLE);
				mVideoView.setKeepScreenOn(true);

				mVideoView.requestFocus();
				mVideoView.start();

			} catch (Exception e)
			{
				Log.e(this.getClass().getSimpleName(), "Error playing video!", e);
			}
		}
	}
}
