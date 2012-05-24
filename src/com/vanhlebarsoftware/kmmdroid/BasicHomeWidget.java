package com.vanhlebarsoftware.kmmdroid;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;

public class BasicHomeWidget extends AppWidgetProvider 
{
	private static final String TAG = BasicHomeWidget.class.getSimpleName();

	
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		Cursor c = null;
		if(context == null)
			Log.d(TAG, "Context is NULL");
		else
		{
			c = context.getContentResolver().query(KMMDProvider.CONTENT_URI, null, null, null, null);
			Log.d(TAG, "Number of Accounts: " + c.getCount());
			Log.d(TAG, "Begin onUpdate");
		}
		
		try
		{
			if(c.moveToFirst())
			{
				String strAccount = c.getString(0);
				String strBalance = c.getString(1);
				
				// Loop through all the instances of this widget
				for(int appWidgetId : appWidgetIds)
				{
					Log.d(TAG, "Updating widget: " + appWidgetId);
					RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.basichomewidget);
					views.setTextViewText(R.id.hrAccountName, strAccount);
					views.setTextViewText(R.id.hrAccountBalance, strBalance);
					Log.d(TAG, "Acount Name: " + strAccount);
					Log.d(TAG, "Account Balance: " + strBalance);
				}
			}
			else
				Log.d(TAG, "No data to update");
		}
		finally
		{
			c.close();
		}
		Log.d(TAG, "onUpdated");
	}
	
/*	@Override
	public void onReceive(Context context, Intent intent)
	{
		//super.onReceive(context, intent);
		
		if(intent.getAction().equals("NEW_STATUS_INTENT"))
		{
			Log.d(TAG, "onReceived detected new status update");
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			this.onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(new ComponentName(context, BasicHomeWidget.class)));
		}
	}
*/
}
