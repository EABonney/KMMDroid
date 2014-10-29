package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class KMMDService extends Service
{
	private static final String TAG = KMMDService.class.getSimpleName();
	public static final String DATA_CHANGED = "com.vanhlebarsoftware.kmmdroid.DATA_CHANGED";
	public static final String RECEIVE_HOME_UPDATE_NOTIFICATIONS = "com.vanhlebarsoftware.kmmdroid.RECEIVE_HOME_UPDATE_NOTIFICATIONS";
	private static final int ACTION_NEW = 1;
	private static final int ACTION_ENTER_SCHEDULE = 3;
	private KMMDUpdater kmmdUpdater;
	private KMMDroidApp kmmdApp;
	private int[] appWidgetIds;
	private int refreshWidgetId = 0;
	private int deletedWidget = 0;
	private String widgetId = null;
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
		this.kmmdApp = (KMMDroidApp) getApplication();
		this.kmmdUpdater = new KMMDUpdater();
	}

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		this.kmmdUpdater.interrupt();
		this.kmmdUpdater = null;
		this.kmmdApp.setServiceRunning(false);
		Log.d(TAG, "Closing database....");
		if(this.kmmdApp.isDbOpen())
			this.kmmdApp.closeDB();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		super.onStartCommand(intent, flags, startId);
		Bundle extras = null;
		boolean lastWidgetDeleted = false;
		String skippedScheduleId = null;
		
		if( this.kmmdApp.prefs.getBoolean("homeWidgetSetup", false) )
		{
			if( intent.hasExtra("lastWidgetDeleted") || intent.hasExtra("skipScheduleId") || 
				intent.hasExtra("appWidgetIds") || intent.hasExtra("widgetDeleted") || intent.hasExtra("refreshWidgetId") )
			{
				extras = intent.getExtras();
				lastWidgetDeleted = extras.getBoolean("lastWidgetDeleted");
				skippedScheduleId = extras.getString("skipScheduleId");
				appWidgetIds = extras.getIntArray("appWidgetIds");
				refreshWidgetId = extras.getInt("refreshWidgetId");
				deletedWidget = extras.getInt("widgetDeleted");	
		        widgetId = extras.getString("widgetId");
			}
			else
				Log.d(TAG, "No extras where passed!");
			// See if we are telling the service we deleted the last widget.
			// If so then update the preferences so the user can add another one later.
			if(lastWidgetDeleted)
			{
				SharedPreferences.Editor editor = this.kmmdApp.prefs.edit();
				editor.putBoolean("homeWidgetSetup", false);
				editor.apply();
			}
			else if(deletedWidget != 0)
			{
				Log.d(TAG, "Deleting preferences for widgetId: " + deletedWidget);
				SharedPreferences.Editor editor = this.kmmdApp.prefs.edit();
				editor.remove("widgetDatabasePath" + String.valueOf(deletedWidget));
				editor.remove("accountUsed" + String.valueOf(deletedWidget));
				editor.remove("updateFrequency" + String.valueOf(deletedWidget));
				editor.remove("displayWeeks" + String.valueOf(deletedWidget));
				editor.apply();				
			}
			else if(refreshWidgetId != 0)
			{
				this.kmmdApp.setServiceRunning(true);
				this.kmmdUpdater.start();
			}
			else
			{
				// See if we are starting the service with any extras in the intent.
				if(skippedScheduleId != null)
				{
					// We need to make sure we have the correct database open for this widget.
					skipSchedule(skippedScheduleId, widgetId);
					
					// Mark the file as dirty.
					kmmdApp.markFileIsDirty(true, widgetId);
					
					// Need to refresh the widget now.
					this.kmmdApp.setServiceRunning(true);
					this.kmmdUpdater.start();
				}
				else
				{
					// See if ALL the appWidgetIds are used in our preferences, if not don't start the service.
					Boolean validIds = true;
					for(int i=0; i<appWidgetIds.length; i++)
					{
						String path = this.kmmdApp.prefs.getString("widgetDatabasePath" + String.valueOf(appWidgetIds[i]), null);
						if(path == null)
						{
							validIds = false;
							break;
						}
					}
				
					if(validIds)
					{
						this.kmmdApp.setServiceRunning(true);
						this.kmmdUpdater.start();
					}
				}
			}
		}
		
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
		String year = String.valueOf(date.get(Calendar.YEAR));
		year = year.substring(2);
		formattedDate = (date.get(Calendar.MONTH) + 1) + "/" + String.valueOf(date.get(Calendar.DAY_OF_MONTH)) +
				"/" + year;
		
		return formattedDate;
	}
	
	private void updateHomeWidgets()
	{
		Cursor c = null;
		String strBal = null;
		int lastRow = 0;
		RemoteViews views = new RemoteViews(getPackageName(), R.layout.basichomewidget);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		if(appWidgetIds == null)
		{
			ComponentName thisAppWidget = new ComponentName(getPackageName(), BasicHomeWidget.class.getName());
			appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);			
		}
		
		// Loop through all the instances of this widget
		for(int appWidgetId : appWidgetIds)
		{
			// Need to get the user prefs for our application.
			String accountUsed = kmmdApp.prefs.getString("accountUsed" + String.valueOf(appWidgetId), "");
			String weeksToDisplay = kmmdApp.prefs.getString("displayWeeks" + String.valueOf(appWidgetId), "1");
			
			// Get our account and account balance from the database.
			String frag = "#" + appWidgetId;
			Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, accountUsed + frag);
			u = Uri.parse(u.toString());

			c = getContentResolver().query(u, null, null, null, null);
			c.moveToFirst();
			views.setTextViewText(R.id.hrAccountName, c.getString(0));
			strBal = Transaction.convertToDollars(Transaction.convertToPennies(c.getString(1)), true, false);
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
			
			// Get our active schedules from the database.
			Uri schedules = Uri.parse(KMMDProvider.CONTENT_SCHEDULE_URI.toString() + frag);
			c = getContentResolver().query(schedules, null, null, null, null);
			
			// We have our open schedules from the database, now create the user defined period of cash flow.
			ArrayList<Schedule> Schedules = new ArrayList<Schedule>();
			Schedules = Schedule.BuildCashRequired(c, strStartDate, strEndDate, Transaction.convertToPennies(strBal), getBaseContext(),
												   String.valueOf(appWidgetId));

			// close our cursor as we no longer need it.
			c.close();
			
			// Make sure we hide any unused rows.
			lastRow = Schedules.size();
			
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
				strAmount = Transaction.convertToDollars(sch.getAmount(), true, false);
				strBalance = Transaction.convertToDollars(sch.getBalance(), true, false);
				
				// Convert the Calendar object to a string formated: Month Day, Year (1/10/12)
				strDate = FormatDate(Date);
				
				// See if the schedule is past due or due today, set color accordingly.
				if(sch.isPastDue())
					setPastDueColor(i, views);
				else if(sch.isDueToday())
					setDueTodayColor(i, views);
				else
					setNormalColor(i, views);

				// Setup the intent for the first row to launch the ScheduleActions Dialog
				String URI_SCHEME = "com.vanhlebarsoftware.kmmdroid";
				Uri uri = Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/"),String.valueOf(appWidgetId));
				Intent intentDialog = new Intent(getBaseContext(), ScheduleActionsActivity.class);
				intentDialog.putExtra("Action", ACTION_ENTER_SCHEDULE);
				intentDialog.putExtra("widgetId", String.valueOf(appWidgetId));
				String prefString = "widgetDatabasePath" + String.valueOf(appWidgetId);
				String path = kmmdApp.prefs.getString(prefString, "");
				intentDialog.putExtra("widgetDatabasePath", path);
				intentDialog.setData(uri);
				
				String rowAction = null;
				switch(i)
				{
				case 1:
					views.setTextViewText(R.id.scheduleDate1, strDate);
					views.setTextViewText(R.id.scheduleName1, strDescription);
					views.setTextViewText(R.id.scheduleAmount1, strAmount);
					views.setTextViewText(R.id.BalanceAmount1, strBalance);
					
					// Set up the onClick action for clicking on a row to Enter or Skip a schedule.
					rowAction = "com.vanhlebarsoftware.kmmdroid.hwRowOne" + "#" + String.valueOf(appWidgetId);
					intentDialog.setAction(rowAction);
					intentDialog.putExtra("scheduleId", sch.getId());
					intentDialog.putExtra("scheduleDescription", sch.getDescription());
					PendingIntent pendingIntentDialog1 = PendingIntent.getActivity(getBaseContext(), 0, intentDialog, 
							PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.hwRowOne, pendingIntentDialog1);					
					break;
				case 2:
					views.setTextViewText(R.id.scheduleDate2, strDate);
					views.setTextViewText(R.id.scheduleName2, strDescription);
					views.setTextViewText(R.id.scheduleAmount2, strAmount);
					views.setTextViewText(R.id.BalanceAmount2, strBalance);
					
					// Set up the onClick action for clicking on a row to Enter or Skip a schedule.
					rowAction = "com.vanhlebarsoftware.kmmdroid.hwRowTwo" + "#" + String.valueOf(appWidgetId);
					intentDialog.setAction(rowAction);
					intentDialog.putExtra("scheduleId", sch.getId());
					intentDialog.putExtra("scheduleDescription", sch.getDescription());
					PendingIntent pendingIntentDialog2 = PendingIntent.getActivity(this.getBaseContext(), 0, intentDialog, 
							PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.hwRowTwo, pendingIntentDialog2);	
					break;
				case 3:
					views.setTextViewText(R.id.scheduleDate3, strDate);
					views.setTextViewText(R.id.scheduleName3, strDescription);
					views.setTextViewText(R.id.scheduleAmount3, strAmount);
					views.setTextViewText(R.id.BalanceAmount3, strBalance);
					
					// Set up the onClick action for clicking on a row to Enter or Skip a schedule.
					rowAction = "com.vanhlebarsoftware.kmmdroid.hwRowThree" + "#" + String.valueOf(appWidgetId);
					intentDialog.setAction(rowAction);
					intentDialog.putExtra("scheduleId", sch.getId());
					intentDialog.putExtra("scheduleDescription", sch.getDescription());
					PendingIntent pendingIntentDialog3 = PendingIntent.getActivity(this.getBaseContext(), 0, intentDialog, 
							PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.hwRowThree, pendingIntentDialog3);	
					break;
				case 4:
					views.setTextViewText(R.id.scheduleDate4, strDate);
					views.setTextViewText(R.id.scheduleName4, strDescription);
					views.setTextViewText(R.id.scheduleAmount4, strAmount);
					views.setTextViewText(R.id.BalanceAmount4, strBalance);
					
					// Set up the onClick action for clicking on a row to Enter or Skip a schedule.
					rowAction = "com.vanhlebarsoftware.kmmdroid.hwRowFour" + "#" + String.valueOf(appWidgetId);
					intentDialog.setAction(rowAction);
					intentDialog.putExtra("scheduleId", sch.getId());
					intentDialog.putExtra("scheduleDescription", sch.getDescription());
					PendingIntent pendingIntentDialog4 = PendingIntent.getActivity(this.getBaseContext(), 0, intentDialog, 
							PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.hwRowFour, pendingIntentDialog4);	
					break;
				case 5:
					views.setTextViewText(R.id.scheduleDate5, strDate);
					views.setTextViewText(R.id.scheduleName5, strDescription);
					views.setTextViewText(R.id.scheduleAmount5, strAmount);
					views.setTextViewText(R.id.BalanceAmount5, strBalance);
					
					// Set up the onClick action for clicking on a row to Enter or Skip a schedule.
					rowAction = "com.vanhlebarsoftware.kmmdroid.hwRowFive" + "#" + String.valueOf(appWidgetId);
					intentDialog.setAction(rowAction);
					intentDialog.putExtra("scheduleId", sch.getId());
					intentDialog.putExtra("scheduleDescription", sch.getDescription());
					PendingIntent pendingIntentDialog5 = PendingIntent.getActivity(this.getBaseContext(), 0, intentDialog, 
							PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.hwRowFive, pendingIntentDialog5);	
					break;
				case 6:
					views.setTextViewText(R.id.scheduleDate6, strDate);
					views.setTextViewText(R.id.scheduleName6, strDescription);
					views.setTextViewText(R.id.scheduleAmount6, strAmount);
					views.setTextViewText(R.id.BalanceAmount6, strBalance);
					
					// Set up the onClick action for clicking on a row to Enter or Skip a schedule.
					rowAction = "com.vanhlebarsoftware.kmmdroid.hwRowSix" + "#" + String.valueOf(appWidgetId);
					intentDialog.setAction(rowAction);
					intentDialog.putExtra("scheduleId", sch.getId());
					intentDialog.putExtra("scheduleDescription", sch.getDescription());
					PendingIntent pendingIntentDialog6 = PendingIntent.getActivity(this.getBaseContext(), 0, intentDialog, 
							PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.hwRowSix, pendingIntentDialog6);	
					break;
				case 7:
					views.setTextViewText(R.id.scheduleDate7, strDate);
					views.setTextViewText(R.id.scheduleName7, strDescription);
					views.setTextViewText(R.id.scheduleAmount7, strAmount);
					views.setTextViewText(R.id.BalanceAmount7, strBalance);
					
					// Set up the onClick action for clicking on a row to Enter or Skip a schedule.
					rowAction = "com.vanhlebarsoftware.kmmdroid.hwRowSeven" + "#" + String.valueOf(appWidgetId);
					intentDialog.setAction(rowAction);
					intentDialog.putExtra("scheduleId", sch.getId());
					intentDialog.putExtra("scheduleDescription", sch.getDescription());
					PendingIntent pendingIntentDialog7 = PendingIntent.getActivity(this.getBaseContext(), 0, intentDialog, 
							PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.hwRowSeven, pendingIntentDialog7);	
					break;
				case 8:
					views.setTextViewText(R.id.scheduleDate8, strDate);
					views.setTextViewText(R.id.scheduleName8, strDescription);
					views.setTextViewText(R.id.scheduleAmount8, strAmount);
					views.setTextViewText(R.id.BalanceAmount8, strBalance);
					
					// Set up the onClick action for clicking on a row to Enter or Skip a schedule.
					rowAction = "com.vanhlebarsoftware.kmmdroid.hwRowEight" + "#" + String.valueOf(appWidgetId);
					intentDialog.setAction(rowAction);
					intentDialog.putExtra("scheduleId", sch.getId());
					intentDialog.putExtra("scheduleDescription", sch.getDescription());
					PendingIntent pendingIntentDialog8 = PendingIntent.getActivity(this.getBaseContext(), 0, intentDialog, 
							PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.hwRowEight, pendingIntentDialog8);	
					break;
				case 9:
					views.setTextViewText(R.id.scheduleDate9, strDate);
					views.setTextViewText(R.id.scheduleName9, strDescription);
					views.setTextViewText(R.id.scheduleAmount9, strAmount);
					views.setTextViewText(R.id.BalanceAmount9, strBalance);
					
					// Set up the onClick action for clicking on a row to Enter or Skip a schedule.
					rowAction = "com.vanhlebarsoftware.kmmdroid.hwRowNine" + "#" + String.valueOf(appWidgetId);
					intentDialog.setAction(rowAction);
					intentDialog.putExtra("scheduleId", sch.getId());
					intentDialog.putExtra("scheduleDescription", sch.getDescription());
					PendingIntent pendingIntentDialog9 = PendingIntent.getActivity(this.getBaseContext(), 0, intentDialog, 
							PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.hwRowNine, pendingIntentDialog9);	
					break;
				case 10:
					views.setTextViewText(R.id.scheduleDate10, strDate);
					views.setTextViewText(R.id.scheduleName10, strDescription);
					views.setTextViewText(R.id.scheduleAmount10, strAmount);
					views.setTextViewText(R.id.BalanceAmount10, strBalance);
					
					// Set up the onClick action for clicking on a row to Enter or Skip a schedule.
					rowAction = "com.vanhlebarsoftware.kmmdroid.hwRowTen" + "#" + String.valueOf(appWidgetId);
					intentDialog.setAction(rowAction);
					intentDialog.putExtra("scheduleId", sch.getId());
					intentDialog.putExtra("scheduleDescription", sch.getDescription());
					PendingIntent pendingIntentDialog10 = PendingIntent.getActivity(this.getBaseContext(), 0, intentDialog, 
							PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.hwRowTen, pendingIntentDialog10);	
					break;
				case 11:
					views.setTextViewText(R.id.scheduleDate11, strDate);
					views.setTextViewText(R.id.scheduleName11, strDescription);
					views.setTextViewText(R.id.scheduleAmount11, strAmount);
					views.setTextViewText(R.id.BalanceAmount11, strBalance);
					
					// Set up the onClick action for clicking on a row to Enter or Skip a schedule.
					rowAction = "com.vanhlebarsoftware.kmmdroid.hwRowEleven" + "#" + String.valueOf(appWidgetId);
					intentDialog.setAction(rowAction);
					intentDialog.putExtra("scheduleId", sch.getId());
					intentDialog.putExtra("scheduleDescription", sch.getDescription());
					PendingIntent pendingIntentDialog11 = PendingIntent.getActivity(this.getBaseContext(), 0, intentDialog, 
							PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.hwRowEleven, pendingIntentDialog11);	
					break;
				case 12:
					views.setTextViewText(R.id.scheduleDate12, strDate);
					views.setTextViewText(R.id.scheduleName12, strDescription);
					views.setTextViewText(R.id.scheduleAmount12, strAmount);
					views.setTextViewText(R.id.BalanceAmount12, strBalance);
					
					// Set up the onClick action for clicking on a row to Enter or Skip a schedule.
					rowAction = "com.vanhlebarsoftware.kmmdroid.hwRowTwelve" + "#" + String.valueOf(appWidgetId);
					intentDialog.setAction(rowAction);
					intentDialog.putExtra("scheduleId", sch.getId());
					intentDialog.putExtra("scheduleDescription", sch.getDescription());
					PendingIntent pendingIntentDialog12 = PendingIntent.getActivity(this.getBaseContext(), 0, intentDialog, 
							PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.hwRowTwelve, pendingIntentDialog12);	
				default:
					// If we made it here we have to many to display so just skip the rest.
					i = Schedules.size() + 1;
					break;
				}

			}
	
			// see if we filled all 12 rows, if we didn't then we need to hide them.
			if(lastRow <= 12)
			{
				for(int i=lastRow+1; i<=12; i++)
					hideRow(i, views);
			}
			
			// Setup the onClick response to the various buttons on the widget
			// Start application by clicking on the icon
			Intent intent = new Intent(getBaseContext(), WelcomeActivity.class);
			intent.putExtra("fromWidgetId", String.valueOf(appWidgetId));
			String action = "com.vanhlebarsoftware.kmmdroid.Welcome" + "#" + String.valueOf(appWidgetId);
			intent.setAction(action);
			PendingIntent pendingIntent = PendingIntent.getActivity(this.getBaseContext(), 0, intent, 0);
			views.setOnClickPendingIntent(R.id.kmmd_icon, pendingIntent);
			
			// Refresh icon
			intent = new Intent(getBaseContext(), KMMDService.class);
			intent.putExtra("refreshWidgetId", appWidgetId);
			action = "com.vanhlebarsoftware.kmmdroid.Refresh" + "#" + String.valueOf(appWidgetId);
			intent.setAction(action);
			pendingIntent = PendingIntent.getService(this.getBaseContext(), 0, intent, 0);
			views.setOnClickPendingIntent(R.id.kmmd_refresh, pendingIntent);
			
			// New transaction Icon
			intent = new Intent(getBaseContext(), CreateModifyTransactionActivity.class);
			intent.putExtra("Action", ACTION_NEW);
			intent.putExtra("accountUsed", accountUsed);
			intent.putExtra("fromHome", true);
			intent.putExtra("widgetDatabasePath", "widgetDatabasePath" + String.valueOf(appWidgetId));
			action = "com.vanhlebarsoftware.kmmdroid.AddTransaction" + "#" + String.valueOf(appWidgetId);
			intent.setAction(action);
			pendingIntent = PendingIntent.getActivity(this.getBaseContext(), 0, intent, 0);
			views.setOnClickPendingIntent(R.id.kmmd_addTransaction, pendingIntent);
			
			// Preferences icon
			intent = new Intent(getBaseContext(), HomeScreenConfiguration.class);
			intent.putExtra("widgetId", String.valueOf(appWidgetId));
			action = "com.vanhlebarsoftware.kmmdroid.Preferences" + "#" + String.valueOf(appWidgetId);
			intent.setAction(action);
			pendingIntent = PendingIntent.getActivity(this.getBaseContext(), 0, intent, 0);
			views.setOnClickPendingIntent(R.id.kmmd_widgetSettings, pendingIntent);
			
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
	
	private void clearHomeWidgets()
	{
		String strBalance = null;
		String strDate = null;
		String strDescription = null;
		String strAmount = null;
		
		RemoteViews views = new RemoteViews(getPackageName(), R.layout.basichomewidget);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		ComponentName thisAppWidget = new ComponentName(getPackageName(), BasicHomeWidget.class.getName());
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);	
		
		// Loop through all the instances of this widget
		for(int appWidgetId : appWidgetIds)
		{
			// Need to update the Account used and the balance items.
			views.setTextViewText(R.id.hrAccountName, strBalance);
			views.setTextViewText(R.id.hrAccountBalance, strDescription);
			
			for(int i=1; i <= 12; i++)
			{
				switch(i)
				{
				case 1:
					views.setViewVisibility(R.id.hwRowOne, View.VISIBLE);
					views.setTextViewText(R.id.scheduleDate1, strDate);
					views.setTextViewText(R.id.scheduleName1, strDescription);
					views.setTextViewText(R.id.scheduleAmount1, strAmount);
					views.setTextViewText(R.id.BalanceAmount1, strBalance);

					break;
				case 2:
					views.setViewVisibility(R.id.hwRowTwo, View.VISIBLE);
					views.setTextViewText(R.id.scheduleDate2, strDate);
					views.setTextViewText(R.id.scheduleName2, strDescription);
					views.setTextViewText(R.id.scheduleAmount2, strAmount);
					views.setTextViewText(R.id.BalanceAmount2, strBalance);
					break;
				case 3:
					views.setViewVisibility(R.id.hwRowThree, View.VISIBLE);
					views.setTextViewText(R.id.scheduleDate3, strDate);
					views.setTextViewText(R.id.scheduleName3, strDescription);
					views.setTextViewText(R.id.scheduleAmount3, strAmount);
					views.setTextViewText(R.id.BalanceAmount3, strBalance);
					break;
			case 4:
					views.setViewVisibility(R.id.hwRowFour, View.VISIBLE);
					views.setTextViewText(R.id.scheduleDate4, strDate);
					views.setTextViewText(R.id.scheduleName4, strDescription);
					views.setTextViewText(R.id.scheduleAmount4, strAmount);
					views.setTextViewText(R.id.BalanceAmount4, strBalance);
					break;
			case 5:
					views.setViewVisibility(R.id.hwRowFive, View.VISIBLE);
					views.setTextViewText(R.id.scheduleDate5, strDate);
					views.setTextViewText(R.id.scheduleName5, strDescription);
					views.setTextViewText(R.id.scheduleAmount5, strAmount);
					views.setTextViewText(R.id.BalanceAmount5, strBalance);
					break;
			case 6:
					views.setViewVisibility(R.id.hwRowSix, View.VISIBLE);
					views.setTextViewText(R.id.scheduleDate6, strDate);
					views.setTextViewText(R.id.scheduleName6, strDescription);
					views.setTextViewText(R.id.scheduleAmount6, strAmount);
					views.setTextViewText(R.id.BalanceAmount6, strBalance);
				break;
			case 7:
					views.setViewVisibility(R.id.hwRowSeven, View.VISIBLE);
					views.setTextViewText(R.id.scheduleDate7, strDate);
					views.setTextViewText(R.id.scheduleName7, strDescription);
					views.setTextViewText(R.id.scheduleAmount7, strAmount);
					views.setTextViewText(R.id.BalanceAmount7, strBalance);
					break;
			case 8:
					views.setViewVisibility(R.id.hwRowEight, View.VISIBLE);
					views.setTextViewText(R.id.scheduleDate8, strDate);
					views.setTextViewText(R.id.scheduleName8, strDescription);
					views.setTextViewText(R.id.scheduleAmount8, strAmount);
					views.setTextViewText(R.id.BalanceAmount8, strBalance);
					break;
			case 9:
					views.setViewVisibility(R.id.hwRowNine, View.VISIBLE);
					views.setTextViewText(R.id.scheduleDate9, strDate);
					views.setTextViewText(R.id.scheduleName9, strDescription);
					views.setTextViewText(R.id.scheduleAmount9, strAmount);
					views.setTextViewText(R.id.BalanceAmount9, strBalance);
					break;
			case 10:
					views.setViewVisibility(R.id.hwRowTen, View.VISIBLE);
					views.setTextViewText(R.id.scheduleDate10, strDate);
					views.setTextViewText(R.id.scheduleName10, strDescription);
					views.setTextViewText(R.id.scheduleAmount10, strAmount);
					views.setTextViewText(R.id.BalanceAmount10, strBalance);
					break;
			case 11:
					views.setViewVisibility(R.id.hwRowEleven, View.VISIBLE);
					views.setTextViewText(R.id.scheduleDate11, strDate);
					views.setTextViewText(R.id.scheduleName11, strDescription);
					views.setTextViewText(R.id.scheduleAmount11, strAmount);
					views.setTextViewText(R.id.BalanceAmount11, strBalance);
					break;
			case 12:
					views.setViewVisibility(R.id.hwRowTwelve, View.VISIBLE);
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
	
	private void hideRow(int row, RemoteViews view)
	{	
		switch(row)
		{
		case 1:
			view.setViewVisibility(R.id.hwRowOne, View.INVISIBLE);
			break;
		case 2:
			view.setViewVisibility(R.id.hwRowTwo, View.INVISIBLE);
			break;
		case 3:
			view.setViewVisibility(R.id.hwRowThree, View.INVISIBLE);
			break;
		case 4:
			view.setViewVisibility(R.id.hwRowFour, View.INVISIBLE);
			break;
		case 5:
			view.setViewVisibility(R.id.hwRowFive, View.INVISIBLE);
			break;
		case 6:
			view.setViewVisibility(R.id.hwRowSix, View.INVISIBLE);
			break;
		case 7:
			view.setViewVisibility(R.id.hwRowSeven, View.INVISIBLE);
			break;
		case 8:
			view.setViewVisibility(R.id.hwRowEight, View.INVISIBLE);
			break;
		case 9:
			view.setViewVisibility(R.id.hwRowNine, View.INVISIBLE);
			break;
		case 10:
			view.setViewVisibility(R.id.hwRowTen, View.INVISIBLE);
			break;
		case 11:
			view.setViewVisibility(R.id.hwRowEleven, View.INVISIBLE);
			break;
		case 12:
			view.setViewVisibility(R.id.hwRowTwelve, View.INVISIBLE);
			break;
		}
	}
	
	private void setPastDueColor(int row, RemoteViews view)
	{
		switch(row)
		{
		case 1:
			view.setTextColor(R.id.scheduleDate1, Color.RED);
			view.setTextColor(R.id.scheduleName1, Color.RED);
			view.setTextColor(R.id.scheduleAmount1, Color.RED);
			view.setTextColor(R.id.BalanceAmount1, Color.RED);
			break;
		case 2:
			view.setTextColor(R.id.scheduleDate2, Color.RED);
			view.setTextColor(R.id.scheduleName2, Color.RED);
			view.setTextColor(R.id.scheduleAmount2, Color.RED);
			view.setTextColor(R.id.BalanceAmount2, Color.RED);
			break;
		case 3:
			view.setTextColor(R.id.scheduleDate3, Color.RED);
			view.setTextColor(R.id.scheduleName3, Color.RED);
			view.setTextColor(R.id.scheduleAmount3, Color.RED);
			view.setTextColor(R.id.BalanceAmount3, Color.RED);
			break;
		case 4:
			view.setTextColor(R.id.scheduleDate4, Color.RED);
			view.setTextColor(R.id.scheduleName4, Color.RED);
			view.setTextColor(R.id.scheduleAmount4, Color.RED);
			view.setTextColor(R.id.BalanceAmount4, Color.RED);
			break;
		case 5:
			view.setTextColor(R.id.scheduleDate5, Color.RED);
			view.setTextColor(R.id.scheduleName5, Color.RED);
			view.setTextColor(R.id.scheduleAmount5, Color.RED);
			view.setTextColor(R.id.BalanceAmount5, Color.RED);
			break;
		case 6:
			view.setTextColor(R.id.scheduleDate6, Color.RED);
			view.setTextColor(R.id.scheduleName6, Color.RED);
			view.setTextColor(R.id.scheduleAmount6, Color.RED);
			view.setTextColor(R.id.BalanceAmount6, Color.RED);
			break;
		case 7:
			view.setTextColor(R.id.scheduleDate7, Color.RED);
			view.setTextColor(R.id.scheduleName7, Color.RED);
			view.setTextColor(R.id.scheduleAmount7, Color.RED);
			view.setTextColor(R.id.BalanceAmount7, Color.RED);
			break;
		case 8:
			view.setTextColor(R.id.scheduleDate8, Color.RED);
			view.setTextColor(R.id.scheduleName8, Color.RED);
			view.setTextColor(R.id.scheduleAmount8, Color.RED);
			view.setTextColor(R.id.BalanceAmount8, Color.RED);
			break;
		case 9:
			view.setTextColor(R.id.scheduleDate9, Color.RED);
			view.setTextColor(R.id.scheduleName9, Color.RED);
			view.setTextColor(R.id.scheduleAmount9, Color.RED);
			view.setTextColor(R.id.BalanceAmount9, Color.RED);
			break;
		case 10:
			view.setTextColor(R.id.scheduleDate10, Color.RED);
			view.setTextColor(R.id.scheduleName10, Color.RED);
			view.setTextColor(R.id.scheduleAmount10, Color.RED);
			view.setTextColor(R.id.BalanceAmount10, Color.RED);
			break;
		case 11:
			view.setTextColor(R.id.scheduleDate11, Color.RED);
			view.setTextColor(R.id.scheduleName11, Color.RED);
			view.setTextColor(R.id.scheduleAmount11, Color.RED);
			view.setTextColor(R.id.BalanceAmount11, Color.RED);
			break;
		case 12:
			view.setTextColor(R.id.scheduleDate12, Color.RED);
			view.setTextColor(R.id.scheduleName12, Color.RED);
			view.setTextColor(R.id.scheduleAmount12, Color.RED);
			view.setTextColor(R.id.BalanceAmount12, Color.RED);
			break;
		}
	}
	
	private void setNormalColor(int row, RemoteViews view)
	{
		switch(row)
		{
		case 1:
			view.setTextColor(R.id.scheduleDate1, Color.BLACK);
			view.setTextColor(R.id.scheduleName1, Color.BLACK);
			view.setTextColor(R.id.scheduleAmount1, Color.BLACK);
			view.setTextColor(R.id.BalanceAmount1, Color.BLACK);
			break;
		case 2:
			view.setTextColor(R.id.scheduleDate2, Color.BLACK);
			view.setTextColor(R.id.scheduleName2, Color.BLACK);
			view.setTextColor(R.id.scheduleAmount2, Color.BLACK);
			view.setTextColor(R.id.BalanceAmount2, Color.BLACK);
			break;
		case 3:
			view.setTextColor(R.id.scheduleDate3, Color.BLACK);
			view.setTextColor(R.id.scheduleName3, Color.BLACK);
			view.setTextColor(R.id.scheduleAmount3, Color.BLACK);
			view.setTextColor(R.id.BalanceAmount3, Color.BLACK);
			break;
		case 4:
			view.setTextColor(R.id.scheduleDate4, Color.BLACK);
			view.setTextColor(R.id.scheduleName4, Color.BLACK);
			view.setTextColor(R.id.scheduleAmount4, Color.BLACK);
			view.setTextColor(R.id.BalanceAmount4, Color.BLACK);
			break;
		case 5:
			view.setTextColor(R.id.scheduleDate5, Color.BLACK);
			view.setTextColor(R.id.scheduleName5, Color.BLACK);
			view.setTextColor(R.id.scheduleAmount5, Color.BLACK);
			view.setTextColor(R.id.BalanceAmount5, Color.BLACK);
			break;
		case 6:
			view.setTextColor(R.id.scheduleDate6, Color.BLACK);
			view.setTextColor(R.id.scheduleName6, Color.BLACK);
			view.setTextColor(R.id.scheduleAmount6, Color.BLACK);
			view.setTextColor(R.id.BalanceAmount6, Color.BLACK);
			break;
		case 7:
			view.setTextColor(R.id.scheduleDate7, Color.BLACK);
			view.setTextColor(R.id.scheduleName7, Color.BLACK);
			view.setTextColor(R.id.scheduleAmount7, Color.BLACK);
			view.setTextColor(R.id.BalanceAmount7, Color.BLACK);
			break;
		case 8:
			view.setTextColor(R.id.scheduleDate8, Color.BLACK);
			view.setTextColor(R.id.scheduleName8, Color.BLACK);
			view.setTextColor(R.id.scheduleAmount8, Color.BLACK);
			view.setTextColor(R.id.BalanceAmount8, Color.BLACK);
			break;
		case 9:
			view.setTextColor(R.id.scheduleDate9, Color.BLACK);
			view.setTextColor(R.id.scheduleName9, Color.BLACK);
			view.setTextColor(R.id.scheduleAmount9, Color.BLACK);
			view.setTextColor(R.id.BalanceAmount9, Color.BLACK);
			break;
		case 10:
			view.setTextColor(R.id.scheduleDate10, Color.BLACK);
			view.setTextColor(R.id.scheduleName10, Color.BLACK);
			view.setTextColor(R.id.scheduleAmount10, Color.BLACK);
			view.setTextColor(R.id.BalanceAmount10, Color.BLACK);
			break;
		case 11:
			view.setTextColor(R.id.scheduleDate11, Color.BLACK);
			view.setTextColor(R.id.scheduleName11, Color.BLACK);
			view.setTextColor(R.id.scheduleAmount11, Color.BLACK);
			view.setTextColor(R.id.BalanceAmount11, Color.BLACK);
			break;
		case 12:
			view.setTextColor(R.id.scheduleDate12, Color.BLACK);
			view.setTextColor(R.id.scheduleName12, Color.BLACK);
			view.setTextColor(R.id.scheduleAmount12, Color.BLACK);
			view.setTextColor(R.id.BalanceAmount12, Color.BLACK);
			break;	
		}		
	}
	
	private void setDueTodayColor(int row, RemoteViews view)
	{
		switch(row)
		{
		case 1:
			view.setTextColor(R.id.scheduleDate1, Color.GREEN);
			view.setTextColor(R.id.scheduleName1, Color.GREEN);
			view.setTextColor(R.id.scheduleAmount1, Color.GREEN);
			view.setTextColor(R.id.BalanceAmount1, Color.GREEN);
			break;
		case 2:
			view.setTextColor(R.id.scheduleDate2, Color.GREEN);
			view.setTextColor(R.id.scheduleName2, Color.GREEN);
			view.setTextColor(R.id.scheduleAmount2, Color.GREEN);
			view.setTextColor(R.id.BalanceAmount2, Color.GREEN);
			break;
		case 3:
			view.setTextColor(R.id.scheduleDate3, Color.GREEN);
			view.setTextColor(R.id.scheduleName3, Color.GREEN);
			view.setTextColor(R.id.scheduleAmount3, Color.GREEN);
			view.setTextColor(R.id.BalanceAmount3, Color.GREEN);
			break;
		case 4:
			view.setTextColor(R.id.scheduleDate4, Color.GREEN);
			view.setTextColor(R.id.scheduleName4, Color.GREEN);
			view.setTextColor(R.id.scheduleAmount4, Color.GREEN);
			view.setTextColor(R.id.BalanceAmount4, Color.GREEN);
			break;
		case 5:
			view.setTextColor(R.id.scheduleDate5, Color.GREEN);
			view.setTextColor(R.id.scheduleName5, Color.GREEN);
			view.setTextColor(R.id.scheduleAmount5, Color.GREEN);
			view.setTextColor(R.id.BalanceAmount5, Color.GREEN);
			break;
		case 6:
			view.setTextColor(R.id.scheduleDate6, Color.GREEN);
			view.setTextColor(R.id.scheduleName6, Color.GREEN);
			view.setTextColor(R.id.scheduleAmount6, Color.GREEN);
			view.setTextColor(R.id.BalanceAmount6, Color.GREEN);
			break;
		case 7:
			view.setTextColor(R.id.scheduleDate7, Color.GREEN);
			view.setTextColor(R.id.scheduleName7, Color.GREEN);
			view.setTextColor(R.id.scheduleAmount7, Color.GREEN);
			view.setTextColor(R.id.BalanceAmount7, Color.GREEN);
			break;
		case 8:
			view.setTextColor(R.id.scheduleDate8, Color.GREEN);
			view.setTextColor(R.id.scheduleName8, Color.GREEN);
			view.setTextColor(R.id.scheduleAmount8, Color.GREEN);
			view.setTextColor(R.id.BalanceAmount8, Color.GREEN);
			break;
		case 9:
			view.setTextColor(R.id.scheduleDate9, Color.GREEN);
			view.setTextColor(R.id.scheduleName9, Color.GREEN);
			view.setTextColor(R.id.scheduleAmount9, Color.GREEN);
			view.setTextColor(R.id.BalanceAmount9, Color.GREEN);
			break;
		case 10:
			view.setTextColor(R.id.scheduleDate10, Color.GREEN);
			view.setTextColor(R.id.scheduleName10, Color.GREEN);
			view.setTextColor(R.id.scheduleAmount10, Color.GREEN);
			view.setTextColor(R.id.BalanceAmount10, Color.GREEN);
			break;
		case 11:
			view.setTextColor(R.id.scheduleDate11, Color.GREEN);
			view.setTextColor(R.id.scheduleName11, Color.GREEN);
			view.setTextColor(R.id.scheduleAmount11, Color.GREEN);
			view.setTextColor(R.id.BalanceAmount11, Color.GREEN);
			break;
		case 12:
			view.setTextColor(R.id.scheduleDate12, Color.GREEN);
			view.setTextColor(R.id.scheduleName12, Color.GREEN);
			view.setTextColor(R.id.scheduleAmount12, Color.GREEN);
			view.setTextColor(R.id.BalanceAmount12, Color.GREEN);
			break;	
		}		
	}
	
	private void skipSchedule(String schToSkip, String widgetId)
	{
		
		// Get our schedule that the user wants to skip from the database.
		String frag = "#" + widgetId;
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_SCHEDULE_URI, schToSkip + frag);
		u = Uri.parse(u.toString());
		Log.d(TAG, "Skipped schedule uri: " + u.toString());
		Cursor c = getContentResolver().query(u, null, null, null, null);

		Schedule sch = new Schedule(c, getBaseContext(), widgetId);
		sch.skipSchedule();
		
		// Update the nextPaymentDue and startDates for the actual schedule
		ContentValues values = new ContentValues();
		values.put("nextPaymentDue", sch.getDatabaseFormattedString());
		values.put("startDate", sch.getDatabaseFormattedString());
		getContentResolver().update(u, values, null, new String[] { sch.getId() });
		
		// Update the postDate on the schedules transaction.
		u = Uri.withAppendedPath(KMMDProvider.CONTENT_TRANSACTION_URI, schToSkip + frag);
		u = Uri.parse(u.toString());
		values.clear();
		values.put("postDate", sch.getDatabaseFormattedString());
		getContentResolver().update(u, values, null, new String[] { sch.getId() });
		
		// Update the splits postDate for the schedule.
		u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI, schToSkip + frag);
		u = Uri.parse(u.toString());
		values.clear();		
		values.put("postDate", sch.getDatabaseFormattedString());		
		getContentResolver().update(u, values, null, new String[] { sch.getId() });			
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
			clearHomeWidgets();
			updateHomeWidgets();
			stopSelf();
		}
	}
}
