package com.vanhlebarsoftware.kmmdroid;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class KMMDService extends Service
{
	private static final String TAG = KMMDService.class.getSimpleName();
	static final int DELAY = 60000;
	private boolean runFlag = false;
	private KMMDUpdater kmmdUpdater;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		this.kmmdUpdater = new KMMDUpdater();
		Log.d(TAG, "onCreated");
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		this.runFlag = false;
		this.kmmdUpdater.interrupt();
		this.kmmdUpdater = null;
		Log.d(TAG, "onDestroyed");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		super.onStartCommand(intent, flags, startId);
		this.runFlag = true;
		this.kmmdUpdater.start();
		Log.d(TAG, "onStarted");
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	/**************************************************************************************************************
	 * Thread that will perform the actual updating of the home screen widgets
	 *************************************************************************************************************/
	private class KMMDUpdater extends Thread
	{

		public KMMDUpdater()
		{
			super("KMMDUpdateService-Updater");
		}
		@Override
		public void run()
		{
			KMMDService kmmdService = KMMDService.this;
			
			while(kmmdService.runFlag)
			{
				Log.d(TAG, "KMMDService is running...");
				try
				{
					// Do stuff here
					Log.d(TAG, "KMMDUpdater ran");
					Thread.sleep(DELAY);
				}
				catch(InterruptedException e)
				{
					kmmdService.runFlag = false;
				}
			}
		}
	}
}
