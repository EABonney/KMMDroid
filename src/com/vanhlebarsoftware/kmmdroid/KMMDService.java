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
	public static final String DATA_CHANGED = "com.vanhlebarsoftware.kmmdroid.DATA_CHANGED";
	public static final String RECEIVE_HOME_UPDATE_NOTIFICATIONS = "com.vanhlebarsoftware.kmmdroid.RECEIVE_HOME_UPDATE_NOTIFICATIONS";
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private static final int ACTION_ENTER_SCHEDULE = 3;
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
	}

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		this.runFlag = false;
		this.kmmdUpdater.interrupt();
		this.kmmdUpdater = null;
		this.kmmdApp.setServiceRunning(false);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		super.onStartCommand(intent, flags, startId);
		
		// See if we are starting the service with any extras in the intent.
        Bundle extras = intent.getExtras();
        String skippedScheduleId = null;
        if(extras != null)
        {
        	Log.d(TAG, "onStartCommand skipScheduleId: " + extras.getString("skipScheduleId"));
        	skippedScheduleId = extras.getString("skipScheduleId");
        }
        if(skippedScheduleId != null)
        	skipSchedule(skippedScheduleId);
   
		this.runFlag = true;
		this.kmmdApp.setServiceRunning(true);
		this.kmmdUpdater.start();

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
		ComponentName thisAppWidget = new ComponentName(getPackageName(), BasicHomeWidget.class.getName());
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

		// Need to get the user prefs for our application.
		String accountUsed = kmmdApp.prefs.getString("accountUsed", "");
		String weeksToDisplay = kmmdApp.prefs.getString("displayWeeks", "1");
		
		// Get our account and account balance from the database.
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, accountUsed);
		c = getContentResolver().query(u, null, null, null, null);
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
		
		// Get our active schedules from the database.
		c = getContentResolver().query(KMMDProvider.CONTENT_SCHEDULE_URI, null, null, null, null);
		
		// We have our open schedules from the database, now create the user defined period of cash flow.
		ArrayList<Schedule> Schedules = new ArrayList<Schedule>();
		Schedules = Schedule.BuildCashRequired(c, strStartDate, strEndDate, Transaction.convertToPennies(strBal));

		// close our cursor as we no longer need it.
		c.close();
		
		// Make sure we hide any unused rows.
		lastRow = Schedules.size();
		
		// Loop through all the instances of this widget
		for(int appWidgetId : appWidgetIds)
		{	
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
				
				// Convert the Calendar object to a string formated: Month Day, Year (1/10/12)
				strDate = FormatDate(Date);
				
				// See if the current schedule is past due, if so change the text color.
				if(sch.isPastDue())
					setPastDueColor(i, views);
				else
					setNormalColor(i, views);

				// Setup the basic Intent information for the onClick event of Skipping a schedule.
				Intent intent = new Intent(getBaseContext(), KMMDService.class);
				intent.putExtra("skipScheduleId", sch.getId());
				Intent intentEnter = new Intent(getBaseContext(), CreateModifyTransactionActivity.class);
				intentEnter.putExtra("scheduleId", sch.getId());
				intentEnter.putExtra("Action", ACTION_ENTER_SCHEDULE);
				
				switch(i)
				{
				case 1:
					views.setTextViewText(R.id.scheduleDate1, strDate);
					views.setTextViewText(R.id.scheduleName1, strDescription);
					views.setTextViewText(R.id.scheduleAmount1, strAmount);
					views.setTextViewText(R.id.BalanceAmount1, strBalance);
					
					// Skip scheduled transaction and Enter transaction onClickEvent handler
					intent.setAction("com.vanhlebarsoftware.kmmdroid.SkipSchedule1");
					intentEnter.setAction("com.vanhlebarsoftware.kmmdroid.EnterSchedule1");
					PendingIntent pendingIntent1 = PendingIntent.getService(this.getBaseContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
					PendingIntent pendingIntentEnterSch1 = PendingIntent.getActivity(this.getBaseContext(), 0, intentEnter, PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.kmmd_schSkip1, pendingIntent1);
					views.setOnClickPendingIntent(R.id.kmmd_schEnter1, pendingIntentEnterSch1);
					break;
				case 2:
					views.setTextViewText(R.id.scheduleDate2, strDate);
					views.setTextViewText(R.id.scheduleName2, strDescription);
					views.setTextViewText(R.id.scheduleAmount2, strAmount);
					views.setTextViewText(R.id.BalanceAmount2, strBalance);
					
					// Skip scheduled transaction and Enter onClickEvent handler
					intent.setAction("com.vanhlebarsoftware.kmmdroid.SkipSchedule2");
					intentEnter.setAction("com.vanhlebarsoftware.kmmdroid.EnterSchedule2");
					PendingIntent pendingIntent2 = PendingIntent.getService(this.getBaseContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
					PendingIntent pendingIntentEnterSch2 = PendingIntent.getActivity(this.getBaseContext(), 0, intentEnter, PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.kmmd_schSkip2, pendingIntent2);
					views.setOnClickPendingIntent(R.id.kmmd_schEnter2, pendingIntentEnterSch2);
					break;
				case 3:
					views.setTextViewText(R.id.scheduleDate3, strDate);
					views.setTextViewText(R.id.scheduleName3, strDescription);
					views.setTextViewText(R.id.scheduleAmount3, strAmount);
					views.setTextViewText(R.id.BalanceAmount3, strBalance);
					
					// Skip scheduled transaction and Enter onClickEvent handler
					intent.setAction("com.vanhlebarsoftware.kmmdroid.SkipSchedule3");
					intentEnter.setAction("com.vanhlebarsoftware.kmmdroid.EnterSchedule3");
					PendingIntent pendingIntent3 = PendingIntent.getService(this.getBaseContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
					PendingIntent pendingIntentEnterSch3 = PendingIntent.getActivity(this.getBaseContext(), 0, intentEnter, PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.kmmd_schSkip3, pendingIntent3);
					views.setOnClickPendingIntent(R.id.kmmd_schEnter3, pendingIntentEnterSch3);
					break;
				case 4:
					views.setTextViewText(R.id.scheduleDate4, strDate);
					views.setTextViewText(R.id.scheduleName4, strDescription);
					views.setTextViewText(R.id.scheduleAmount4, strAmount);
					views.setTextViewText(R.id.BalanceAmount4, strBalance);
					
					// Skip scheduled transaction and Enter onClickEvent handler
					intent.setAction("com.vanhlebarsoftware.kmmdroid.SkipSchedule4");
					intentEnter.setAction("com.vanhlebarsoftware.kmmdroid.EnterSchedule4");
					PendingIntent pendingIntent4 = PendingIntent.getService(this.getBaseContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
					PendingIntent pendingIntentEnterSch4 = PendingIntent.getActivity(this.getBaseContext(), 0, intentEnter, PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.kmmd_schSkip4, pendingIntent4);
					views.setOnClickPendingIntent(R.id.kmmd_schEnter4, pendingIntentEnterSch4);
					break;
				case 5:
					views.setTextViewText(R.id.scheduleDate5, strDate);
					views.setTextViewText(R.id.scheduleName5, strDescription);
					views.setTextViewText(R.id.scheduleAmount5, strAmount);
					views.setTextViewText(R.id.BalanceAmount5, strBalance);
					
					// Skip scheduled transaction and Enter onClickEvent handler
					intent.setAction("com.vanhlebarsoftware.kmmdroid.SkipSchedule5");
					intentEnter.setAction("com.vanhlebarsoftware.kmmdroid.EnterSchedule5");
					PendingIntent pendingIntent5 = PendingIntent.getService(this.getBaseContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
					PendingIntent pendintIntentEnterSch5 = PendingIntent.getActivity(this.getBaseContext(), 0, intentEnter, PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.kmmd_schSkip5, pendingIntent5);
					views.setOnClickPendingIntent(R.id.kmmd_schEnter5, pendintIntentEnterSch5);
					break;
				case 6:
					views.setTextViewText(R.id.scheduleDate6, strDate);
					views.setTextViewText(R.id.scheduleName6, strDescription);
					views.setTextViewText(R.id.scheduleAmount6, strAmount);
					views.setTextViewText(R.id.BalanceAmount6, strBalance);
					
					// Skip scheduled transaction and Enter onClickEvent handler
					intent.setAction("com.vanhlebarsoftware.kmmdroid.SkipSchedule6");
					intentEnter.setAction("com.vanhlebarsoftware.kmmdroid.EnterSchedule6");
					PendingIntent pendingIntent6 = PendingIntent.getService(this.getBaseContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
					PendingIntent pendingIntentEnterSch6 = PendingIntent.getActivity(this.getBaseContext(), 0, intentEnter, PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.kmmd_schSkip6, pendingIntent6);
					views.setOnClickPendingIntent(R.id.kmmd_schEnter6, pendingIntentEnterSch6);
					break;
				case 7:
					views.setTextViewText(R.id.scheduleDate7, strDate);
					views.setTextViewText(R.id.scheduleName7, strDescription);
					views.setTextViewText(R.id.scheduleAmount7, strAmount);
					views.setTextViewText(R.id.BalanceAmount7, strBalance);
					
					// Skip scheduled transaction and Enter onClickEvent handler
					intent.setAction("com.vanhlebarsoftware.kmmdroid.SkipSchedule7");
					intentEnter.setAction("com.vanhlebarsoftware.kmmdroid.EnterSchedule7");
					PendingIntent pendingIntent7 = PendingIntent.getService(this.getBaseContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
					PendingIntent pendingIntentEnterSch7 = PendingIntent.getActivity(this.getBaseContext(), 0, intentEnter, PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.kmmd_schSkip7, pendingIntent7);
					views.setOnClickPendingIntent(R.id.kmmd_schEnter7, pendingIntentEnterSch7);
					break;
				case 8:
					views.setTextViewText(R.id.scheduleDate8, strDate);
					views.setTextViewText(R.id.scheduleName8, strDescription);
					views.setTextViewText(R.id.scheduleAmount8, strAmount);
					views.setTextViewText(R.id.BalanceAmount8, strBalance);
					
					// Skip scheduled transaction and Enter onClickEvent handler
					intent.setAction("com.vanhlebarsoftware.kmmdroid.SkipSchedule8");
					intentEnter.setAction("com.vanhlebarsoftware.kmmdroid.EnterSchedule8");
					PendingIntent pendingIntent8 = PendingIntent.getService(this.getBaseContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
					PendingIntent pendingIntentEnterSch8 = PendingIntent.getActivity(this.getBaseContext(), 0, intentEnter, PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.kmmd_schSkip8, pendingIntent8);
					views.setOnClickPendingIntent(R.id.kmmd_schEnter8, pendingIntentEnterSch8);
					break;
				case 9:
					views.setTextViewText(R.id.scheduleDate9, strDate);
					views.setTextViewText(R.id.scheduleName9, strDescription);
					views.setTextViewText(R.id.scheduleAmount9, strAmount);
					views.setTextViewText(R.id.BalanceAmount9, strBalance);
					
					// Skip scheduled transaction and Enter onClickEvent handler
					intent.setAction("com.vanhlebarsoftware.kmmdroid.SkipSchedule9");
					intentEnter.setAction("com.vanhlebarsoftware.kmmdroid.EnterSchedule9");
					PendingIntent pendingIntent9 = PendingIntent.getService(this.getBaseContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
					PendingIntent pendingIntentEnterSch9 = PendingIntent.getActivity(this.getBaseContext(), 0, intentEnter, PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.kmmd_schSkip9, pendingIntent9);
					views.setOnClickPendingIntent(R.id.kmmd_schEnter9, pendingIntentEnterSch9);
					break;
				case 10:
					views.setTextViewText(R.id.scheduleDate10, strDate);
					views.setTextViewText(R.id.scheduleName10, strDescription);
					views.setTextViewText(R.id.scheduleAmount10, strAmount);
					views.setTextViewText(R.id.BalanceAmount10, strBalance);
					
					// Skip scheduled transaction and Enter onClickEvent handler
					intent.setAction("com.vanhlebarsoftware.kmmdroid.SkipSchedule10");
					intentEnter.setAction("com.vanhlebarsoftware.kmmdroid.EnterSchedule10");
					PendingIntent pendingIntent10 = PendingIntent.getService(this.getBaseContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
					PendingIntent pendingIntentEnterSch10 = PendingIntent.getActivity(this.getBaseContext(), 0, intentEnter, PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.kmmd_schSkip10, pendingIntent10);
					views.setOnClickPendingIntent(R.id.kmmd_schEnter10, pendingIntentEnterSch10);
					break;
				case 11:
					views.setTextViewText(R.id.scheduleDate11, strDate);
					views.setTextViewText(R.id.scheduleName11, strDescription);
					views.setTextViewText(R.id.scheduleAmount11, strAmount);
					views.setTextViewText(R.id.BalanceAmount11, strBalance);
					
					// Skip scheduled transaction and Enter onClickEvent handler
					intent.setAction("com.vanhlebarsoftware.kmmdroid.SkipSchedule11");
					intentEnter.setAction("com.vanhlebarsoftware.kmmdroid.EnterSchedule11");
					PendingIntent pendingIntent11 = PendingIntent.getService(this.getBaseContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
					PendingIntent pendingIntentEnterSch11 = PendingIntent.getActivity(this.getBaseContext(), 0, intentEnter, PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.kmmd_schSkip11, pendingIntent11);
					views.setOnClickPendingIntent(R.id.kmmd_schEnter11, pendingIntentEnterSch11);
					break;
				case 12:
					views.setTextViewText(R.id.scheduleDate12, strDate);
					views.setTextViewText(R.id.scheduleName12, strDescription);
					views.setTextViewText(R.id.scheduleAmount12, strAmount);
					views.setTextViewText(R.id.BalanceAmount12, strBalance);
					
					// Skip scheduled transaction and Enter onClickEvent handler
					intent.setAction("com.vanhlebarsoftware.kmmdroid.SkipSchedule12");
					intentEnter.setAction("com.vanhlebarsoftware.kmmdroid.EnterSchedule12");
					PendingIntent pendingIntent12 = PendingIntent.getService(this.getBaseContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
					PendingIntent pendingIntentEnterSch12 = PendingIntent.getActivity(this.getBaseContext(), 0, intentEnter, PendingIntent.FLAG_CANCEL_CURRENT);
					views.setOnClickPendingIntent(R.id.kmmd_schSkip12, pendingIntent12);
					views.setOnClickPendingIntent(R.id.kmmd_schEnter12, pendingIntentEnterSch12);
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
			PendingIntent pendingIntent = PendingIntent.getActivity(this.getBaseContext(), 0, intent, 0);
			views.setOnClickPendingIntent(R.id.kmmd_icon, pendingIntent);
			
			// Refresh icon
			intent = new Intent(getBaseContext(), KMMDService.class);
			intent.setAction("com.vanhlebarsoftware.kmmdroid.Refresh");
			pendingIntent = PendingIntent.getService(this.getBaseContext(), 0, intent, 0);
			views.setOnClickPendingIntent(R.id.kmmd_refresh, pendingIntent);
			
			// New transaction Icon
			intent = new Intent(getBaseContext(), CreateModifyTransactionActivity.class);
			intent.putExtra("Action", ACTION_NEW);
			intent.putExtra("accountUsed", accountUsed);
			pendingIntent = PendingIntent.getActivity(this.getBaseContext(), 0, intent, 0);
			views.setOnClickPendingIntent(R.id.kmmd_addTransaction, pendingIntent);
			
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
	
	private void skipSchedule(String schToSkip)
	{
		
		// Get our schedule that the user wants to skip from the database.
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_SCHEDULE_URI, schToSkip);
		Cursor c = getContentResolver().query(u, null, null, null, null);

		Schedule sch = new Schedule(c);
		sch.skipSchedule();
		ContentValues values = new ContentValues();
		values.put("nextPaymentDue", sch.getDatabaseFormattedString());
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
			KMMDService kmmdService = KMMDService.this;
			
			while(kmmdService.runFlag)
			{
				clearHomeWidgets();
				updateHomeWidgets();
				stopSelf();
			}
		}
	}
}
