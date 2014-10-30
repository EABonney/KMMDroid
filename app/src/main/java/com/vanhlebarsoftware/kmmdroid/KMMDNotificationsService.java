package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

public class KMMDNotificationsService extends Service
{
	private static final String TAG = KMMDNotificationsService.class.getSimpleName();
	public static final String CHECK_SCHEDULES = "com.vanhlebarsoftware.kmmdroid.CHECK_SCHEDULES";
	private KMMDNotificationScheduleUpdater kmmdNotificationScheduleUpdater;
	private NotificationManager kmmdNotifcationMgr;
	private Notification kmmdNotification;
	private KMMDroidApp kmmdApp;
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
		this.kmmdApp = (KMMDroidApp) getApplication();
		this.kmmdNotificationScheduleUpdater = new KMMDNotificationScheduleUpdater();
	}

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		this.kmmdNotificationScheduleUpdater.interrupt();
		this.kmmdNotificationScheduleUpdater = null;
		this.kmmdApp.setServiceRunning(false);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		super.onStartCommand(intent, flags, startId);
		
		this.kmmdApp.setServiceRunning(true);
		this.kmmdNotificationScheduleUpdater.start();

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
	private void checkSchedules()
	{
		Cursor c = null;
		
		// Get our active schedules from the database.
		c = getContentResolver().query(KMMDProvider.CONTENT_SCHEDULE_URI, null, null, null, null);

		GregorianCalendar calToday = new GregorianCalendar();
		GregorianCalendar calYesterday = new GregorianCalendar();
		calYesterday = (GregorianCalendar) calToday.clone();
		calYesterday.add(Calendar.DAY_OF_MONTH, -1);
		String strToday = String.valueOf(calToday.get(Calendar.YEAR)) + "-" + String.valueOf(calToday.get(Calendar.MONTH)+ 1) + "-"
				+ String.valueOf(calToday.get(Calendar.DAY_OF_MONTH));
		String strYesterday = String.valueOf(calYesterday.get(Calendar.YEAR)) + "-" + String.valueOf(calYesterday.get(Calendar.MONTH)+ 1) + "-"
				+ String.valueOf(calYesterday.get(Calendar.DAY_OF_MONTH));
		
		// We have our open schedules from the database, now create the user defined period of cash flow.
		ArrayList<Schedule> Schedules = new ArrayList<Schedule>();
		
		Schedules = Schedule.BuildCashRequired(c, Schedule.padFormattedDate(strYesterday), 
											Schedule.padFormattedDate(strToday), Transaction.convertToPennies("0.00"), getBaseContext(), "9999");
		ArrayList<String> autoEnterSchedules = new ArrayList<String>();
		// See if we even have any schedules either past due or due today.
		if(Schedules.size() > 0)
		{
			// See how many pastDue schedules we have
			int pastDue = 0;
			int dueToday = 0;
			for(int i=0; i < Schedules.size(); i++)
			{
				if( Schedules.get(i).isPastDue() )
					pastDue++;
				else if( Schedules.get(i).isDueToday())
				{
					dueToday++;
					
					//See if the user wants us to auto-enter those schedules due today and setup for auto entry.
					if(kmmdApp.prefs.getBoolean("checkSchedulesNotifications", false) && Schedules.get(i).getAutoEnter())
						autoEnterSchedules.add(Schedules.get(i).getId());
				}
			}
			
    		// Take the list of schedules that need to be entered and create the transactions from the scheduleId and enter it into the database.
    		Schedule schedule = null;
    		ArrayList<String> newTransactionIds = new ArrayList<String>();
    		for(String scheduleId : autoEnterSchedules)
    		{
    			// Get the schedule from the supplied id
    			schedule = getSchedule(scheduleId);
    			Transaction transaction = schedule.convertToTransaction(createTransId());
    			transaction.setEntryDate(calToday);
    			// Enter the transaction into the database now.
    			ContentValues valuesTrans = new ContentValues();
    			valuesTrans.put("id", transaction.getTransId());
    			valuesTrans.put("txType", transaction.getTxType());
    			valuesTrans.put("postDate", transaction.formatDateString());
    			valuesTrans.put("memo", transaction.getMemo());
    			valuesTrans.put("entryDate", transaction.formatEntryDateString());
    			valuesTrans.put("currencyId", transaction.getCurrencyId());
    			valuesTrans.put("bankId", transaction.getBankId());
    			getContentResolver().insert(KMMDProvider.CONTENT_TRANSACTION_URI, valuesTrans);
    			// Enter the splits into the database now.
    			for(int i=0; i<transaction.splits.size(); i++)
    			{
    				ContentValues valuesSplit = new ContentValues();
    				valuesSplit.put("transactionId", transaction.splits.get(i).getTransactionId());
    				valuesSplit.put("txType", transaction.splits.get(i).getTxType());
    				valuesSplit.put("splitId", transaction.splits.get(i).getSplitId());
    				valuesSplit.put("payeeId", transaction.splits.get(i).getPayeeId());
    				valuesSplit.put("reconcileDate", transaction.splits.get(i).getReconcileDate());
    				valuesSplit.put("action", transaction.splits.get(i).getAction());
    				valuesSplit.put("reconcileFlag", transaction.splits.get(i).getReconcileFlag());
    				valuesSplit.put("value", transaction.splits.get(i).getValue());
    				valuesSplit.put("valueFormatted", transaction.splits.get(i).getValueFormatted());
    				valuesSplit.put("shares", transaction.splits.get(i).getShares());
    				valuesSplit.put("sharesFormatted", transaction.splits.get(i).getSharesFormatted());
    				valuesSplit.put("price", transaction.splits.get(i).getPrice());
    				valuesSplit.put("priceFormatted", transaction.splits.get(i).getPriceFormatted());
    				valuesSplit.put("memo", transaction.splits.get(i).getMemo());
    				valuesSplit.put("accountId", transaction.splits.get(i).getAccountId());
    				valuesSplit.put("checkNumber", transaction.splits.get(i).getCheckNumber());
    				valuesSplit.put("postDate", transaction.splits.get(i).getPostDate());
    				valuesSplit.put("bankId", transaction.splits.get(i).getBankId());   	
    				getContentResolver().insert(KMMDProvider.CONTENT_SPLIT_URI, valuesSplit);
    				valuesSplit.clear();
    			}
    			schedule = null;
    			
    			// Need to repull in the information for the schedule as the transactionId is changed above and stays on the transaction not the
    			// schedule. Not sure why...
    			schedule = getSchedule(scheduleId);
				//Need to advance the schedule to the next date and update the lastPayment and startDate dates to the recorded date of the transaction.
    			schedule.advanceDueDate(/*Schedule.getOccurence(schedule.getOccurence(), schedule.getOccurenceMultiplier())*/);
				ContentValues values = new ContentValues();
				values.put("nextPaymentDue", schedule.getDatabaseFormattedString());
				values.put("startDate", schedule.getDatabaseFormattedString());
				values.put("lastPayment", transaction.formatEntryDateString());
				Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_SCHEDULE_URI, schedule.getId());
				getContentResolver().update(u, values, null, new String[] { schedule.getId() });
				//Need to update the schedules splits in the kmmsplits table as this is where the upcoming bills in desktop comes from.
				for(int i=0; i < schedule.Splits.size(); i++)
				{
					Split s = schedule.Splits.get(i);
					s.setPostDate(schedule.getDatabaseFormattedString());
    				ContentValues valuesSplit = new ContentValues();
    				valuesSplit.put("transactionId", s.getTransactionId());
    				valuesSplit.put("txType", s.getTxType());
    				valuesSplit.put("splitId", s.getSplitId());
    				valuesSplit.put("payeeId", s.getPayeeId());
    				valuesSplit.put("reconcileDate", s.getReconcileDate());
    				valuesSplit.put("action", s.getAction());
    				valuesSplit.put("reconcileFlag", s.getReconcileFlag());
    				valuesSplit.put("value", s.getValue());
    				valuesSplit.put("valueFormatted", s.getValueFormatted());
    				valuesSplit.put("shares", s.getShares());
    				valuesSplit.put("sharesFormatted", s.getSharesFormatted());
    				valuesSplit.put("price", s.getPrice());
    				valuesSplit.put("priceFormatted", s.getPriceFormatted());
    				valuesSplit.put("memo", s.getMemo());
    				valuesSplit.put("accountId", s.getAccountId());
    				valuesSplit.put("checkNumber", s.getCheckNumber());
    				valuesSplit.put("postDate", s.getPostDate());
    				valuesSplit.put("bankId", s.getBankId());  
    				u = null;
    				u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI, schedule.getId());
    				getContentResolver().update(u, valuesSplit, null, new String[] { schedule.getId(), String.valueOf(s.getSplitId()) });
    				valuesSplit.clear();
    				// Need to update the Accounts for this split.
    				updateAccountInfo(s.getAccountId(), s.getValueFormatted(), 1);
					s = null;
					// Save this transactionId for the ScheduleNotificationsActivity.
					newTransactionIds.add(transaction.getTransId());
				}	
				//Need to update the schedule in kmmTransactions postDate to match the splits and the actual schedule for the next payment due date.
				values.clear();
				values.put("postDate", schedule.getDatabaseFormattedString());
				u = null;
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_TRANSACTION_URI, schedule.getId());
				getContentResolver().update(u, values, null, new String[] { schedule.getId() });
    			//Now update the kmmFileInfo row for the entered items.
				getContentResolver().update(KMMDProvider.CONTENT_FILEINFO_URI, null, "hiTransactionId", new String[] { "1" });
				getContentResolver().update(KMMDProvider.CONTENT_FILEINFO_URI, null, "transactions", new String[] { "1" });
				getContentResolver().update(KMMDProvider.CONTENT_FILEINFO_URI, null, "splits", new String[] { String.valueOf(transaction.splits.size()) });	
				getContentResolver().update(KMMDProvider.CONTENT_FILEINFO_URI, null, "lastModified", new String[] { "0" });
    			transaction = null;
    			schedule = null;
    			
    			// If the user has the preference item of updateFrequency = Auto fire off a Broadcast
    			if(kmmdApp.getAutoUpdate())
    			{
    				Log.d(TAG, "Fired off update notification");
    				Intent intent = new Intent(KMMDService.DATA_CHANGED);
    				sendBroadcast(intent, KMMDService.RECEIVE_HOME_UPDATE_NOTIFICATIONS);
    			}
    		}
    		
			this.kmmdNotifcationMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			this.kmmdNotification = new Notification(R.drawable.homewidget_icon, "", 0);
			Intent intent = new Intent(this, SchedulesNotificationsActivity.class);
			intent.putExtra("autoEnteredScheduleIds", autoEnterSchedules);
			intent.putExtra("newTransactionIds", newTransactionIds);
			intent.putExtra("accountUsed", kmmdApp.prefs.getString("accountUsed", ""));
			PendingIntent pendingIntent = PendingIntent.getActivity(this, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			this.kmmdNotification.when = System.currentTimeMillis();
			this.kmmdNotification.flags |= Notification.FLAG_AUTO_CANCEL;
			String notificationTitle = "Schedules due";
			String notificationSummary = null;
			String stroverDue = "";
			String strdueToday = "";
			String strautoEntered = "";
			if(kmmdApp.prefs.getBoolean("overdueSchedules", false) && pastDue > 0)
				stroverDue = "Overdue: " + String.valueOf(pastDue);
			if(kmmdApp.prefs.getBoolean("dueTodaySchedules", false) && dueToday > 0)
				strdueToday = "Due today: " + String.valueOf(dueToday);
			if(kmmdApp.prefs.getBoolean("checkSchedulesNotifications", false) && autoEnterSchedules.size() > 0)
				strautoEntered = "Auto: " + String.valueOf(autoEnterSchedules.size());
			
			notificationSummary = stroverDue + "    " + strdueToday + "    " + strautoEntered;
			this.kmmdNotification.setLatestEventInfo(this, notificationTitle, notificationSummary, pendingIntent);
			
			// Only need to notify if we actually have schedules past due or due today.
			if(Schedules.size() > 0)
				this.kmmdNotifcationMgr.notify(0, this.kmmdNotification);
			
			Log.d(TAG, "Schedules past due: " + String.valueOf(pastDue));
			Log.d(TAG, "Schedules due today: " + String.valueOf(dueToday));
		}
		
		// close our cursor as we no longer need it.
		c.close();	
	}
	
	// **************************************************************************************************
	// ************************************ Helper methods **********************************************
    
	private Schedule getSchedule(String schId)
	{
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_SCHEDULE_URI, schId);
		Cursor schedule = getContentResolver().query(u, new String[] { "*" }, "id=?", null, null);
		u = null;
		u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI, schId);
		Cursor splits = getContentResolver().query(u, null, null, null, null);
		u = null;
		u = Uri.withAppendedPath(KMMDProvider.CONTENT_TRANSACTION_URI, schId);
		Cursor transaction = getContentResolver().query(u, null, null, null, null);
		return new Schedule(schedule, splits, transaction, getBaseContext(), "9999");
	}

	private String createTransId()
	{
		final String[] dbColumns = { "hiTransactionId"};
		final String strOrderBy = "hiTransactionId DESC";
		// Run a query to get the Transaction ids so we can create a new one.
		Cursor cursor = getContentResolver().query(KMMDProvider.CONTENT_FILEINFO_URI, dbColumns, null, null, strOrderBy);
		cursor.moveToFirst();

		// Since id is in T000000000000000000 format, we need to pick off the actual number then increase by 1.
		int lastId = cursor.getInt(0);
		lastId = lastId +1;
		String nextId = Integer.toString(lastId);
		
		// Need to pad out the number so we get back to our P000000 format
		String newId = "T";
		for(int i= 0; i < (18 - nextId.length()); i++)
		{
			newId = newId + "0";
		}
		
		// Tack on the actual number created.
		newId = newId + nextId;
		
		return newId;
	}
	
	private void updateAccountInfo(String accountId, String transValue, int nChange)
	{
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, accountId);
		Cursor c = getContentResolver().query(u, new String[] { "balanceFormatted", "transactionCount" }, "id=?", new String[] { accountId }, null);
		c.moveToFirst();
		
		// Update the current balance for this account.
		long balance = Transaction.convertToPennies(c.getString(0));
		
		// If we are editing a transaction we need to reverse the original transaction values, this takes care of that for us.
		long tValue = Transaction.convertToPennies(transValue) * nChange;
		
		long newBalance = balance + tValue;

		// Update the number of transactions used for this account.
		int Count = c.getInt(1) + nChange;
		
		ContentValues values = new ContentValues();
		values.put("balanceFormatted", Transaction.convertToDollars(newBalance, false, false));
		values.put("balance", Account.createBalance(newBalance));
		values.put("transactionCount", Count);
		
		getContentResolver().update(u, values, "id=?", new String[] { accountId });
	}
	/**************************************************************************************************************
	 * Thread that will perform the actual updating of the notifications for schedules
	 *************************************************************************************************************/
	private class KMMDNotificationScheduleUpdater extends Thread
	{

		public KMMDNotificationScheduleUpdater()
		{
			super("KMMDNotificationScheduleUpdater-Updater");
		}
		
		@Override
		public void run()
		{
			// Check to see if the user has specified to use the last opened file. If not then skip checking the schedules.
			// This is a hack right now, need a better method of dealing with mutliple files.
			if(kmmdApp.prefs.getBoolean("openLastUsed", false))
				checkSchedules();
			else
				Log.d(TAG, "checkSchedules() skipped as user does not have correct settings!");
			stopSelf();
		}
	}
}
