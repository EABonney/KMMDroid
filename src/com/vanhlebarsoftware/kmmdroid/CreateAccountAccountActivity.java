package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

public class CreateAccountAccountActivity extends Activity
{
	static final String[] FROM = { "name" };
	static final int[] TO = { android.R.id.text1 };
	EditText accountName;
	EditText openDate;
	EditText openBalance;
	Spinner spinType;
	Spinner spinCurrency;
	CheckBox checkPreferred;
	Cursor cursorCurrency;
	SimpleCursorAdapter adapterCurrency;
	ArrayAdapter<CharSequence> adapterTypes;
	KMMDroidApp KMMDapp;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createaccount_account);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views
        spinCurrency = (Spinner) findViewById(R.id.accountCurrency);
        spinType = (Spinner) findViewById(R.id.accountType);
        accountName = (EditText) findViewById(R.id.accountName);
        openDate = (EditText) findViewById(R.id.accountOpenDate);
        openBalance = (EditText) findViewById(R.id.accountOpenBalance);
        checkPreferred = (CheckBox) findViewById(R.id.checkboxAccountPreferred);
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		
		//Get all the currencies to be displayed.
		cursorCurrency = KMMDapp.db.query("kmmCurrencies", new String[] { "ISOCode AS _id", "name" }, null, null, null, null, null);
		startManagingCursor(cursorCurrency);
		
		// Set up the adapters
		adapterCurrency = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursorCurrency, FROM, TO);
		adapterCurrency.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinCurrency.setAdapter(adapterCurrency);
		adapterTypes = ArrayAdapter.createFromResource(this, R.array.arrayAccountTypes, android.R.layout.simple_spinner_item);
		adapterTypes.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinType.setAdapter(adapterTypes);
	}
}
