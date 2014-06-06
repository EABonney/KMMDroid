package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.List;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class WidgetPreferredAccountsRVService extends RemoteViewsService
{
	private static final String TAG = WidgetPreferredAccountsRVService.class.getSimpleName();
	public static final String RECEIVE_HOME_UPDATE_NOTIFICATIONS = "com.vanhlebarsoftware.kmmdroid.RECEIVE_HOME_UPDATE_NOTIFICATIONS";
	
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) 
	{
		// TODO Auto-generated method stub
		Log.d(TAG, "Inside onGetViewFactory");
		
		return new WidgetPreferredAccountsRVFactory(getApplicationContext(), intent);
	}

	/************************* Begin inner class WidgetPreferredAccountsRVFactory *****************/
	class WidgetPreferredAccountsRVFactory implements RemoteViewsFactory 
	{
		private Context context;
		private Intent intent;
		private int widgetId;
		private Cursor c;
		private List<Account> mAccounts;
		
		public WidgetPreferredAccountsRVFactory(Context context, Intent intent)
		{
			this.context = context;
			this.intent = intent;
			widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			mAccounts = new ArrayList<Account>();
			
			Log.d(TAG, "Inside the contructor of the RVService class.");
			Log.d(TAG, "widgetId: " + String.valueOf(widgetId));
		}

		public void onCreate() 
		{
			final long token = Binder.clearCallingIdentity();
			try
			{
				c = executeQuery();
				// Make sure our ArrayList is empty before adding anything.
				mAccounts.clear();
				mAccounts = formatList(c);
			}
			finally
			{
				Binder.restoreCallingIdentity(token);
			}
			Log.d(TAG, "Cursor size: " + String.valueOf(c.getCount()));
		}

		public void onDataSetChanged() 
		{
			final long token = Binder.clearCallingIdentity();
			try
			{
				c = executeQuery();
				// Make sure our ArrayList is empty before adding anything.
				mAccounts.clear();
				mAccounts = formatList(c);
			}
			finally
			{
				Binder.restoreCallingIdentity(token);
			}
			Log.d(TAG, "Cursor size: " + String.valueOf(c.getCount()));
		}

		public void onDestroy() 
		{
			c.close();
		}

		public int getCount() 
		{
			if(c != null)
			{
				Log.d(TAG, "Cursor size: " + String.valueOf(mAccounts.size()));
				return mAccounts.size();
			}
			else
				return 0;
		}

		public RemoteViews getViewAt(int position) 
		{
			// Move the cursor to the correct position
			Account acct = mAccounts.get(position);
			//c.moveToPosition(position);
			
			// Get our values from the cursor here
			//int idIdx = c.getColumnIndex("id");
			//String id = c.getString(idIdx);
			String id = acct.getId();
			
			// Create the Remote Views object and use it to populate the layout for each account.
			RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.home_row);
			//rv.setTextViewText(R.id.hrAccountName, c.getString(c.getColumnIndex("accountName")));
			rv.setTextViewText(R.id.hrAccountName, acct.getName());
			//String value = Transaction.convertToDollars(Transaction.convertToPennies(c.getString(c.getColumnIndex("balanceFormatted"))), true);
			String value = Transaction.convertToDollars(Transaction.convertToPennies(acct.getBalance()), true);
			//rv.setTextViewText(R.id.hrAccountBalance, value);
			rv.setTextViewText(R.id.hrAccountBalance, value);
			
			// Create the fill-in Intent that adds the URI for the current item to the template Intent.
			Intent fillInIntent = new Intent();
	    	fillInIntent.putExtra("AccountId", acct.getId());
	    	fillInIntent.putExtra("AccountName", acct.getName());
	    	fillInIntent.putExtra("Balance", acct.getBalance());
			Uri uri = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, id);
			fillInIntent.setData(uri);
			
			rv.setOnClickFillInIntent(R.id.hrAccountName, fillInIntent);
			rv.setOnClickFillInIntent(R.id.hrAccountBalance, fillInIntent);
			
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
			return 2;
		}

		public long getItemId(int position) 
		{
			if(c != null )
				return c.getLong(c.getColumnIndex("id"));
			else
				return position;
		}

		public boolean hasStableIds() 
		{
			// TODO Auto-generated method stub
			return false;
		}

		/******************************** Helper functions ************************************/
		private Cursor executeQuery()
		{
			// Run the query against the Content provider to pull ALL "preferred" accounts
			String[] projection = { "id", "accountName", "balanceFormatted", "kvpId", "kvpData" };
			String selection = "kvpKey='PreferredAccount' AND kvpData='Yes' AND kvpId=id";
			String frag = "#" + String.valueOf(widgetId);
			Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_PREFERREDACCOUNTS_URI, frag);
			Log.d(TAG, "Using Uri: " + u.toString());
			u = Uri.parse(u.toString());
			
			return context.getContentResolver().query(u, projection, selection, null, null);

		}
		
		private List<Account> formatList(Cursor accounts)
		{
			List<Account> preferredAccounts = new ArrayList<Account>();
			
			accounts.moveToFirst();
			for(int i=0; i<accounts.getCount(); i++)
			{
				// Run the query against the Content provider to see if this account is closed, if not add it to the ArrayList
				String[] projection = { "kvpId" };
				String selection = "kvpId=? AND kvpKey='mm-closed'";
				String[] selectionArgs = { accounts.getString(accounts.getColumnIndexOrThrow("id")) };
				String frag = "#" + String.valueOf(widgetId);
				Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_KVP_URI, frag);
				Log.d(TAG, "Using Uri: " + u.toString());
				u = Uri.parse(u.toString());
				Cursor acct = context.getContentResolver().query(u, projection, selection, selectionArgs, null);
				
				if( acct.getCount() > 0 )
				{
					Log.d(TAG, "AccountId: " + accounts.getString(accounts.getColumnIndexOrThrow("id")));
					Log.d(TAG, "Account: " + accounts.getString(accounts.getColumnIndexOrThrow("accountName")) + " is closed!");

				}
				else
				{
					Log.d(TAG, "Adding open account to our ArrayList.");
					Log.d(TAG, "AccountId: " + accounts.getString(accounts.getColumnIndexOrThrow("id")));
					Log.d(TAG, "Account: " + accounts.getString(accounts.getColumnIndexOrThrow("accountName")));
					Account a = new Account(accounts.getString(accounts.getColumnIndexOrThrow("id")),
											accounts.getString(accounts.getColumnIndexOrThrow("accountName")),
											accounts.getString(accounts.getColumnIndexOrThrow("balanceFormatted")),
											null, 0, false, context);
					preferredAccounts.add(a);
				}
				
				// close our cursor
				acct.close();
				
				// make sure we don't move past the end of the cursor
				if( !accounts.isLast() )
					accounts.moveToNext();
			}
			
			return preferredAccounts;
		}
	}
	/************************** End of inner class WidgetPreferredAccountsRVFactory ****************/
}
