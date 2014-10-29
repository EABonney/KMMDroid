package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class HomeLoader extends AsyncTaskLoader<List<Account>>
{
	private final static String TAG = AccountsLoader.class.getSimpleName();
	public final static String HOMECHANGED = "HomeAccounts-Changed";
	private final static String[] dbColumns = { "accountName", "balance", "accountTypeString", "accountType", "id", 
												"accountTypeString", "accountType", "parentId"};
	private final String strSelectionAccts = "(accountType != ? AND accountType != ? AND accountType != ? AND accountType != ? AND parentId!=?)";
	//private final String strSelectionHome = " AND (balance != '0/1')";
	private static final String [] selectionArgs = new String[] { String.valueOf(Account.ACCOUNT_EXPENSE), 
																  String.valueOf(Account.ACCOUNT_INCOME),
																  String.valueOf(Account.ACCOUNT_STOCK),
																  String.valueOf(Account.ACCOUNT_EQUITY),
																  "{null}"};
	private static final String strOrderBy = "accountTypeString, accountName ASC";
	List<Account> mAccounts;
	Context mContext;
	Bundle mBundle;
	private HomeAccountsListener mObserver = null;
	
	public HomeLoader(Context context, Bundle extras) 
	{
		super(context);
		this.mContext = context;
		this.mBundle = extras;
	}

	@Override
	public List<Account> loadInBackground() 
	{	
		return getHomeAccounts();
	}
	
    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override 
    public void deliverResult(List<Account> accounts) 
    {
        if (isReset()) 
        {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (accounts != null) 
            {
                onReleaseResources(accounts);
            }
        }
        List<Account> oldAccounts = accounts;
        mAccounts = accounts;

        if (isStarted()) 
        {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(accounts);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldAccounts != null) 
        {
            onReleaseResources(oldAccounts);
        }    	
    }
    
    /**
     * Handles a request to start the Loader.
     */
    @Override 
    protected void onStartLoading() 
    {
        if (mAccounts != null) 
        {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mAccounts);
        }
        
        // Start monitoring the data source.
        if( this.mObserver == null )
        {
        	this.mObserver = new HomeAccountsListener(this);
        }
        //if( this.mObserver == null)
        //{
        //	this.mObserver = this.mContext.getContentResolver().registerContentObserver(KMMDProvider.CONTENT_ACCOUNT_URI, true, mObserver);
        //}
        
        if (takeContentChanged() || mAccounts == null )
        {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }
 
    /**
     * Handles a request to stop the Loader.
     */
    @Override protected void onStopLoading() 
    {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override 
    public void onCanceled(List<Account> accounts) 
    {
        super.onCanceled(accounts);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(accounts);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override 
    protected void onReset() 
    {
        super.onReset();
        
        // Ensure the loader is stopped
        onStopLoading();
        
        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (mAccounts != null) 
        {
            onReleaseResources(mAccounts);
            mAccounts = null;
        }
        
        // The loader is being reset so we should stop monitor for changes.
        if( this.mObserver != null )
        {
        	Log.d(TAG, "Unregistering the Home observer.");
            LocalBroadcastManager.getInstance(this.mContext).unregisterReceiver(this.mObserver);
            this.mObserver = null;
        }
    }
    
    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(List<Account> accounts) 
    {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    }
    
    private List<Account> getHomeAccounts()
    {
		final Context context = getContext();
		
		// Get our accounts for the home activity.
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, frag);
		u = Uri.parse(u.toString());
		Cursor c = context.getContentResolver().query(u, dbColumns, strSelectionAccts /*+ strSelectionHome*/, selectionArgs, strOrderBy);

		return getAccounts(c);
    }
    
    private List<Account> getAccounts(Cursor c)
    {
		List<Account> accounts = new ArrayList<Account>();
		final Context context = getContext();
		
		// Loop over the cursor to build the accounts ArrayList and adjust each account for possible furture transactions.
		c.moveToFirst();
		// Get today's date and format it correctly for the sql query.
		Calendar date = Calendar.getInstance();
		String strDate = date.get(Calendar.YEAR) + "-" + (date.get(Calendar.MONTH) + 1) + "-" + date.get(Calendar.DAY_OF_MONTH);
		strDate = Schedule.padFormattedDate(strDate);
		for(int i=0; i<c.getCount(); i++)
		{
			// Check to see if this account is closed.
			//String frag = "#9999";
			//Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_KVP_URI, frag);
			//u = Uri.parse(u.toString());
			//Cursor acc = context.getContentResolver().query(u, new String[] { "kvpId" }, "kvpId=? AND kvpKey='mm-closed'",
			//												new String[] { c.getString(c.getColumnIndex("id")) }, null);
			// Only add the account to the view if the account is open.
			Account account = new Account(c.getString(c.getColumnIndexOrThrow("id")),
										  c.getString(c.getColumnIndexOrThrow("accountName")),
										  null, null, 0, false, context);
			if( !isClosed(account) )
			{
				// Get all the splits for this account that are in the future.
				String[] projection = { "valueFormatted" };
				String selection = "postDate>? AND splitId=0 AND accountId=? AND txType='N'";
				String[] selectionArgs = { strDate, c.getString(c.getColumnIndex("id")) };
				String frag = "#9999";
				Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI, frag);
				u = Uri.parse(u.toString());
				Cursor splits = context.getContentResolver().query(u, projection, selection, selectionArgs, null);
				long bal = Account.convertBalance(c.getString(c.getColumnIndex("balance")));
				String strBal = Account.adjustForFutureTransactions(c.getString(c.getColumnIndex("id")), 
						bal, splits);
				accounts.add(new Account(c.getString(c.getColumnIndex("id")), c.getString(c.getColumnIndex("accountName")), strBal,
						c.getString(c.getColumnIndex("accountTypeString")), c.getInt(c.getColumnIndex("accountType")),
						isParent(c.getString(c.getColumnIndex("id"))), context));
						
				splits.close();
			}
			c.moveToNext();
			//acc.close();
		}
		
		// Close our cursor.
		c.close();
		
		// Find any investment accounts and create their "current" balances.
		// Loop through the preferred Accounts checking each one and setting the balance if it is an investment
		for( int i=0; i < accounts.size(); i++ )
		{
			if( isInvestment(accounts.get(i)) )
			{
				Long bal = getInvestmentBalance(accounts.get(i));
				accounts.get(i).setOpenBalance(Transaction.convertToDollars(bal, true, false));
			}
		}
		
		return accounts;
    }
    
	private boolean isClosed(Account account)
	{
		// Run the query against the Content provider to see if this account is closed, if not add it to the ArrayList
		final Context context = getContext();
		String[] projection = { "kvpId" };
		String selection = "kvpId=? AND kvpKey='mm-closed'";
		String[] selectionArgs = { account.getId() };
		String frag = "#9999";
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
		final Context context = getContext();
		String[] projection = { "accountType" };
		String selection = "id=?";
		String[] selectionArgs = { account.getId() };
		String frag = "#9999";
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
		final Context context = getContext();
		double total = 0.00;
		String[] prj = { "accountName", "currencyId", "balance", "balanceFormatted" };
		String selection = "parentId=?";
		String[] selectionAgs = { account.getId() };
		String frag = "#9999";
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
	
    private boolean isParent(String id)
    {
		final Context context = getContext();
		
		// See if this account is a parent account.
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, id + frag);
		u = Uri.parse(u.toString());
		Cursor c = context.getContentResolver().query(u, new String[] {"id"}, "parentId=?", new String[] { id }, null);
		
		if(c.getCount() > 0)
		{
			c.close();
			return true;
		}
		else
		{
			c.close();
			return false;
		}
    }
    
    private class HomeAccountsListener extends BroadcastReceiver
    {
    	final HomeLoader mLoader;
    	
    	public HomeAccountsListener(HomeLoader loader)
    	{
    		mLoader = loader;
        	Log.d(TAG, "Registering observer for Home changes.");
        	LocalBroadcastManager.getInstance(mContext).registerReceiver(this, new IntentFilter(HOMECHANGED));
    	}
    	
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			Log.d(TAG, "Need to update the HomeLoader!");
			mLoader.onContentChanged();
		}
    	
    }
}
