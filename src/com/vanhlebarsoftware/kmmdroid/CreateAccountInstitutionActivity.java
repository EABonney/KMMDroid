package com.vanhlebarsoftware.kmmdroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
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

public class CreateAccountInstitutionActivity extends FragmentActivity implements OnClickListener,
	OnCheckedChangeListener
{
	private static final String TAG = "CreateAccountInstitutionActivity";
	private static final int ACTION_NEW = 1;
	static final String[] FROM = { "name" };
	static final int[] TO = { android.R.id.text1 };
	private String institutionSelected = null;
	private String institutionId = null;
	private int columnUsed = 1;
	private int numberOfPasses = 0;
	private CreateModifyAccountActivity parentTabHost;
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
        
        // Get the activity for the tabHost.
        parentTabHost = ((CreateModifyAccountActivity) this.getParent());
        
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
        
        // Set up the other keyListener's for the various editText items.
        accountNumber.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				 parentTabHost.setIsDirty(true);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        accountIBAN.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				 parentTabHost.setIsDirty(true);
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
		spinInstitutions.setSelection(setInstitution(institutionSelected, columnUsed));
	}

	public void onClick(View view) 
	{
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

	public void onCheckedChanged(CompoundButton btn, boolean arg1) 
	{
		switch( btn.getId() )
		{
			case R.id.checkboxNoInstitution:
				// if the user is selecting the checkbox, turn off the Institutions spinner
				if( btn.isChecked() )
				{
					spinInstitutions.setVisibility(View.GONE);
					textInstitutions.setVisibility(View.GONE);
					buttonNewInstitution.setVisibility(View.GONE);
				}
				else
				{
					spinInstitutions.setVisibility(View.VISIBLE);
					textInstitutions.setVisibility(View.VISIBLE);
					buttonNewInstitution.setVisibility(View.VISIBLE);
				}
				break;
		}
		parentTabHost.setIsDirty(true);
	}
	
	public class AccountInstitutionOnItemSelectedListener implements OnItemSelectedListener
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			if( numberOfPasses > 1 )
			{
				Cursor c = (Cursor) parent.getAdapter().getItem(pos);
			
				institutionId = c.getString(0);
				institutionSelected = c.getString(1);
				parentTabHost.setIsDirty(true);
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
	// *********************************** Helper functions *******************************************
	
	private int setInstitution(String institution, int columUsed)
	{
		int i = 0;
		cursorInst.moveToFirst();
		
		if( institution != null )
		{
			while(!institution.equals(cursorInst.getString(columUsed)))
			{
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