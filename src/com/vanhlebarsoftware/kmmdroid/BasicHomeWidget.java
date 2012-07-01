package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

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
		Cursor c = null;
		String strBal = null;
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.basichomewidget);
		
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
		String weeksToDisplay = prefs.getString("displayWeeks", "1");
		
		// Get our account and account balance from the database.
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, accountUsed);
		c = context.getContentResolver().query(u, null, null, null, null);
		c.moveToFirst();
		views.setTextViewText(R.id.hrAccountName, c.getString(0));
		strBal = Transaction.convertToDollars(Transaction.convertToPennies(c.getString(1)));
		views.setTextViewText(R.id.hrAccountBalance, strBal);
		c.close();
		
		// Take today's date and then determine the number of days to add to it based on the user's preference.
		GregorianCalendar calStart = new GregorianCalendar();
		GregorianCalendar calEnd = new GregorianCalendar();
		calEnd.add(Calendar.DAY_OF_MONTH, (7 * Integer.valueOf(weeksToDisplay)));
		String strStartDate = String.valueOf(calStart.get(Calendar.YEAR)) + "-" + String.valueOf(calStart.get(Calendar.MONTH)+ 1) + "-"
				+ String.valueOf(calStart.get(Calendar.DAY_OF_MONTH));
		String strEndDate = String.valueOf(calEnd.get(Calendar.YEAR)) + "-" + String.valueOf(calEnd.get(Calendar.MONTH)+ 1) + "-"
				+ String.valueOf(calEnd.get(Calendar.DAY_OF_MONTH));
		
		Log.d(TAG, "Start Date: " + strStartDate);
		Log.d(TAG, "End Date: " + strEndDate);
		
		// Get our active schedules from the database.
		c = context.getContentResolver().query(KMMDProvider.CONTENT_SCHEDULE_URI, null, null, null, null);
		
		// We have our open schedules from the database, now create the user defined period of cash flow.
		ArrayList<Schedule> Schedules = new ArrayList<Schedule>();
		Schedules = Schedule.BuildCashRequired(c, strStartDate, strEndDate, Transaction.convertToPennies(strBal));

		// close our cursor as we no longer need it.
		c.close();
		
		// Loop through all the instances of this widget
		for(int appWidgetId : appWidgetIds)
		{
			Log.d(TAG, "Updating widget: " + appWidgetId);
			
			// Loop thru the returned ArrayList and populate the widgets rows.
			Calendar Date = null;
			String strDate = null;
			String strDescription = null;
			String strAmount = null;
			String strBalance = null;
			Schedule sch = null;
			for(int i=1; i <= Schedules.size(); i++)
			{
				// Pop off the schedule to be displayed.
				sch = Schedules.get(i-1);
				Date = sch.getDueDate();
				strDescription = sch.getDescription();
				strAmount = Transaction.convertToDollars(sch.getAmount());
				strBalance = Transaction.convertToDollars(sch.getBalance());
				
				// Convert the Calendar object to a string formated: Month Day, Year (Jan 10, 2012)
				strDate = FormatDate(Date);
				
				switch(i)
				{
				case 1:
					views.setTextViewText(R.id.scheduleDate1, strDate);
					views.setTextViewText(R.id.scheduleName1, strDescription);
					views.setTextViewText(R.id.scheduleAmount1, strAmount);
					views.setTextViewText(R.id.BalanceAmount1, strBalance);
					break;
				case 2:
					views.setTextViewText(R.id.scheduleDate2, strDate);
					views.setTextViewText(R.id.scheduleName2, strDescription);
					views.setTextViewText(R.id.scheduleAmount2, strAmount);
					views.setTextViewText(R.id.BalanceAmount2, strBalance);
					break;
				case 3:
					views.setTextViewText(R.id.scheduleDate3, strDate);
					views.setTextViewText(R.id.scheduleName3, strDescription);
					views.setTextViewText(R.id.scheduleAmount3, strAmount);
					views.setTextViewText(R.id.BalanceAmount3, strBalance);
					break;
				case 4:
					views.setTextViewText(R.id.scheduleDate4, strDate);
					views.setTextViewText(R.id.scheduleName4, strDescription);
					views.setTextViewText(R.id.scheduleAmount4, strAmount);
					views.setTextViewText(R.id.BalanceAmount4, strBalance);
					break;
				case 5:
					views.setTextViewText(R.id.scheduleDate5, strDate);
					views.setTextViewText(R.id.scheduleName5, strDescription);
					views.setTextViewText(R.id.scheduleAmount5, strAmount);
					views.setTextViewText(R.id.BalanceAmount5, strBalance);
					break;
				case 6:
					views.setTextViewText(R.id.scheduleDate6, strDate);
					views.setTextViewText(R.id.scheduleName6, strDescription);
					views.setTextViewText(R.id.scheduleAmount6, strAmount);
					views.setTextViewText(R.id.BalanceAmount6, strBalance);
					break;
				case 7:
					views.setTextViewText(R.id.scheduleDate7, strDate);
					views.setTextViewText(R.id.scheduleName7, strDescription);
					views.setTextViewText(R.id.scheduleAmount7, strAmount);
					views.setTextViewText(R.id.BalanceAmount7, strBalance);
					break;
				case 8:
					views.setTextViewText(R.id.scheduleDate8, strDate);
					views.setTextViewText(R.id.scheduleName8, strDescription);
					views.setTextViewText(R.id.scheduleAmount8, strAmount);
					views.setTextViewText(R.id.BalanceAmount8, strBalance);
					break;
				case 9:
					views.setTextViewText(R.id.scheduleDate9, strDate);
					views.setTextViewText(R.id.scheduleName9, strDescription);
					views.setTextViewText(R.id.scheduleAmount9, strAmount);
					views.setTextViewText(R.id.BalanceAmount9, strBalance);
					break;
				case 10:
					views.setTextViewText(R.id.scheduleDate10, strDate);
					views.setTextViewText(R.id.scheduleName10, strDescription);
					views.setTextViewText(R.id.scheduleAmount10, strAmount);
					views.setTextViewText(R.id.BalanceAmount10, strBalance);
					break;
				case 11:
					views.setTextViewText(R.id.scheduleDate11, strDate);
					views.setTextViewText(R.id.scheduleName11, strDescription);
					views.setTextViewText(R.id.scheduleAmount11, strAmount);
					views.setTextViewText(R.id.BalanceAmount11, strBalance);
					break;
				case 12:
					views.setTextViewText(R.id.scheduleDate12, strDate);
					views.setTextViewText(R.id.scheduleName12, strDescription);
					views.setTextViewText(R.id.scheduleAmount12, strAmount);
					views.setTextViewText(R.id.BalanceAmount12, strBalance);
					break;
				default:
					// If we made it here we have to many to display so just skip the rest.
					i = Schedules.size() + 1;
					break;
				}
			}
				
			appWidgetManager.updateAppWidget(appWidgetId, views);
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
	
	/****************************************************************************************************************
	 * Helper Functions
	 ***************************************************************************************************************/
	
	private String FormatDate(Calendar date)
	{
		String formattedDate = null;
		
		formattedDate = (date.get(Calendar.MONTH) + 1) + "/" + String.valueOf(date.get(Calendar.DAY_OF_MONTH)) +
				"/" + String.valueOf(date.get(Calendar.YEAR));
		
		return formattedDate;
	}
}
