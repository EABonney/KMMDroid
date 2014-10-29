package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class TransactionsTabActivity extends Fragment implements
LoaderManager.LoaderCallbacks<Cursor>
{
	private static final String TAG = TransactionsTabActivity.class.getSimpleName();
	private static final int TRANSTAB_LOADER = 0x22;
	private static final String[] dbColumns = { "splitId", "transactionId AS _id", "valueFormatted",
												"accountId", "postDate", "id", "accountName", "memo" };
	private static final String strSelectionPayee = "(accountId = id) AND payeeId = ? AND splitId = 0 AND txType = 'N'";
	private static final String strSelectionCategory = "(accountId = id) AND accountId = ? AND txType = 'N'";
	private static final String strOrderBy = "postDate ASC";
	static final String[] FROM = { "postDate", "accountName", "valueFormatted", "memo" };
	static final int[] TO = { R.id.ptrDate, R.id.ptrAccount, R.id.ptrAmount, R.id.ptrDetails };
	private OnSendTransactionTabDataListener onSendTransactionTabData;
	private String PayeeId = null;
	private String PayeeName = null;
	private String CategoryId = null;
	private String CategoryName = null;
	//private boolean fromPayee = false;
	CreateModifyPayeeActivity payeeTabHost;
	CreateModifyCategoriesActivity categoryTabHost;
	//KMMDroidApp KMMDapp;
	//Cursor cursor;
	ListView listPayeeTrans;
	TransactionsAdapter adapter;
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) 
	{
		super.onAttach(activity);
		
		try
		{
			onSendTransactionTabData = (OnSendTransactionTabDataListener) activity;
		}
		catch(ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement OnSendTransactionTabDataListener");
		}
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
        if (container == null) 
        {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }
        
        View view = inflater.inflate(R.layout.payee_transactions, container, false);
        
        // Set the AccountId selected fields.
        //Bundle extras = savedInstanceState;
        //PayeeId = extras.getString("PayeeId");
        //PayeeName = extras.getString("PayeeName");
        //CategoryId = extras.getString("CategoryId");
        //CategoryName = extras.getString("CategoryName");
        
        //if( PayeeId != null )
        //	fromPayee = true;
    
        // Find our views
        listPayeeTrans = (ListView) view.findViewById(R.id.listPayeeTransView);
        
		// Set up the adapter
		adapter = new TransactionsAdapter(getActivity(), R.layout.payee_transactions_row, null, FROM, TO, 0);
		//adapter.setViewBinder(VIEW_BINDER);
		listPayeeTrans.setAdapter(adapter);
		
		// Prepare the loader. Either re-connect with the existing one,
		// or start a new one.
		getLoaderManager().initLoader(TRANSTAB_LOADER, null, this);
        
        return view;
	}
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        
        // Get our application
        //KMMDapp = ((KMMDroidApp) getApplication());

        // See if the database is already open, if not open it Read/Write.
        //if(!KMMDapp.isDbOpen())
        //{
        //	KMMDapp.openDB();
        //}
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public void onResume()
	{
		super.onResume();
	
		//Get all the accounts to be displayed.
		//if(PayeeId != null)
		//{
			// Put the PayeeId into a String array
		//	String[] selectionArgs = { PayeeId };
		//	cursor = KMMDapp.db.query(dbTable, dbColumns, strSelectionPayee, selectionArgs, null, null, strOrderBy);
		//}
		//else if(CategoryId != null)
		//{
			// Put the PayeeId into a String array
		//	String[] selectionArgs = { CategoryId };
		//	cursor = KMMDapp.db.query(dbTable, dbColumns, strSelectionCategory, selectionArgs, null, null, strOrderBy);
		//}
		
		//startManagingCursor(cursor);
		
		// Set up the adapter
		//adapter = new SimpleCursorAdapter(this, R.layout.payee_transactions_row, cursor, FROM, TO);
		//adapter.setViewBinder(VIEW_BINDER);
		//listPayeeTrans.setAdapter(adapter);
	}
	
/*	@Override
	public void onBackPressed()
	{
		boolean isDirty = false;
		
        // Get the correct tabHost based on the parent.
        if( fromPayee )
        {
        	payeeTabHost = ((CreateModifyPayeeActivity) this.getParent());
        	isDirty = payeeTabHost.getIsDirty();
        }
        else
        {
        	categoryTabHost = ((CreateModifyCategoriesActivity) this.getParent());
        	isDirty = categoryTabHost.getIsDirty();
        }
        
		if( isDirty )
		{
			AlertDialog.Builder alertDel = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogNoTitle));
			alertDel.setTitle(R.string.BackActionWarning);
			alertDel.setMessage(getString(R.string.titleBackActionWarning));

			alertDel.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					finish();
				}
			});
			
			alertDel.setNegativeButton(getString(R.string.titleButtonCancel), new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int whichButton) 
				{
					// Canceled.
				}
			});				
			alertDel.show();
		}
		else
		{
			finish();
		}
	}*/
	
	// View binder to do formatting of the string values to numbers with commas and parenthesis
