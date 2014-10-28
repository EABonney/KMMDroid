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
	public static final String DATA_CHANGED = "com.vanhlebarsoftware.kmmdroid.DATA_CHANGED";
	
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) 
	{
		// TODO Auto-generated method stub		
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
		}

		public void onDestroy() 
		{
			c.close();
		}

		public int getCount() 
		{
			if(c != null)
			{
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
			String value = Transaction.convertToDollars(Transaction.convertToPennies(acct.getBalance()), true, false);
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
			String[] projection = { "id", "accountName", "accountType", "balanceFormatted", "kvpId", "kvpData" };
			String selection = "kvpKey='PreferredAccount' AND kvpData='Yes' AND kvpId=id";
			String frag = "#" + String.valueOf(widgetId);
			Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_PREFERREDACCOUNTS_URI, frag);
			u = Uri.parse(u.toString());
			String orderBy = "accountType, accountName ASC";
			
			return context.getContentResolver().query(u, projection, selection, null, orderBy);

		}
		
		private List<Account> formatList(Cursor accounts)
		{
			List<Account> preferredAccounts = new ArrayList<Account>();
			
			accounts.moveToFirst();
			for(int i=0; i<accounts.getCount(); i++)
			{
				Account account = new Account(accounts.getString(accounts.getColumnIndexOrThrow("id")),
						accounts.getString(accounts.getColumnIndexOrThrow("accountName")),
						accounts.getString(accounts.getColumnIndexOrThrow("balanceFormatted")),
						null, 0, false, context);
				
				if( !isClosed(account) )
				{
					preferredAccounts.add(account);
				}
				
				// make sure we don't move past the end of the cursor
				if( !accounts.isLast() )
					accounts.moveToNext();
			}
			
			// Find any investment accounts and create their "current" balances.
			// Loop through the preferred Accounts checking each one and setting the balance if it is an investment
			for( int i=0; i < preferredAccounts.size(); i++ )
			{
				if( isInvestment(preferredAccounts.get(i)) )
				{
					Long bal = getInvestmentBalance(preferredAccounts.get(i));
					preferredAccounts.get(i).setOpenBalance(Transaction.convertToDollars(bal, true, false));
				}
			}
			
			return preferredAccounts;
		}
		
		private boolean isClosed(Account account)
		{
			// Run the query against the Content provider to see if this account is closed, if not add it to the ArrayList
			String[] projection = { "kvpId" };
			String selection = "kvpId=? AND kvpKey='mm-closed'";
			String[] selectionArgs = { account.getId() };
			String frag = "#" + String.valueOf(widgetId);
			Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_KVP_URI, frag);
			u = Uri.parse(u.toString());
			Cursor acct = context.getContentResolver().query(u, projection, selection, selectionArgs, null);
			
			if( acct.getCount() > 0 )
			{
				acct.close();
				return true;
			}
			else
			{
				acct.close();
				return false;
			}
		}
		
		private boolean isInvestment(Account account)
		{
			String[] projection = { "accountType" };
			String selection = "id=?";
			String[] selectionArgs = { account.getId() };
			String frag = "#" + String.valueOf(widgetId);
			Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, frag);
			u = Uri.parse(u.toString());
			Cursor acct = context.getContentResolver().query(u, projection, selection, selectionArgs, null);
			acct.moveToFirst();
			
			if(Integer.valueOf(acct.getString(acct.getColumnIndexOrThrow("accountType"))) == Account.ACCOUNT_INVESTMENT)
			{
				acct.close();
				return true;
			}
			else
			{
				acct.close();
				return false;
			}
		}
		
		private Long getInvestmentBalance(Account account)
		{
			// We have an investment account, pull all child accounts for this parent.
			double total = 0.00;
			String[] prj = { "accountName", "currencyId", "balance", "balanceFormatted" };
			String selection = "parentId=?";
			String[] selectionAgs = { account.getId() };
			String frag = "#" + String.valueOf(widgetId);
			Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, frag);
			u = Uri.parse(u.toString());
			Cursor children = context.getContentResolver().query(u, prj, selection, selectionAgs, null);
			if( children != null )
			{
				children.moveToFirst();
				for(int i=0; i<children.getCount(); i++)
				{
					// Now we need to pull the latest price for this currency and multiply it by the balance, which is the number
					// of shares we own.
					String[] proj = { "priceDate", "price", "priceFormatted" };
					selection = "fromId=?";
					String[] selArgs = { children.getString(children.getColumnIndexOrThrow("currencyId")) };
					String orderBy = "priceDate DESC";
					u = Uri.withAppendedPath(KMMDProvider.CONTENT_PRICES, frag);
					u = Uri.parse(u.toString());
					Cursor pr = context.getContentResolver().query(u, proj, selection, selArgs, orderBy);
					
					// Now we have our prices, pick off the first one and use it as our multiplier.
					pr.moveToFirst();
					double price = Account.convertBalance(pr.getString(pr.getColumnIndexOrThrow("price"))).doubleValue() / 100;
					double bal = Account.convertBalance(children.getString(children.getColumnIndexOrThrow("balance"))).doubleValue();
					total = total + (price * bal);
					
					pr.close();
					
					// Make sure we don't go past the last cursor item.
					if( !children.isLast() )
						children.moveToNext();
					else
						i = children.getCount() + 1;
				}
			}
			
			children.close();
			return Long.valueOf((long) total);
		}
	}
	/************************** End of inner class WidgetPreferredAccountsRVFactory ****************/
}
