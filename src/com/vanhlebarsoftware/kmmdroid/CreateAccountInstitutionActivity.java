package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

public class CreateAccountInstitutionActivity extends Activity
{
	static final String[] FROM = { "name" };
	static final int[] TO = { android.R.id.text1 };
	EditText accountNumber;
	EditText accountIBAN;
	Spinner spinInstitutions;
	Cursor cursorInst;
	SimpleCursorAdapter adapterInst;
	KMMDroidApp KMMDapp;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createaccount_institution);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views
        spinInstitutions = (Spinner) findViewById(R.id.accountInstitution);
        accountNumber = (EditText) findViewById(R.id.accountNumber);
        accountIBAN = (EditText) findViewById(R.id.accountIBAN);
        
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
		cursorInst = KMMDapp.db.query("kmmInstitutions", new String[] { "id AS _id", "name" }, null, null, null, null, null);
		startManagingCursor(cursorInst);
		
		// Set up the adapters
		adapterInst = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursorInst, FROM, TO);
		adapterInst.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinInstitutions.setAdapter(adapterInst);
	}
}
