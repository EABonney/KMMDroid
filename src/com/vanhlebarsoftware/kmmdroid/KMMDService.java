package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class KMMDService extends Service
{
	public static final String DATA_CHANGED = "com.vanhlebarsoftware.kmmdroid.DATA_CHANGED";
	public static final String RECEIVE_HOME_UPDATE_NOTIFICATIONS = "com.vanhlebarsoftware.kmmdroid.RECEIVE_HOME_UPDATE_NOTIFICATIONS";
	private static final String TAG = KMMDService.class.getSimpleName();
	private boolean runFlag = false;
	private KMMDUpdater kmmdUpdater;
	private KMMDroidApp kmmdApp;
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
		this.kmmdApp = (KMMDroidApp) getApplication();
		this.kmmdUpdater = new KMMDUpdater();
		
		// See if we have already set up the repeating alarm for our service. If we have not then set it up.
		//kmmdApp.setRepeatingAlarm();

		//this.kmmdUpdater.start();
		Log.d(TAG, "onCreated");
	}

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		this.runFlag = false;
		this.kmmdUpdater.interrupt();
		this.kmmdUpdater = null;
		this.kmmdApp.setServiceRunning(false);
		Log.d(TAG, "onDestroyed");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		super.onStartCommand(intent, flags, startId);
		this.runFlag = true;
		this.kmmdApp.setServiceRunning(true);
		this.kmmdUpdater.start();
		//updateHomeWidgets();
		Log.d(TAG, "onStarted");
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

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
	
	private void updateHomeWidgets()
	{
		Cursor c = null;
		String strBal = null;
		RemoteViews views = new RemoteViews(getPackageName(), R.layout.basichomewidget);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		ComponentName thisAppWidget = new ComponentName(getPackageName(), BasicHomeWidget.class.getName());
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

		// Need to get the user prefs for our application.
		String accountUsed = kmmdApp.prefs.getString("accountUsed", "");
		String weeksToDisplay = kmmdApp.prefs.getString("displayWeeks", "1");
		
		Log.d(TAG, "Starting to update Home Widgets.........");
		// Get our account and account balance from the database.
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, accountUsed);
		c = getContentResolver().query(u, null, null, null, null);
		Log.d(TAG, "Returned from KMMDProvider!");
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
		c = getContentResolver().query(KMMDProvider.CONTENT_SCHEDULE_URI, null, null, null, null);
		
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
	}
	
	private void clearHomeWidgets()
	{
		String strBalance = null;
		String strDate = null;
		String strDescription = null;
		String strAmount = null;
		
		Log.d(TAG, "Clearing HomeWidget data...");
		RemoteViews views = new RemoteViews(getPackageName(), R.layout.basichomewidget);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		ComponentName thisAppWidget = new ComponentName(getPackageName(), BasicHomeWidget.class.getName());
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);	
		
		// Loop through all the instances of this widget
		for(int appWidgetId : appWidgetIds)
		{
			for(int i=1; i <= 12; i++)
			{
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
				break;
			}
		}
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
	/**************************************************************************************************************
	 * Thread that will perform the actual updating of the home screen widgets
	 *************************************************************************************************************/
	private class KMMDUpdater extends Thread
	{

		public KMMDUpdater()
		{
			super("KMMDUpdater-Updater");
		}
		
		@Override
		public void run()
		{
			KMMDService kmmdService = KMMDService.this;
			
			while(kmmdService.runFlag)
			{
				Log.d(TAG, "KMMDService is running...");
				clearHomeWidgets();
				updateHomeWidgets();
				Log.d(TAG, "KMMDUpdater ran");
				stopSelf();
			}
		}
	}
}