/*	static final ViewBinder VIEW_BINDER = new ViewBinder() 
	{
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) 
		{
			LinearLayout row = (LinearLayout) view.getRootView().findViewById(R.id.payeeTransRow);
			
			if( row != null)
			{
				if( cursor.getPosition() % 2 == 0)
					row.setBackgroundColor(Color.rgb(0x62, 0xB1, 0xF6));
				else
					row.setBackgroundColor(Color.rgb(0x62, 0xa1, 0xc6));
			}
			
			switch(view.getId())
			{
			case R.id.ptrDate:
				return false;
			case R.id.ptrAccount:
				return false;
			case R.id.ptrDetails:
				((TextView) view).setText(cursor.getString(columnIndex));
				return true;
			case R.id.ptrAmount:
				// Format the Amount properly.
				((TextView) view).setText(Transaction.convertToDollars(Transaction.convertToPennies(cursor.getString(columnIndex)), true));
				return true;
			default:
				return false;
			}
		}
	};*/
	
	private class TransactionsAdapter extends SimpleCursorAdapter
	{
		Cursor c;
		Context context;
		
		TransactionsAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags)
		{
			super(context, layout, c, from, to, flags);
			this.c = c;
			this.context = context;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if(convertView == null)
				convertView = View.inflate(context, R.layout.payee_transactions_row, null);
			View view = convertView;
			
			LinearLayout row = (LinearLayout) view.findViewById(R.id.payeeTransRow);
			TextView date = (TextView) view.findViewById(R.id.ptrDate);
			TextView account = (TextView) view.findViewById(R.id.ptrAccount);
			TextView details = (TextView) view.findViewById(R.id.ptrDetails);
			TextView amount = (TextView) view.findViewById(R.id.ptrAmount);
			
			if( row != null)
			{
				if( position % 2 == 0)
					row.setBackgroundColor(Color.rgb(0x62, 0xB1, 0xF6));
				else
					row.setBackgroundColor(Color.rgb(0x62, 0xa1, 0xc6));
			}
			this.mCursor.moveToPosition(position);
			date.setText(this.mCursor.getString(this.mCursor.getColumnIndex("postDate")));
			account.setText(this.mCursor.getString(this.mCursor.getColumnIndex("accountName")));
			details.setText(this.mCursor.getString(this.mCursor.getColumnIndex("memo")));
			amount.setText(Transaction.convertToDollars(Transaction.convertToPennies(this.mCursor.getString(this.mCursor.getColumnIndex("valueFormatted"))), true, false));
			
			return view;
		}
	}
	
	public Loader<Cursor> onCreateLoader(int id, Bundle args) 
	{
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_TRANSTAB_URI, frag);
		u = Uri.parse(u.toString());
		String strSelection = null;
		String[] selectionArgs = { null };
		
		// Notify the parent that we need our data.
		sendTransactionTabData();
		
		// See which type of query we need to do.
		if(PayeeId != null)
		{
			// Put the PayeeId into a String array
			strSelection = strSelectionPayee;
			selectionArgs[0] = PayeeId;
		}
		else if(CategoryId != null)
		{
			// Put the CategoryId into a String array
			selectionArgs[0] = CategoryId;
			strSelection = strSelectionCategory;
		}
		return new CursorLoader(getActivity().getBaseContext(), u, dbColumns, strSelection, selectionArgs, strOrderBy);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor transactions) 
	{
		Log.d(TAG, "Transactions returned: " + transactions.getCount());
		adapter.swapCursor(transactions);
	}

	public void onLoaderReset(Loader<Cursor> loader) 
	{
		adapter.swapCursor(null);
	}
	// *******************************************************************************************************
	// *********************************** Public Event Interfaces *******************************************
	public interface OnSendTransactionTabDataListener
	{
		public void onSendTransactionTabData();
	}
	// ************************************************************************************************
	// ******************************* Helper Functions ***********************************************
	public void sendTransactionTabData()
	{
		onSendTransactionTabData.onSendTransactionTabData();
	}
	public void putCategoryInfo(String id, String name)
	{
		this.CategoryId = id;
		this.CategoryName = name;
	}
	
	public void putPayeeInfo(String id, String name)
	{
		this.PayeeId = id;
		this.PayeeName = name;
	}
}
