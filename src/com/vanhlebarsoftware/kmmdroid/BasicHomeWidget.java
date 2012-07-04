package com.vanhlebarsoftware.kmmdroid;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BasicHomeWidget extends AppWidgetProvider 
{
	private static final String TAG = BasicHomeWidget.class.getSimpleName();
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		// Ensure the widget gets updated when first added to the home screen.
		Intent intent = new Intent(context, KMMDService.class);
		context.startService(intent);
		Log.d(TAG, "onUpdated");
	}
	
	@Override
	public void onDisabled(Context context)
	{
	}
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		super.onReceive(context, intent);
		
		if(intent.getAction().equals(KMMDService.DATA_CHANGED))
		{
			Log.d(TAG, "onReceived detected the database has changed.");
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			this.onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(new ComponentName(context, BasicHomeWidget.class)));			
		}
	}

}
