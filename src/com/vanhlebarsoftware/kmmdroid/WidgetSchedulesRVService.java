package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Binder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class WidgetSchedulesRVService extends RemoteViewsService
{
	private static final String TAG = WidgetSchedulesRVService.class.getSimpleName();
	public static final String DATA_CHANGED = "com.vanhlebarsoftware.kmmdroid.DATA_CHANGED";
	public static final String RECEIVE_HOME_UPDATE_NOTIFICATIONS = "com.vanhlebarsoftware.kmmdroid.RECEIVE_HOME_UPDATE_NOTIFICATIONS";
	private static final int ACTION_NEW = 1;
	private static final int ACTION_ENTER_SCHEDULE = 3;
	
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) 
	{
		// TODO Auto-generated method stub		
		return new WidgetSchedulesRVFactory(getApplicationContext(), intent);
	}
	
	/************************* Begin inner class WidgetSchedulessRVFactory *****************/
	class WidgetSchedulesRVFactory implements RemoteViewsService.RemoteViewsFactory 
	{
		private Context context;
		private Intent intent;
		private int widgetId;
		private String strBal = null;
		private String accountUsed = null;
		private String weeksToDisplay = null;
		private String strAccountName = null;
		private ArrayList<Schedule> SchedulesDue;
		private KMMDroidApp kmmdApp;
		
		public WidgetSchedulesRVFactory(Context context, Intent intent)
		{
			this.context = context;
			this.intent = intent;
			widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			kmmdApp = (KMMDroidApp) getApplication();
			SchedulesDue = new ArrayList<Schedule>();
		}

		public void onCreate() 
		{
			// TODO Auto-generated method stub
			final long token = Binder.clearCallingIdentity();
			try
			{
				getAccountInfo();
				SchedulesDue = executeQuery();
			}
			finally
			{
				Binder.restoreCallingIdentity(token);
			}
		}

		public void onDataSetChanged() 
		{
			// TODO Auto-generated method stub
			// Make sure our ArrayList is empty
			SchedulesDue.clear();
			int wId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			final long token = Binder.clearCallingIdentity();
			try
			{
				getAccountInfo();
				SchedulesDue = executeQuery();
			}
			finally
			{
				Binder.restoreCallingIdentity(token);
			}
		}

		public void onDestroy() 
		{
			// TODO Auto-generated method stub
			
		}

		public int getCount() 
		{
			// TODO Auto-generated method stub
			return SchedulesDue.size();
		}

		public RemoteViews getViewAt(int position) 
		{
			// TODO Auto-generated method stub
			// Create the Remote Views object and use it to populate the layout for each account.
			RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_schedule_row);
						
			//rv.setTextViewText(R.id.hrAccountName, strAccountName);
			//rv.setTextViewText(R.id.hrAccountBalance, strBal);
			
			// Move the cursor to the correct position
			Schedule schedule = SchedulesDue.get(position);
			
			// See if the schedule is past due or due today, set color accordingly.
			if(schedule.isPastDue())
				setPastDueColor(rv);
			else if(schedule.isDueToday())
				setDueTodayColor(rv);
			else
				setNormalColor(rv);
			
			// Get our values from the cursor here
			rv.setTextViewText(R.id.scheduleDate, FormatDate(schedule.getDueDate()));
			rv.setTextViewText(R.id.scheduleName, schedule.getDescription());
			rv.setTextViewText(R.id.scheduleAmount, Transaction.convertToDollars(schedule.getAmount(), true, false));
			rv.setTextViewText(R.id.BalanceAmount, Transaction.convertToDollars(schedule.getBalance(), true, false));
			
			// Create the fill-in Intent that adds the new transaction info for the current item to the template Intent.
			Intent fillInIntent = new Intent();
			String prefString = "widgetDatabasePath" + String.valueOf(widgetId);
			String path = kmmdApp.prefs.getString(prefString, "");
			fillInIntent.putExtra("widgetDatabasePath", path);
			fillInIntent.putExtra("scheduleId", schedule.getId());
			fillInIntent.putExtra("scheduleDescription", schedule.getDescription());
			//fillInIntent.putExtra("widgetId", String.valueOf(widgetId));			
			rv.setOnClickFillInIntent(R.id.scheduleDate, fillInIntent);
			rv.setOnClickFillInIntent(R.id.scheduleName, fillInIntent);
			rv.setOnClickFillInIntent(R.id.scheduleAmount, fillInIntent);
			rv.setOnClickFillInIntent(R.id.BalanceAmount, fillInIntent);
			
			return rv;
		}

		public RemoteViews getLoadingView() 
		{
			// TODO Auto-generated method stub
			return null;
		}

		public int getViewTypeCount() 
		{
			// TODO Auto-generated method stub
			return 4;
		}

		public long getItemId(int position) 
		{
			// TODO Auto-generated method stub
			//if(SchedulesDue.size() > 0)
			//	return Integer.valueOf(SchedulesDue.get(position).getId());
			//else
				return position;
		}

		public boolean hasStableIds() 
		{
			// TODO Auto-generated method stub
			return false;
		}
		
		/******************************** Helper functions ************************************/
		private ArrayList <Schedule> executeQuery()
		{		
			// Need to get the user prefs for our application.
			String accountUsed = kmmdApp.prefs.getString("accountUsed" + String.valueOf(widgetId), "");
			String weeksToDisplay = kmmdApp.prefs.getString("displayWeeks" + String.valueOf(widgetId), "1");
			String frag = "#" + widgetId;
			
			// Get our account and account balance from the database.
			Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, accountUsed + frag);
			u = Uri.parse(u.toString());

			Cursor cur = getContentResolver().query(u, null, null, null, null);
			cur.moveToFirst();
			strBal = Transaction.convertToDollars(Transaction.convertToPennies(cur.getString(cur.getColumnIndexOrThrow("balanceFormatted"))), true, false);
			strAccountName = cur.getString(cur.getColumnIndexOrThrow("accountName"));
			cur.close();
			
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
			Cursor c = getContentResolver().query(schedules, null, null, null, null);
			
			// We have our open schedules from the database, now create the user defined period of cash flow.
			ArrayList<Schedule> Schedules = new ArrayList<Schedule>();
			Schedules = Schedule.BuildCashRequired(c, strStartDate, strEndDate, Transaction.convertToPennies(strBal), getBaseContext(),
												   String.valueOf(widgetId));

			// close our cursor as we no longer need it.
			c.close();

			return Schedules;
		}
		
		private String FormatDate(Calendar date)
		{
			String formattedDate = null;
			String year = String.valueOf(date.get(Calendar.YEAR));
			year = year.substring(2);
			formattedDate = (date.get(Calendar.MONTH) + 1) + "/" + String.valueOf(date.get(Calendar.DAY_OF_MONTH)) +
					"/" + year;
			
			return formattedDate;
		}
		
		private void setPastDueColor(RemoteViews view)
		{
			view.setTextColor(R.id.scheduleDate, Color.RED);
			view.setTextColor(R.id.scheduleName, Color.RED);
			view.setTextColor(R.id.scheduleAmount, Color.RED);
			view.setTextColor(R.id.BalanceAmount, Color.RED);			
		}
		
		private void setDueTodayColor(RemoteViews view)
		{
			view.setTextColor(R.id.scheduleDate, Color.GREEN);
			view.setTextColor(R.id.scheduleName, Color.GREEN);
			view.setTextColor(R.id.scheduleAmount, Color.GREEN);
			view.setTextColor(R.id.BalanceAmount, Color.GREEN);
		}
		
		private void setNormalColor(RemoteViews view)
		{
			view.setTextColor(R.id.scheduleDate, Color.BLACK);
			view.setTextColor(R.id.scheduleName, Color.BLACK);
			view.setTextColor(R.id.scheduleAmount, Color.BLACK);
			view.setTextColor(R.id.BalanceAmount, Color.BLACK);			
		}
		
		private void getAccountInfo()
		{			
			// Create the Remote Views object and use it to populate the layout for each account.
			RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_schedules);
			
			// Need to get the user prefs for our application.
			String accountUsed = kmmdApp.prefs.getString("accountUsed" + String.valueOf(widgetId), "");
			String frag = "#" + widgetId;
			
			// Get our account and account balance from the database.
			Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, accountUsed + frag);
			u = Uri.parse(u.toString());

			Cursor cur = getContentResolver().query(u, null, null, null, null);
			cur.moveToFirst();
			strBal = Transaction.convertToDollars(Transaction.convertToPennies(cur.getString(cur.getColumnIndexOrThrow("balanceFormatted"))), true, false);
			strAccountName = cur.getString(cur.getColumnIndexOrThrow("accountName"));
			cur.close();
						
			rv.setTextViewText(R.id.hrAccountName, strAccountName);
			rv.setTextViewText(R.id.hrAccountBalance, strBal);	
			
			// Notify the App Widget Manager to update the widget using the modified remote view.
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			appWidgetManager.updateAppWidget(widgetId, rv);
		}
		
	}
	/************************** End inner class WidgetSchedulesRVFactory ****************/

}
