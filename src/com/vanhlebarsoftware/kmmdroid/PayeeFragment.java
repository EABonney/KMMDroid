package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.TextView;

public class PayeeFragment extends Fragment implements
									LoaderManager.LoaderCallbacks<Cursor>
{
	private static final String TAG = PayeeFragment.class.getSimpleName();
	public static final int PAYEE_LOADER = 0x30;
	private OnSendWidgetIdListener onSendWidgetId;
	static final String[] FROM = { "name" };
	static final int[] TO = { android.R.id.text1 };
	private String widgetId = "9999";
	private String payeeId = null;
	private boolean firstRun = true;
	private boolean isDirty = false;
	Spinner spinPayee;
	TextView textTitle;
	SimpleCursorAdapter adapter;
	
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
		View view = inflater.inflate(R.layout.fragment_payee, container, false);
		
		// Find our views.
		spinPayee = (Spinner) view.findViewById(R.id.payee);
		textTitle = (TextView) view.findViewById(R.id.titlePayeeToFrom);
		
		// Set up the adapter
		adapter = new SimpleCursorAdapter(getActivity().getBaseContext(), android.R.layout.simple_spinner_item, null, FROM, TO, 0);
		spinPayee.setAdapter(adapter);
        
        spinPayee.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
    		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    		{
    			if( !firstRun )
    			{
    				Cursor c = (Cursor) parent.getAdapter().getItem(pos);
    				payeeId = c.getString(c.getColumnIndex("_id")).toString();
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

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		
		// Get the widgetId that we need from the parent.
		onSendWidgetId.onSendWidgetId(PAYEE_LOADER);
		
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(PAYEE_LOADER, null, this);
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) 
	{
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_PAYEE_URI,"#" + this.widgetId);
		u = Uri.parse(u.toString());
		
		return new CursorLoader(getActivity().getBaseContext(), u, new String[] { "name", "id AS _id" }, null, null, "name ASC");
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor payees) 
	{
		adapter.swapCursor(payees);
		
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
	
	public void setPayeeId(String pId)
	{
		this.payeeId = pId;
		
		updateUIElements();
	}
	
	public String getPayeeId()
	{
		return this.payeeId;
	}
	
	private void updateUIElements()
	{
		if( this.payeeId != null )
		{
			for(int i=0; i<adapter.getCount();i++)
			{
				Cursor c = (Cursor) adapter.getItem(i);
				if( this.payeeId.equalsIgnoreCase(c.getString(c.getColumnIndex("_id"))) )
				{
					this.spinPayee.setSelection(i);
					break;
				}
			}		
		}
	}
}
