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

public class CategoriesHierarchyActivity extends Fragment implements
								LoaderManager.LoaderCallbacks<Cursor>
{
	private static final String TAG = CategoriesGeneralActivity.class.getSimpleName();
	private static final int CATEGORYACCOUNTS_LOADER = 0x21;
	static final String[] FROM = { "accountName" };
	static final int[] TO = { android.R.id.text1 };
	private OnSendHierarchyDataListener onSendHierarchyData;
	String strAccountType = null;
	String strParentAccount = null;
	int parentId = 0;
	private int numberOfPasses = 0;
	private Activity ParentActivity;
	Spinner spinParent;
	Cursor cursor;
	SimpleCursorAdapter adapter;
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) 
	{
		super.onAttach(activity);
		
		// Save our parent activity.
		ParentActivity = activity;
		
		try
		{
			onSendHierarchyData = (OnSendHierarchyDataListener) activity;
		}
		catch(ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement OnSendHierarchyDataListener");
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

		View view = inflater.inflate(R.layout.categories_hierarchy, container, false);
		
        // Find our views
        spinParent = (Spinner) view.findViewById(R.id.categorySubAccount);
        
        // Set the OnItemSelectedListeners for the spinners.
        spinParent.setOnItemSelectedListener(new CategoryHierarchyOnItemSelectedListener());
        
		// Set up the adapters
		adapter = new SimpleCursorAdapter(getActivity().getBaseContext(), android.R.layout.simple_spinner_item, null, FROM, TO, 0);
		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinParent.setAdapter(adapter);
		
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(CATEGORYACCOUNTS_LOADER, null, this);
		
        return view;
	}
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);        
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
	}
	

	public class CategoryHierarchyOnItemSelectedListener implements OnItemSelectedListener
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			if( numberOfPasses > 0 )
			{
				Cursor c = (Cursor) parent.getAdapter().getItem(pos);
				strParentAccount = c.getString(1);
				((CreateModifyCategoriesActivity) ParentActivity).setParentId(strParentAccount);
				((CreateModifyCategoriesActivity) ParentActivity).setIsParentInvalid(false);
				((CreateModifyCategoriesActivity) ParentActivity).setIsDirty(true);
			}
			else
				numberOfPasses++;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// do nothing.
		}		
	}
	
	public Loader<Cursor> onCreateLoader(int id, Bundle args) 
	{
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, frag);
		u = Uri.parse(u.toString());
		String[] dbColumns = { "accountName", "id AS _id"};
		String strSelectionType = "accountTypeString=?";
		String strOrderBy = "accountName ASC";
		strAccountType = getTypeStringFromType(((CreateModifyCategoriesActivity) ParentActivity).getCategoryType());
		return new CursorLoader(getActivity().getBaseContext(), u, dbColumns, strSelectionType, new String[] { strAccountType }, strOrderBy);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor accounts) 
	{
		adapter.swapCursor(accounts);
		
		// Notify the parent activity to send it's data if any.
		sendHierarchyData();
		
		// Update our UI
		updateUIElements();
	}

	public void onLoaderReset(Loader<Cursor> loader) 
	{
		adapter.swapCursor(null);
	}
	// *******************************************************************************************************
	// *********************************** Public Event Interfaces *******************************************
	public interface OnSendHierarchyDataListener
	{
		public void onSendHierarchyData();
	}
	// ************************************************************************************************
	// ******************************* Helper Functions ***********************************************
	private int setParentItem(String type, int columnCompare)
	{
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, frag);
		u = Uri.parse(u.toString());
		String[] dbColumns = { "accountName", "id AS _id"};
		String strSelectionType = "accountTypeString=?";
		String strOrderBy = "accountName ASC";
		String strType = getTypeStringFromType(((CreateModifyCategoriesActivity) ParentActivity).getCategoryType());
		int i = 0;
		
		Cursor c = getActivity().getBaseContext().getContentResolver().query(u, dbColumns, strSelectionType, new String[] { strType }, strOrderBy);
		c.moveToFirst();
		
		while(!c.isAfterLast())
		{
			if(type.equals(c.getString(columnCompare)))
			{
				// Clean up our cursor
				c.close();				
				return i;
			}
			else
			{
				c.moveToNext();
				i++;
			}
		}
		
		// Clean up our cursor
		c.close();
		
		// Return -1 if we didn't find a match.
		return -1;
	}
	
	private String getTypeStringFromType(int type)
	{
		switch(type)
		{
		case Account.ACCOUNT_EXPENSE:
			return getString(R.string.Expense);
		case Account.ACCOUNT_INCOME:
		default:
			return getString(R.string.Income);
		}
	}
	public String getParentAccount()
	{
		return strParentAccount;
	}
	
	public void putParentAccount(String id)
	{
		strParentAccount = id;
	}
	
	public void sendHierarchyData()
	{
		onSendHierarchyData.onSendHierarchyData();
	}
	
	public void reloadLoader()
	{
		getLoaderManager().restartLoader(CATEGORYACCOUNTS_LOADER, null, this);
	}
	
	private void updateUIElements()
	{
		SharedPreferences prefs = getActivity().getPreferences(Activity.MODE_PRIVATE);
		String parentId = prefs.getString("Parent", null);
		int type = ((CreateModifyCategoriesActivity) ParentActivity).getCategoryType();
		String strType = getTypeStringFromType(type);
				
		if( parentId != null )
		{
			// See if our previous parentId is in our cursor, if not then we will set it to whatever the
			// parent type is set at.
			int pos = setParentItem(parentId, 1);
			
			if( pos == -1 )
				spinParent.setSelection(setParentItem(strType, 0));
			else
				spinParent.setSelection(pos);
		}
		else
		{
			strParentAccount = ((CreateModifyCategoriesActivity) ParentActivity).getParentId();
			spinParent.setSelection(setParentItem(strParentAccount, 1));
		}
	}
	
	public Bundle getHierarchyBundle()
	{
		Bundle bld = new Bundle();
		
		bld.putString("parentId", strParentAccount);
		
		return bld;
	}
}
