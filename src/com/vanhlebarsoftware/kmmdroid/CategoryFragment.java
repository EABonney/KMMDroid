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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;


public class CategoryFragment extends Fragment implements
									LoaderManager.LoaderCallbacks<Cursor>
{
	private static final String TAG = CategoryFragment.class.getSimpleName();
	public static final int WITHDRAW_CATEGORY_LOADER = 0x35;
	public static final int TRANSFER_CATEGORY_LOADER = 0x36;
	private OnSplitsClickedListener onSplitsClicked;
	private OnSendWidgetIdListener onSendWidgetId;
	static final int[] TO = { android.R.id.text1 };
	static final String[] FROM = { "accountName" };
	private String strTransCategoryId = null;
	private String widgetId = "9999";
	private boolean isDirty = false;
	private boolean firstRun = true;
	private int loaderType = WITHDRAW_CATEGORY_LOADER;
	Spinner spinCategory;
	Button buttonChooseCategory;
	ImageButton buttonSplits;
	EditText editCategory;
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
			onSplitsClicked = (OnSplitsClickedListener) activity;
		}
		catch(ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement OnSplitsClickedListener");
		}
		
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
		View view = inflater.inflate(R.layout.category_fragment, container, false);
		
		// Find our views.
		spinCategory = (Spinner) view.findViewById(R.id.category);
		editCategory = (EditText) view.findViewById(R.id.editCategory);
        buttonChooseCategory = (Button) view.findViewById(R.id.buttonChooseCategory);
        buttonSplits = (ImageButton) view.findViewById(R.id.buttonSplit);
		textTitle = (TextView) view.findViewById(R.id.titleCategory);
		
        // Make it so the user is not able to edit the Category selected without using the Spinner.
        editCategory.setKeyListener(null);
		
        // Set our OnClickListener events
        buttonChooseCategory.setOnClickListener(new View.OnClickListener()
        {			
			public void onClick(View arg0)
			{
				spinCategory.refreshDrawableState();
				spinCategory.performClick();
			}
		});
        
        buttonSplits.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				onSplitsClicked.onSplitsClicked(strTransCategoryId);
			}
		});
        
        spinCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
    		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    		{
    			if( !firstRun )
    			{
    				Cursor c = (Cursor) parent.getAdapter().getItem(pos);
    				editCategory.setText(c.getString(c.getColumnIndex("accountName")).toString());
    				strTransCategoryId = c.getString(c.getColumnIndex("_id")).toString();
    				isDirty = true;   
    			}
    			else
    				firstRun = false;
    		}

			public void onNothingSelected(AdapterView<?> arg0) 
			{
				// do nothing.
			}
        });

		// Set up the adapter
		adapter = new SimpleCursorAdapter(getActivity().getBaseContext(), android.R.layout.simple_spinner_item, null, FROM, TO, 0);
		spinCategory.setAdapter(adapter);
		
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
		onSendWidgetId.onSendWidgetId(WITHDRAW_CATEGORY_LOADER);
		
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(this.loaderType, null, this);
	}
	
	public Loader<Cursor> onCreateLoader(int id, Bundle args) 
	{
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, "#" + this.widgetId);
		u = Uri.parse(u.toString());

		switch( id )
		{
		case WITHDRAW_CATEGORY_LOADER:
			return new CursorLoader(getActivity().getBaseContext(), u, new String[] { "accountName", "id AS _id" },
					"(accountType=? OR accountType=?)", 
					new String[] { String.valueOf(Account.ACCOUNT_EXPENSE), String.valueOf(Account.ACCOUNT_INCOME) },
					"accountName ASC");
		case TRANSFER_CATEGORY_LOADER:
			return new CursorLoader(getActivity().getBaseContext(), u, new String[] { "accountName", "id AS _id" },
					"(accountType=? OR accountType=?)",
					new String[] { String.valueOf(Account.ACCOUNT_ASSET), String.valueOf(Account.ACCOUNT_LIABILITY) },
					"accountName ASC");
		default:
			return new CursorLoader(getActivity().getBaseContext(), u, new String[] { "accountName", "id AS _id" },
					"(accountType=? OR accountType=?)", 
					new String[] { String.valueOf(Account.ACCOUNT_EXPENSE), String.valueOf(Account.ACCOUNT_INCOME) },
					"accountName ASC");			
		}
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor categories) 
	{
		adapter.swapCursor(categories);
		
		updateUIElements();
	}

	public void onLoaderReset(Loader<Cursor> loader) 
	{
		adapter.swapCursor(null);
	}
	// *******************************************************************************************************
	// *********************************** Public Event Interfaces *******************************************
	public interface OnSplitsClickedListener
	{
		public void onSplitsClicked(String categoryId);
	}
	
	public interface OnSendWidgetIdListener
	{
		public void onSendWidgetId(int fromFragment);
	}
	// **************************************************************************************************
	// ************************************ Helper methods **********************************************
	public void setLoaderType(int type)
	{
		if( !firstRun )
		{
			// First destroy the old loader.
			getLoaderManager().destroyLoader(this.loaderType);
		
			// Restart the loader again with the new type.
			getLoaderManager().initLoader(type, null, this);
			
			// Make sure that our editCategory gets cleared out and stays that way.
			editCategory.setText(null);
			firstRun = true;
		}
		
		// Save the new loaderType for use next time we need to kill it, etc.
		this.loaderType = type;
	}
	
	public void setWidgetId(String wId)
	{
		this.widgetId = wId;
	}
	
	public void setCategoryId(String cId)
	{		
		this.strTransCategoryId = cId;
		
		// refresh the UI
		updateUIElements();
	}
	
	public void setCategoryName(String strName)
	{
		this.editCategory.setText(strName);
		
		// refresh the UI
		updateUIElements();
	}
	
	public String getCategoryId()
	{
		return this.strTransCategoryId;
	}
	
	private void updateUIElements()
	{
		// See if we have splits and if so then display the Split Transaction String
		if( this.strTransCategoryId == null )
		{
			// Do nothing.
		}
		else if( this.strTransCategoryId.equalsIgnoreCase(getString(R.string.splitTransaction)) )
		{
			this.editCategory.setText(this.strTransCategoryId);
			this.buttonChooseCategory.setEnabled(false);
		}
		else
		{
			for(int i=0; i<adapter.getCount();i++)
			{
				Cursor c = (Cursor) adapter.getItem(i);
				if( this.strTransCategoryId.equalsIgnoreCase(c.getString(c.getColumnIndex("_id"))) )
				{
					this.spinCategory.setSelection(i);
					this.editCategory.setText(c.getString(c.getColumnIndex("accountName")));
					break;
				}
			}
		}
	}
}
