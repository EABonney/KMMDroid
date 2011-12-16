package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

public class CategoriesGeneralActivity extends Activity
{
	private static final String TAG = "CategoriesGeneralActivity";
	private static final String[] dbColumns = { "name", "ISOcode AS _id"};
	private static final String strOrderBy = "name ASC";
	static final String[] FROM = { "name" };
	static final int[] TO = { android.R.id.text1 };
	private static final int AC_EXPENSE = 13;
	private static final int AC_INCOME = 12;
	private static int currencyPos = 0;
	private static int categoryTypePos = 0;
	String strCurrency = null;
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
        
        // Find our views.
        editCategoryName = (EditText) findViewById(R.id.categoryName);
        editCategoryNotes = (EditText) findViewById(R.id.categoryNotes);
        spinCategoryType = (Spinner) findViewById(R.id.categoryType);
        spinCategoryCurrency = (Spinner) findViewById(R.id.categoryCurrency);
        txtTotTrans = (TextView) findViewById(R.id.titleAccountTransactions);
        
        // Set the OnItemSelectedListeners for the spinners.
        spinCategoryType.setOnItemSelectedListener(new CategoryGeneralOnItemSelectedListener());
        spinCategoryCurrency.setOnItemSelectedListener(new CategoryGeneralOnItemSelectedListener());
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
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
			switch(parent.getId())
			{
				case R.id.categoryType:
					CreateModifyCategoriesActivity.strType = parent.getItemAtPosition(pos).toString();
					CreateModifyCategoriesActivity.inValidateParentId = true;
					break;
				case R.id.categoryCurrency:
					Cursor c = (Cursor) parent.getAdapter().getItem(pos);
					strCurrency = c.getString(1);
					break;
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			// do nothing.
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
}
