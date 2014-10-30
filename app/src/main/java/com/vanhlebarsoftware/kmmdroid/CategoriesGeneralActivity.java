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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

public class CategoriesGeneralActivity extends Fragment implements
								LoaderManager.LoaderCallbacks<Cursor>
{
	private static final String TAG = CategoriesGeneralActivity.class.getSimpleName();
	private static final int CATEGORYCURRENCY_LOADER = 0x20;
	static final String[] FROM = { "name" };
	static final int[] TO = { android.R.id.text1 };
	private OnSendGeneralDataListener onSendGeneralData;
	private String strTypeSelected = null;
	private String strCategoryName = null;
	private String strNotes = null;
	private String strCurrency = null;
	private int currencyPos = 0;
	private int categoryTypePos = 0;
	private int categoryType = 0;
	private int numberOfPasses = 0;
	private boolean bNeedUpdateParent = true;
	private Activity ParentActivity;
	EditText editCategoryName;
	EditText editCategoryNotes;
	Spinner spinCategoryType;
	Spinner spinCategoryCurrency;
	TextView txtTotTrans;
	Cursor cursorCurrency;
	SimpleCursorAdapter adapterCurrency;
	ArrayAdapter<CharSequence> adapterTypes;
	//KMMDroidApp KMMDapp;
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		
		// Save our ParentActivity
		ParentActivity = activity;
		
		try
		{
			onSendGeneralData = (OnSendGeneralDataListener) activity;
		}
		catch(ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement OnSendGeneralDataListener");
		}
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
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
        
        View view = inflater.inflate(R.layout.categories_general, container, false);
        
        // Find our views.
        editCategoryName = (EditText) view.findViewById(R.id.categoryName);
        editCategoryNotes = (EditText) view.findViewById(R.id.categoryNotes);
        spinCategoryType = (Spinner) view.findViewById(R.id.categoryType);
        spinCategoryCurrency = (Spinner) view.findViewById(R.id.categoryCurrency);
        txtTotTrans = (TextView) view.findViewById(R.id.titleAccountTransactions);
        
        // Set the OnItemSelectedListeners for the spinners.
        spinCategoryType.setOnItemSelectedListener(new CategoryGeneralOnItemSelectedListener());
        spinCategoryCurrency.setOnItemSelectedListener(new CategoryGeneralOnItemSelectedListener());
        
        // Set up the other keyListener's for the various editText items.
        editCategoryName.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				((CreateModifyCategoriesActivity) ParentActivity).setIsDirty(true);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        editCategoryNotes.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				((CreateModifyCategoriesActivity) ParentActivity).setIsDirty(true);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
		// Set up the adapters
		adapterCurrency = new SimpleCursorAdapter(getActivity().getBaseContext(), android.R.layout.simple_spinner_item, null, FROM, TO, 0);
		adapterCurrency.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinCategoryCurrency.setAdapter(adapterCurrency);
		
		adapterTypes = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.arrayTypes, android.R.layout.simple_spinner_item);
		adapterTypes.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinCategoryType.setAdapter(adapterTypes);
		
		// Prepare the loader. Either re-connect with the existing one,
		// or start a new one.
		getLoaderManager().initLoader(CATEGORYCURRENCY_LOADER, null, this);
		
		// We need to populate the user's default currency.
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
		u = Uri.parse(u.toString());
		Cursor defaultCur = getActivity().getBaseContext().getContentResolver().query(u, new String[] { "baseCurrency" }, null, null, null);
		defaultCur.moveToFirst();
		currencyPos = getCurrencyPos(defaultCur.getString(0));
		defaultCur.close();
		
        return view;
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}
	
	public class CategoryGeneralOnItemSelectedListener implements OnItemSelectedListener
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			if( numberOfPasses > 1 )
			{
				switch(parent.getId())
				{
					case R.id.categoryType:
						categoryTypePos = pos;
						categoryType = getCategoryTypeFromPos(categoryTypePos);
						if( bNeedUpdateParent )
						{
							switch(categoryType)
							{
							case Account.ACCOUNT_EXPENSE:
								((CreateModifyCategoriesActivity) ParentActivity).setParentId("AStd::Expense");
								break;
							case Account.ACCOUNT_INCOME:
								((CreateModifyCategoriesActivity) ParentActivity).setParentId("AStd::Income");
								break;
							}
							((CreateModifyCategoriesActivity) ParentActivity).setCategoryType(categoryType);
							((CreateModifyCategoriesActivity) ParentActivity).setIsParentInvalid(true);
							((CreateModifyCategoriesActivity) ParentActivity).setIsDirty(true);
							((CreateModifyCategoriesActivity) ParentActivity).ReloadHierarchyLoader();
						}
						else
							bNeedUpdateParent = true;
						break;
					case R.id.categoryCurrency:
						Cursor c = (Cursor) parent.getAdapter().getItem(pos);
						strCurrency = c.getString(1);
						((CreateModifyCategoriesActivity) ParentActivity).setIsDirty(true);
						break;
				}
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
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_CURRENCY_URI, frag);
		u = Uri.parse(u.toString());
		String[] dbColumns = { "name", "ISOcode AS _id"};
		String strOrderBy = "name ASC";
		return new CursorLoader(getActivity().getBaseContext(), u, dbColumns, null, null, strOrderBy);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor currencies) 
	{
		adapterCurrency.swapCursor(currencies);
		
		// Notify the parent we need it's data if any.
		sendGeneralData();
		
		// Update our UI
		updateUIElements();
	}

	public void onLoaderReset(Loader<Cursor> loader) 
	{
		adapterCurrency.swapCursor(null);
	}
	// *******************************************************************************************************
	// *********************************** Public Event Interfaces *******************************************
	public interface OnSendGeneralDataListener
	{
		public void onSendGeneralData();
	}
	// ************************************************************************************************
	// ******************************* Helper Functions ***********************************************
	public String getCategoryName()
	{
		return editCategoryName.getText().toString();
	}
	
	public String getCurrency()
	{
		return strCurrency;
	}
	
	public int getCategoryType()
	{
		return getCategoryTypeFromPos(this.categoryTypePos);
	}
	
	public String getNotes()
	{
		return editCategoryNotes.getText().toString();
	}
	
	public int getCategoryTypeFromPos(int pos)
	{
		switch(pos)
		{
		case 0:
			return Account.ACCOUNT_INCOME;
		case 1:
			return Account.ACCOUNT_EXPENSE;
		default:
			return Account.ACCOUNT_INCOME;
		}
	}
	public void putCategoryName(String name)
	{
		this.strCategoryName = name;
	}
	
	public void putCategoryType(int type)
	{
		switch(type)
		{
		case Account.ACCOUNT_EXPENSE:
			this.categoryTypePos = 1;
			break;
		case Account.ACCOUNT_INCOME:
			this.categoryTypePos = 0;
			break;
		}
	}
	
	public void putCurrency(String id)
	{
		strCurrency = id;
	}
	
	public void putNotes(String notes)
	{
		this.strNotes = notes;
	}
	
	public void putTransactionCount(String strCount)
	{
		txtTotTrans.setText(txtTotTrans.getText().toString() + " " + strCount);
	}
	
	private int getCurrencyPos(String id)
	{
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_CURRENCY_URI, frag);
		u = Uri.parse(u.toString());
		String[] dbColumns = { "name", "ISOcode AS id"};
		String strOrderBy = "name ASC";
		int i = 0;

		Cursor c = getActivity().getBaseContext().getContentResolver().query(u, dbColumns, null, null, strOrderBy);
		c.moveToFirst();
		while(!id.equals(c.getString(c.getColumnIndex("id"))))
		{
			c.moveToNext();
			i++;
		}

		// Clean up our cursor.
		c.close();

		return i;
	}
	
	public void sendGeneralData()
	{
		onSendGeneralData.onSendGeneralData();
	}
	
	private void updateUIElements()
	{
		// We need to reset the numberofPasses variable to zero before doing any UI updates.
		this.numberOfPasses = 0;
		
		SharedPreferences prefs = getActivity().getPreferences(Activity.MODE_PRIVATE);
		String Name = prefs.getString("Name", null);
		int TypePos = prefs.getInt("TypePos", -1);
		String Currency = prefs.getString("Currency", null);
		String Notes = prefs.getString("Notes", null);
		bNeedUpdateParent = prefs.getBoolean("needUpdateParent", true);
		
		if( Name != null )
			editCategoryName.setText(Name);
		else
			editCategoryName.setText(strCategoryName);
		
		if( Notes != null )
			editCategoryNotes.setText(Notes);
		else
			editCategoryNotes.setText(strNotes);
		
		// Set the spinner to the correct type.
		if( TypePos != -1 )
			spinCategoryType.setSelection(TypePos);
		else
		{
			if(((CreateModifyCategoriesActivity) ParentActivity).getCategoryType() == Account.ACCOUNT_INCOME)		
				spinCategoryType.setSelection(0);
			else
				spinCategoryType.setSelection(1);
		}
		
		// Set the currency Spinner
		if( Currency != null )
			spinCategoryCurrency.setSelection(getCurrencyPos(Currency));
		else
			spinCategoryCurrency.setSelection(getCurrencyPos(strCurrency));	
		
		this.numberOfPasses = 2;
	}
	
	public Bundle getGeneralBundle()
	{
		Bundle bdl = new Bundle();
		
		bdl.putString("categoryName", strCategoryName);
		bdl.putInt("categoryType", categoryType);
		bdl.putString("categoryCurrency", strCurrency);
		bdl.putString("categoryNotes", strNotes);
		
		return bdl;
	}
}
