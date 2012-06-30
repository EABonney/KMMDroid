package com.vanhlebarsoftware.kmmdroid;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

public class BasicHomeWidget extends AppWidgetProvider 
{
	private static final String TAG = BasicHomeWidget.class.getSimpleName();

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		// Need to get the prefs for our application.
		Context cont = null;
		try 
		{
			cont = context.createPackageContext("com.vanhlebarsoftware.kmmdroid", Context.CONTEXT_IGNORE_SECURITY);
		} 
		catch (NameNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SharedPreferences prefs = cont.getSharedPreferences("com.vanhlebarsoftware.kmmdroid_preferences", Context.MODE_WORLD_READABLE);
		String accountUsed = prefs.getString("accountUsed", "");
		Log.d(TAG, "accountUsed from Prefs: " + accountUsed);
		
		Cursor c = null;
		Log.d(TAG, "Attempting to query the dabase...");
		c = context.getContentResolver().query(KMMDProvider.CONTENT_SCHEDULE_URI, null, null, null, null);
		Log.d(TAG, "Returned from querying the database...");
		Log.d(TAG, "Number of schedules: " + c.getCount());
		
		try
		{
			if(c.moveToFirst())
			{
				// Loop through all the instances of this widget
				for(int appWidgetId : appWidgetIds)
				{
					Log.d(TAG, "Updating widget: " + appWidgetId);
					RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.basichomewidget);
					
					// Loop thru the returned cursor and populate the widgets rows.
					int i = 1;
					String strDate = null;
					String strAccount = null;
					String strBalance = null;
					while(!c.isAfterLast())
					{
						strDate = c.getString(5);
						strAccount = c.getString(1);
						strBalance = Transaction.convertToDollars(Transaction.convertToPennies(c.getString(9)));
						switch(i)
						{
						case 1:
							views.setTextViewText(R.id.scheduleDate1, strDate);
							views.setTextViewText(R.id.scheduleName1, strAccount);
							views.setTextViewText(R.id.scheduleAmount1, strBalance);
							break;
						case 2:
							views.setTextViewText(R.id.scheduleDate2, strDate);
							views.setTextViewText(R.id.scheduleName2, strAccount);
							views.setTextViewText(R.id.scheduleAmount2, strBalance);
							break;
						case 3:
							views.setTextViewText(R.id.scheduleDate3, strDate);
							views.setTextViewText(R.id.scheduleName3, strAccount);
							views.setTextViewText(R.id.scheduleAmount3, strBalance);
							break;
						case 4:
							views.setTextViewText(R.id.scheduleDate4, strDate);
							views.setTextViewText(R.id.scheduleName4, strAccount);
							views.setTextViewText(R.id.scheduleAmount4, strBalance);
							break;
						case 5:
							views.setTextViewText(R.id.scheduleDate5, strDate);
							views.setTextViewText(R.id.scheduleName5, strAccount);
							views.setTextViewText(R.id.scheduleAmount5, strBalance);
							break;
						case 6:
							views.setTextViewText(R.id.scheduleDate6, strDate);
							views.setTextViewText(R.id.scheduleName6, strAccount);
							views.setTextViewText(R.id.scheduleAmount6, strBalance);
							break;
						case 7:
							views.setTextViewText(R.id.scheduleDate7, strDate);
							views.setTextViewText(R.id.scheduleName7, strAccount);
							views.setTextViewText(R.id.scheduleAmount7, strBalance);
							break;
						case 8:
							views.setTextViewText(R.id.scheduleDate8, strDate);
							views.setTextViewText(R.id.scheduleName8, strAccount);
							views.setTextViewText(R.id.scheduleAmount8, strBalance);
							break;
						case 9:
							views.setTextViewText(R.id.scheduleDate9, strDate);
							views.setTextViewText(R.id.scheduleName9, strAccount);
							views.setTextViewText(R.id.scheduleAmount9, strBalance);
							break;
						case 10:
							views.setTextViewText(R.id.scheduleDate10, strDate);
							views.setTextViewText(R.id.scheduleName10, strAccount);
							views.setTextViewText(R.id.scheduleAmount10, strBalance);
							break;
						case 11:
							views.setTextViewText(R.id.scheduleDate11, strDate);
							views.setTextViewText(R.id.scheduleName11, strAccount);
							views.setTextViewText(R.id.scheduleAmount11, strBalance);
							break;
						case 12:
							views.setTextViewText(R.id.scheduleDate12, strDate);
							views.setTextViewText(R.id.scheduleName12, strAccount);
							views.setTextViewText(R.id.scheduleAmount12, strBalance);
							break;
						default:
							c.moveToLast();
							break;
						}
						c.moveToNext();
						i++;
					}
					
					// Get our account to display on the home widget.
					c.close();
					Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, accountUsed);
					Log.d(TAG, "Uri = " + u.toString());
					c = context.getContentResolver().query(u, null, null, null, null);
					c.moveToFirst();
					Log.d(TAG, "Account used: " + c.getString(0));
					views.setTextViewText(R.id.hrAccountName, c.getString(0));
					views.setTextViewText(R.id.hrAccountBalance, Transaction.convertToDollars(Transaction.convertToPennies(c.getString(1))));
					appWidgetManager.updateAppWidget(appWidgetId, views);
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
