package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class CreateAccountInstitutionActivity extends Activity implements OnClickListener,
	OnCheckedChangeListener
{
	private static final String TAG = "CreateAccountInstitutionActivity";
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	static final String[] FROM = { "name" };
	static final int[] TO = { android.R.id.text1 };
	private String institutionSelected = null;
	private String institutionId = null;
	private int columnUsed = 1;
	EditText accountNumber;
	EditText accountIBAN;
	Spinner spinInstitutions;
	Button buttonNewInstitution;
	CheckBox checkboxNoInstitution;
	TextView textInstitutions;
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
        buttonNewInstitution = (Button) findViewById(R.id.buttonNewInstitution);
        checkboxNoInstitution = (CheckBox) findViewById(R.id.checkboxNoInstitution);
        textInstitutions = (TextView) findViewById(R.id.titleAccountInstitution);
        
        // Set our listeners for our items.
        buttonNewInstitution.setOnClickListener(this);
        checkboxNoInstitution.setOnCheckedChangeListener(this);
        spinInstitutions.setOnItemSelectedListener(new AccountInstitutionOnItemSelectedListener());
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
        // Make sure we start by deafult with no Institutions showing.
        checkboxNoInstitution.setChecked(true);
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
		cursorInst = KMMDapp.db.query("kmmInstitutions", new String[] { "id AS _id", "name" }, null, null,
				null, null, "name ASC");
		startManagingCursor(cursorInst);
		
		// Set up the adapters
		adapterInst = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursorInst, FROM, TO);
		adapterInst.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinInstitutions.setAdapter(adapterInst);
		
		// Set the Institutions spinner to the proper location or default of zero.
		Log.d(TAG, "institutionSelected: " + institutionSelected);
		spinInstitutions.setSelection(setInstitution(institutionSelected, columnUsed));
	}

	public void onClick(View view) 
	{
		// TODO Auto-generated method stub
		switch(view.getId())
		{
			case R.id.buttonNewInstitution:
				Intent i = new Intent(getBaseContext(), CreateModifyInstitutionActivity.class);
				i.putExtra("Action", ACTION_NEW);
				startActivity(i);
				// Since we are adding a new Institution, make sure we have the spinner displayed.
				checkboxNoInstitution.setChecked(false);
				spinInstitutions.setVisibility(0);
				textInstitutions.setVisibility(0);				
				break;
		}
	}

	public void onCheckedChanged(CompoundButton btn, boolean arg1) {
		// TODO Auto-generated method stub
		switch( btn.getId() )
		{
			case R.id.checkboxNoInstitution:
				// if the user is selecting the checkbox, turn off the Institutions spinner
				if( btn.isChecked() )
				{
					spinInstitutions.setVisibility(8);
					textInstitutions.setVisibility(8);
				}
				else
				{
					spinInstitutions.setVisibility(0);
					textInstitutions.setVisibility(0);
				}
				break;
		}
	}
	
	public class AccountInstitutionOnItemSelectedListener implements OnItemSelectedListener
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			Cursor c = (Cursor) parent.getAdapter().getItem(pos);
			
			// TODO Auto-generated method stub
			institutionId = c.getString(0);
			institutionSelected = c.getString(1);
			Log.d(TAG, "institutionSelected: " + institutionSelected);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			// do nothing.
		}		
	}
	
	// ************************************************************************************************
	// *********************************** Helper functions *******************************************
	
	private int setInstitution(String institution, int columUsed)
	{
		int i = 0;
		cursorInst.moveToFirst();
		
		if( institution != null )
		{
			while(!institution.equals(cursorInst.getString(columUsed)))
			{
				Log.d(TAG, "name: " + institution + " cursorName: " + cursorInst.getString(columUsed));
				cursorInst.moveToNext();
			
				//check to see if we have moved past the last item in the cursor, if so return current i.
				if(cursorInst.isAfterLast())
					return i;
			
				i++;
			}
		}
		
		// Always set the columnUsed back to using the Name, columnUsed = 1
		columnUsed = 1;
		return i;
	}
	
	public boolean getUseInstitution()
	{
		boolean checked = checkboxNoInstitution.isChecked();
		// Since we are saying true means we are NOT using an institution, we need to return the opposite.
		return !checked;
	}
	
	public String getInstitutionId()
	{
		String id = institutionId;
		
		return id;
	}
	
	public String getAccountNumber()
	{
		String acctNumber = accountNumber.getText().toString();
		
		return acctNumber;
	}
	
	public String getIBAN()
	{
		String iban = accountIBAN.getText().toString();
		
		return iban;
	}
	
	public void putUseInstitution(boolean useInst)
	{
		checkboxNoInstitution.setChecked(useInst);
	}
	
	public void putInstitutionId(String id)
	{
		institutionSelected = id;
		columnUsed = 0;
	}
	
	public void putAccountNumber(String ACNumber)
	{
		accountNumber.setText(ACNumber);
	}
	
	public void putIBAN(String iban)
	{
		accountIBAN.setText(iban);
	}
}