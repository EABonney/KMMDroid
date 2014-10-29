package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

public class TransactionsLoader extends AsyncTaskLoader<List<Transaction>>
{
	private final static String TAG = TransactionsLoader.class.getSimpleName();
	public final static String TRANSCHANGED = "Transactions-Changed";
	List<Transaction> mTransactions;
	Context mContext;
	Bundle mBundle;
	private TransactionsListener mObserver = null;
	private String[] selectionArgs;
	private boolean showAll;
	
	public TransactionsLoader(Context context, Bundle extras) 
	{
		super(context);
		this.mContext = context;
		this.mBundle = extras;
		if( extras != null )
		{
			// We need to get the balance of this account.
			selectionArgs = this.mBundle.getStringArray("selectionArgs");
			showAll = this.mBundle.getBoolean("showAll", false);
			Log.d(TAG, "showAll: " + showAll);
			for(int i=0; i<selectionArgs.length;i++)
				Log.d(TAG, "selectionArgs[" + i + "]: " + selectionArgs[i]);
		}
	}

	@Override
	public List<Transaction> loadInBackground() 
	{	
		return getTransactions();
	}
	
    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override 
    public void deliverResult(List<Transaction> transactions) 
    {
        if (isReset()) 
        {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (transactions != null) 
            {
                onReleaseResources(transactions);
            }
        }
        List<Transaction> oldTransactions = transactions;
        mTransactions = transactions;

        if (isStarted()) 
        {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(transactions);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldTransactions != null) 
        {
            onReleaseResources(oldTransactions);
        }    	
    }
    
    /**
     * Handles a request to start the Loader.
     */
    @Override 
    protected void onStartLoading() 
    {
        if (mTransactions != null) 
        {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mTransactions);
        }
        
        // Start monitoring the data source.
        if( this.mObserver == null )
        {
        	this.mObserver = new TransactionsListener(this);
        }
        
        if (takeContentChanged() || mTransactions == null )
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
    public void onCanceled(List<Transaction> transactions) 
    {
        super.onCanceled(transactions);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(transactions);
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
        if (mTransactions != null) 
        {
            onReleaseResources(mTransactions);
            mTransactions = null;
        }
        
        // The loader is being reset so we should stop monitor for changes.
        if( this.mObserver != null )
        {
        	Log.d(TAG, "Unregistering the Transactions observer.");
            LocalBroadcastManager.getInstance(this.mContext).unregisterReceiver(this.mObserver);
            this.mObserver = null;
        }
    }
    
    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(List<Transaction> transactions) 
    {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    }

    private List<Transaction> getTransactions()
    {
    	String dbSelection = null;
    	long balance = getBalance(selectionArgs[0]);
    	
		List<Transaction> transactions = new ArrayList<Transaction>();
		final Context context = getContext();
		String[] dbColumns = {"transactionId AS _id", "payeeId", "value", "memo", "postDate", "name", "checkNumber", "reconcileFlag"};
		if( showAll )
		{
			dbSelection = "(kmmSplits.payeeId = kmmPayees.id AND accountId = ? AND txType = 'N') UNION SELECT transactionId, payeeId," +
					" value, memo, postDate, bankId, checkNumber, reconcileFlag FROM kmmSplits WHERE payeeID IS NULL AND accountId = ?" +
					" AND txType = 'N'";
		}
		else
		{
			dbSelection = "(kmmSplits.payeeId = kmmPayees.id AND accountId = ? AND txType = 'N') " + 
				"AND postDate <= ? AND postDate >= ? UNION SELECT transactionId, payeeId, value, memo, postDate, " +
				"bankId, checkNumber, reconcileFlag FROM kmmSplits WHERE payeeID IS NULL AND accountId = ? AND txType = 'N' AND postDate <= ?" +
				" AND postDate >= ?";
		}
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_LEDGER_URI, frag);
		u = Uri.parse(u.toString());
		Cursor c = context.getContentResolver().query(u, dbColumns, dbSelection, selectionArgs, null);
		
		// Load up our transactions into our ArrayList
		// This probably is extremely ineffecient, may want to review this better.
		Transaction trans = null;
		c.moveToFirst();
		// Make sure Transactions are empty.
		transactions.clear();
		Log.d(TAG, "Transactions returned: " + c.getCount());
		
		for(int i=0; i < c.getCount(); i++)
		{
			trans = new Transaction(Transaction.convertToDollars(Account.convertBalance(c.getString(c.getColumnIndex("value"))), true, false),
									c.getString(c.getColumnIndex("postDate")),
									c.getString(c.getColumnIndex("memo")), c.getString(c.getColumnIndex("_id")), "9999", context);
			transactions.add(trans);

			c.moveToNext();
		}
		
		// Ensure that we are in date order.
		TransactionComparator comparator = new TransactionComparator();
		Collections.sort(transactions, comparator);	

		// Calc the balances now.
		for(int i = transactions.size() - 1; i >= 0; i--)
		{
			if(i == transactions.size() - 1)
				transactions.get(i).setBalance(balance);
			else if (i == 0)
				balance = transactions.get(i).calcBalance(balance, 0);
			else
				balance = transactions.get(i).calcBalance(balance, transactions.get(i+1).getAmount());
		}
		
		// Create our "load more" option as a transaction to be displayed at the "end" of the list.
		if( !showAll )
		{
			Transaction loadMore = new Transaction("0.00", null, null, "999999", "9999", context);
			transactions.add(0, loadMore);
		}
		
		// Close our cursor.
		c.close();
		
		return transactions;
    }
    
    private long getBalance(String id)
    {
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI,id + frag);
		u = Uri.parse(u.toString());
		Cursor c = getContext().getContentResolver().query(u, new String[] { "balance" }, "id=?", null, null);
		c.moveToFirst();

		return Account.convertBalance(c.getString(c.getColumnIndex("balance")));    	
    }
    
	public class TransactionComparator implements Comparator<Transaction>
	{
		public int compare(Transaction arg0, Transaction arg1) 
		{
			return arg0.getDate().compareTo(arg1.getDate());
		}
	}
	
    private class TransactionsListener extends BroadcastReceiver
    {
    	final TransactionsLoader mLoader;
    	
    	public TransactionsListener(TransactionsLoader loader)
    	{
    		mLoader = loader;
        	Log.d(TAG, "Registering observer for Transaction changes.");
        	LocalBroadcastManager.getInstance(mContext).registerReceiver(this, new IntentFilter(TRANSCHANGED));
    	}
    	
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			Log.d(TAG, "Need to update the TransactionsLoader!");
			mLoader.mBundle = intent.getExtras();
			mLoader.onContentChanged();
		}
    	
    }
}