package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class CreateAccountParentActivity extends Fragment implements
LoaderManager.LoaderCallbacks<Cursor>
{
	private final String TAG = CreateAccountParentActivity.class.getSimpleName();
	private final static int PARENT_LOADER = 0x12;
	private final static String[] FROM = { "accountName" };
	private final static int[] TO = { android.R.id.text1 };
	private OnSendParentDataListener onSendParentData;
	private Activity ParentActivity;
	private String strParentId = "AStd::Asset";
	Spinner spinParent;
	SimpleCursorAdapter adapter;
	private boolean firstRun = true;
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) 
	{
		super.onAttach(activity);
		
		// Save our parent activity.
		ParentActivity = activity;
		
		Log.d(TAG, "onAttach()");
		try
		{
			onSendParentData = (OnSendParentDataListener) activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + "must implment OnSendParentListener");
		}
	}
	
	@Override
	public void onCreate(Bundle savedState)
	{
		super.onCreate(savedState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.createaccount_parent, container, false);
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
        
        // Find our views
        spinParent = (Spinner) view.findViewById(R.id.accountSubAccount);
        
        // Set our listeners for our items.
        spinParent.setOnItemSelectedListener(new AccountParentOnItemSelectedListener());
        
		// Set up the adapters
		adapter = new SimpleCursorAdapter(getActivity().getBaseContext(), android.R.layout.simple_spinner_item, null, FROM, TO, 0);
		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinParent.setAdapter(adapter);
		
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(PARENT_LOADER, null, this);
        
        Log.d(TAG, "Inside onCreateView()");
        return view;
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
	}
	
	public class AccountParentOnItemSelectedListener implements OnItemSelectedListener
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			if( !firstRun )
			{
				Cursor c = (Cursor) parent.getAdapter().getItem(pos);			
				strParentId = c.getString(0);
				Log.d(TAG, "parentId: " + strParentId);
				((CreateModifyAccountActivity) ParentActivity).setIsParentDirty(true);
			}
			else
				firstRun = false;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// do nothing.
		}		
	}
	
	public Loader<Cursor> onCreateLoader(int id, Bundle args)
	{
		String dbColumns[] = { "id AS _id", "accountName" };
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, frag);
		u = Uri.parse(u.toString());

		return new CursorLoader(getActivity().getBaseContext(), u, dbColumns, null, null, null);
	}
	
	public void onLoadFinished(Loader<Cursor> loader, Cursor accounts)
	{
		Log.d(TAG, "loader is done and we have our cursor");
		adapter.swapCursor(accounts);
		
		// Set the initial value of strParentId
		Cursor c = (Cursor) adapter.getItem(0);
		c.moveToFirst();
		strParentId = c.getString(c.getColumnIndex("_id"));
		
		// Notify the ParentActivity to send us the Parent data.
		sendParentData();
		
		updateUIElements();
	}
	
	public void onLoaderReset(Loader<Cursor> loader) 
	{
		adapter.swapCursor(null);
	}
	// *******************************************************************************************************
	// *********************************** Public Event Interfaces *******************************************
	public interface OnSendParentDataListener
	{
		public void onSendParentData();
	}
	// ***********************************************************************************************
	// ********************************* Helper Functions ********************************************
	private int setParent(String parentId)
	{
		int i = 0;
		Cursor c = adapter.getCursor();
		c.moveToFirst();
		
		if( parentId != null )
		{
			while(!parentId.equals(c.getString(0)))
			{
				c.moveToNext();
				//check to see if we have moved past the last item in the cursor, if so return current i.
				if(c.isAfterLast())
					return i;
			
				i++;
			}
		}
		return i;
	}
	
	public String getParentId()
	{
		return strParentId;
	}
	
	public void putParentId(String id)
	{
		strParentId = id;
	}
	
	public void sendParentData()
	{
		Log.d(TAG, "Asking for data from the parent...");
		onSendParentData.onSendParentData();
	}
	
	private void updateUIElements()
	{
		Log.d(TAG, "Updating UI");
		SharedPreferences prefs = getActivity().getPreferences(Activity.MODE_PRIVATE);
		String id = prefs.getString("ParentId", "");
		
		if( !id.isEmpty() )
			putParentId(prefs.getString("ParentId", ""));
		
		spinParent.setSelection(setParent(this.strParentId));
	}
	
	public Bundle getParentBundle()
	{
		Bundle bndlParent = new Bundle();
		
		bndlParent.putString("parentId", this.getParentId());
		
		return bndlParent;
	}
}
