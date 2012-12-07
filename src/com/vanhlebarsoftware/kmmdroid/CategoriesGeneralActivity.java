package com.vanhlebarsoftware.kmmdroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

public class CategoriesGeneralActivity extends FragmentActivity
{
	private static final String TAG = "CategoriesGeneralActivity";
	private static final String[] dbColumns = { "name", "ISOcode AS _id"};
	private static final String strOrderBy = "name ASC";
	static final String[] FROM = { "name" };
	static final int[] TO = { android.R.id.text1 };
	private static int currencyPos = 0;
	private static int categoryTypePos = 0;
	String strCurrency = null;
	private int numberOfPasses = 0;
	private CreateModifyCategoriesActivity parentTabHost;
	EditText editCategoryName;
	EditText editCategoryNotes;
	Spinner spinCategoryType;
	Spinner spinCategoryCurrency;
	TextView txtTotTrans;
	Cursor cursorCurrency;
	SimpleCursorAdapter adapterCurrency;
	ArrayAdapter<CharSequence> adapterTypes;
	KMMDroidApp KMMDapp;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.categories_general);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Get the tabHost on the parent.
        parentTabHost = ((CreateModifyCategoriesActivity) this.getParent());
        
        // Find our views.
        editCategoryName = (EditText) findViewById(R.id.categoryName);
        editCategoryNotes = (EditText) findViewById(R.id.categoryNotes);
        spinCategoryType = (Spinner) findViewById(R.id.categoryType);
        spinCategoryCurrency = (Spinner) findViewById(R.id.categoryCurrency);
        txtTotTrans = (TextView) findViewById(R.id.titleAccountTransactions);
        
        // Set the OnItemSelectedListeners for the spinners.
        spinCategoryType.setOnItemSelectedListener(new CategoryGeneralOnItemSelectedListener());
        spinCategoryCurrency.setOnItemSelectedListener(new CategoryGeneralOnItemSelectedListener());
        
        // Set up the other keyListener's for the various editText items.
        editCategoryName.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				parentTabHost.setIsDirty(true);
				Log.d(TAG, "changing isDirty from accountName!");
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
				parentTabHost.setIsDirty(true);
				Log.d(TAG, "changing isDirty from accountName!");
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
		// We need to populate the user's default currency.
		Cursor defaultCur = KMMDapp.db.query("kmmFileInfo", new String[] { "baseCurrency" }, null, null, null, null, null);
		defaultCur.moveToFirst();
		currencyPos = getCurrencyPos(defaultCur.getString(0));
		defaultCur.close();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		//Get all the currencies to be displayed.
		cursorCurrency = KMMDapp.db.query("kmmCurrencies", dbColumns, null, null, null, null, strOrderBy);
		startManagingCursor(cursorCurrency);
		
		// Set up the adapters
		adapterCurrency = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursorCurrency, FROM, TO);
		adapterCurrency.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		adapterTypes = ArrayAdapter.createFromResource(this, R.array.arrayTypes, android.R.layout.simple_spinner_item);
		adapterTypes.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		
		spinCategoryCurrency.setAdapter(adapterCurrency);
		spinCategoryType.setAdapter(adapterTypes);
		
		// Set the spinner to the correct type.
		if(CreateModifyCategoriesActivity.strType == null)		
			spinCategoryType.setSelection(0);
		else if(CreateModifyCategoriesActivity.strType.equalsIgnoreCase("Income"))
			spinCategoryType.setSelection(0);
		else
			spinCategoryType.setSelection(1);
		
		// Set the currency Spinner
		spinCategoryCurrency.setSelection(currencyPos);
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
						CreateModifyCategoriesActivity.strType = parent.getItemAtPosition(pos).toString();
						CreateModifyCategoriesActivity.inValidateParentId = true;
						parentTabHost.setIsDirty(true);
						break;
					case R.id.categoryCurrency:
						Cursor c = (Cursor) parent.getAdapter().getItem(pos);
						strCurrency = c.getString(1);
						parentTabHost.setIsDirty(true);
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
	
	@Override
	public void onBackPressed()
	{
		Log.d(TAG, "User clicked the back button");
		if( parentTabHost.getIsDirty() )
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
					Log.d(TAG, "User cancelled back action.");
				}
			});				
			alertDel.show();
		}
		else
		{
			finish();
		}
	}
	
	// ************************************************************************************************
	// ******************************* Helper Functions ***********************************************
	public String getCategoryName()
	{
		return editCategoryName.getText().toString();
	}
	
	public String getCategoryCurrency()
	{
		return strCurrency;
	}
	

	public String getNotes()
	{
		return editCategoryNotes.getText().toString();
	}
	
	public void putCategoryName(String name)
	{
		editCategoryName.setText(name);
	}
	
	public void putCategoryType(int pos)
	{
		categoryTypePos = pos;
		CreateModifyCategoriesActivity.strType = spinCategoryType.getItemAtPosition(pos).toString();
		CreateModifyCategoriesActivity.inValidateParentId = true;
	}
	
	public void putCurrency(int pos)
	{
		currencyPos = pos;
	}
	
	public void putNotes(String notes)
	{
		editCategoryNotes.setText(notes);
	}
	
	public void putTransactionCount(String strCount)
	{
		txtTotTrans.setText(txtTotTrans.getText().toString() + " " + strCount);
	}
	
	private int getCurrencyPos(String id)
	{
		int i = 0;
		
		Cursor c = KMMDapp.db.query("kmmCurrencies", new String[] { "name", "ISOcode" },
									null, null,	null, null, "name ASC");
		startManagingCursor(c);
		c.moveToFirst();
		
		if(c.getCount() > 0)
		{
			while(!id.equals(c.getString(1)))
			{
				c.moveToNext();
				i++;
			}
		}
		else
		{
			Log.d(TAG, "getCurrencyPos query returned no accounts!");
			i = 0;
		}
		
		return i;
	}
}
