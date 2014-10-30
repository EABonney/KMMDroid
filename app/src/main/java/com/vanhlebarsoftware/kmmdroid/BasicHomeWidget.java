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
		intent.putExtra("appWidgetIds", appWidgetIds);
		context.startService(intent);
		Log.d(TAG, "Number of widgetIds: " + appWidgetIds.length);
	}
	
	@Override
	public void onDisabled(Context context)
	{
		// When this get called I need to figure out how to set the preference 
		// homeWidgetSetup = false.
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName thisAppWidget = new ComponentName(context, BasicHomeWidget.class.getName());
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
		
		if(appWidgetIds.length < 1)
		{
			Log.d(TAG, "Removed the last widget!");
			Intent intent = new Intent(context, KMMDService.class);
			intent.putExtra("lastWidgetDeleted", true);
			context.startService(intent);
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) 
	{
		super.onDeleted(context, appWidgetIds);
		Log.d(TAG, "Deleted widgetId: " + appWidgetIds[0]);
		Intent intent = new Intent(context, KMMDService.class);
		intent.putExtra("widgetDeleted", appWidgetIds[0]);
		context.startService(intent);		
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
