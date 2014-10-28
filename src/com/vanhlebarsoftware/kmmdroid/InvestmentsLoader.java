package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.app.Fragment;
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

public class InvestmentsLoader extends AsyncTaskLoader<List<Account>>
{
	private final static String TAG = InvestmentsLoader.class.getSimpleName();
	public final static String INVESTMENTSCHANGED = "Investments-Changed";
	private final static String[] dbColumns = { "accountName", "balance", "accountTypeString", "accountType", "id", 
												"accountTypeString", "accountType", "parentId", "currencyId"};
	private final String strSelectionAccts = "parentId=?";
	private static final String strOrderBy = "accountName ASC";
	List<Account> mAccounts;
	Context mContext;
	Fragment mFrag;
	Bundle mBundle;
	private InvestmentsListener mObserver = null;
	
	public InvestmentsLoader(Context context, Bundle extras) 
	{
		super(context);
		this.mContext = context;
		this.mBundle = extras;
	}

	@Override
	public List<Account> loadInBackground() 
	{
		return getInvestments();
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
        	this.mObserver = new InvestmentsListener(this);
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
        	Log.d(TAG, "Unregistering the Investments observer.");
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

	private List<Account> getInvestments()
	{
		List<Account> accounts = new ArrayList<Account>();
		//final Context context = getContext();
		String selectionArgs[] = { mBundle.getString("AccountId") };
		// Get our accounts for the home activity.
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, frag);
		u = Uri.parse(u.toString());
		Cursor c = mContext.getContentResolver().query(u, dbColumns, strSelectionAccts, selectionArgs, strOrderBy);
		
		// Loop over the cursor to build the accounts ArrayList and adjust each account for possible furture transactions.
		c.moveToFirst();
		// Get today's date and format it correctly for the sql query.
		Calendar date = Calendar.getInstance();
		String strDate = date.get(Calendar.YEAR) + "-" + (date.get(Calendar.MONTH) + 1) + "-" + date.get(Calendar.DAY_OF_MONTH);
		strDate = Schedule.padFormattedDate(strDate);
		for(int i=0; i<c.getCount(); i++)
		{
			// Get all the splits for this account that are in the future.
			String[] projection = { "valueFormatted" };
			String selection = "postDate>? AND splitId=0 AND accountId=? AND txType='N'";
			String[] selArgs = { strDate, c.getString(c.getColumnIndex("id")) };
			frag = "#9999";
			u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI, frag);
			u = Uri.parse(u.toString());
			Cursor splits = mContext.getContentResolver().query(u, projection, selection, selArgs, null);
			String strBal = Account.adjustForFutureTransactions(c.getString(c.getColumnIndex("id")), 
					Account.convertBalance(c.getString(c.getColumnIndex("balance"))), splits);
			accounts.add(new Account(c.getString(c.getColumnIndex("id")), c.getString(c.getColumnIndex("accountName")), strBal,
									 c.getString(c.getColumnIndex("accountTypeString")), c.getInt(c.getColumnIndex("accountType")),
									 isParent(c.getString(c.getColumnIndex("id"))), mContext));
			
			// Check to see if this account is closed.
			u = Uri.withAppendedPath(KMMDProvider.CONTENT_KVP_URI, frag);
			u = Uri.parse(u.toString());
			Cursor acc = mContext.getContentResolver().query(u, new String[] { "kvpId" }, "kvpId=? AND kvpKey='mm-closed'",
															new String[] { c.getString(c.getColumnIndex("id")) }, null);
			if( acc.getCount() > 0 )
				accounts.get(i).setIsClosed(true);
			else
				accounts.get(i).setIsClosed(false);
			
			// Get the prices for this stock
			u = Uri.withAppendedPath(KMMDProvider.CONTENT_PRICES, frag);
			u = Uri.parse(u.toString());
			String prProj[] = { "*" };
			String prSel = "fromId=?";
			String prSelArgs[] = { c.getString(c.getColumnIndexOrThrow("currencyId")) };
			Cursor prices = mContext.getContentResolver().query(u, prProj, prSel, prSelArgs, "priceDate DESC");
			accounts.get(accounts.size() - 1).setPrices(prices);
			
			c.moveToNext();
			splits.close();
			acc.close();
			prices.close();
		}
		
		// Close our cursor.
		c.close();
		
		return accounts;
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
    
    private class InvestmentsListener extends BroadcastReceiver
    {
    	final InvestmentsLoader mLoader;
    	
    	public InvestmentsListener(InvestmentsLoader loader)
    	{
    		mLoader = loader;
        	Log.d(TAG, "Registering observer for Investment changes.");
        	LocalBroadcastManager.getInstance(mContext).registerReceiver(this, new IntentFilter(INVESTMENTSCHANGED));
    	}
    	
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			Log.d(TAG, "Need to update the InvestmentsLoader!");
			if(intent.getExtras() != null)
			{
				mLoader.mBundle = intent.getExtras();
				mLoader.onContentChanged();
			}
			else
				Log.d(TAG, "We don't have any extras!!!");
		}
    	
    }
}
