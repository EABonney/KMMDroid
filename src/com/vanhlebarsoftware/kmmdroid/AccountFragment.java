package com.vanhlebarsoftware.kmmdroid;

import com.vanhlebarsoftware.kmmdroid.PayeeFragment.OnSendWidgetIdListener;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
//import android.app.Fragment;
//import android.app.LoaderManager;
//import android.content.CursorLoader;
//import android.content.Loader;
//import android.widget.SimpleCursorAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

public class AccountFragment extends Fragment implements
								LoaderManager.LoaderCallbacks<Cursor> 
{
	private static final String TAG = AccountFragment.class.getSimpleName();
	public static final int ACCOUNTS_LOADER = 0x60;
	private OnSendWidgetIdListener onSendWidgetId;
	static final String[] FROM = { "name" };
	static final int[] TO = { android.R.id.text1 };
	private String widgetId = "9999";
	private String accountId = null;
	private boolean firstRun = true;
	private boolean isDirty = false;
	Spinner spinAccounts;
	SimpleCursorAdapter adapter;

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		
		// Get the widgetId that we need from the parent.
		onSendWidgetId.onSendWidgetId(ACCOUNTS_LOADER);
		
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(ACCOUNTS_LOADER, null, this);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) 
	{
		super.onAttach(activity);
		
		try
		{
			onSendWidgetId = (OnSendWidgetIdListener) activity;
		}
		catch(ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement OnSendWidgetIdListener");
		}
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.account_fragment, container, false);
		
		// Get our views
		spinAccounts = (Spinner) view.findViewById(R.id.scheduleAccount);
		
		// Set up the adapter
		adapter = new SimpleCursorAdapter(getActivity().getBaseContext(), android.R.layout.simple_spinner_item, null, FROM, TO, 0);
		spinAccounts.setAdapter(adapter);

        spinAccounts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
    		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    		{
    			if( !firstRun )
    			{
    				Cursor c = (Cursor) parent.getAdapter().getItem(pos);
    				accountId = c.getString(c.getColumnIndex("_id")).toString();
    				isDirty = true;   
    				//c.close();
    			}
    			else
    				firstRun = false;
    		}

			public void onNothingSelected(AdapterView<?> arg0) 
			{
				// do nothing.
			}
        });
		return view;
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) 
	{
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI,"#" + this.widgetId);
		u = Uri.parse(u.toString());
		String[] dbColumns = new String[] { "accountName", "id AS _id" };
		String dbSelection = "(accountType=? OR accountType=? OR accountType=? OR accountType=?) AND (balance != '0/1')";
		String[] dbSelectionArgs = { String.valueOf(Account.ACCOUNT_CHECKING), String.valueOf(Account.ACCOUNT_SAVINGS), 
				String.valueOf(Account.ACCOUNT_LIABILITY), String.valueOf(Account.ACCOUNT_CREDITCARD) };

		return new CursorLoader(getActivity().getBaseContext(), u, dbColumns, dbSelection, dbSelectionArgs, "acountName ASC");
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor accounts) 
	{
		adapter.swapCursor(accounts);
		
		updateUIElements();
	}

	public void onLoaderReset(Loader<Cursor> loader) 
	{
		adapter.swapCursor(null);
	}
	// *******************************************************************************************************
	// *********************************** Public Event Interfaces *******************************************
	public interface OnSendWidgetIdListener
	{
		public void onSendWidgetId(int fromFrag);
	}
	// **************************************************************************************************
	// ************************************ Helper methods **********************************************
	public void setWidgetId(String wId)
	{
		this.widgetId = wId;
	}
	
	public void setAccountId(String aId)
	{
		this.accountId = aId;
	}
	
	public String getAccountd()
	{
		return this.accountId;
	}
	
	private void updateUIElements()
	{
		if( this.accountId != null )
		{
			for(int i=0; i<adapter.getCount();i++)
			{
				Cursor c = (Cursor) adapter.getItem(i);
				if( this.accountId.equalsIgnoreCase(c.getString(c.getColumnIndex("_id"))) )
				{
					this.spinAccounts.setSelection(i);
					break;
				}
			}		
		}
	}
}
