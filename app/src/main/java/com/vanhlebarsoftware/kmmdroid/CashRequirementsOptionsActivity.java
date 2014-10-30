package com.vanhlebarsoftware.kmmdroid;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class CashRequirementsOptionsActivity extends FragmentActivity
{
	protected static final String TAG = "CashRequirementsOptionsActivity";
	static final int SET_DATE_BEG = 0;
	static final int SET_DATE_END = 1;
	static final String[] FROM = { "accountName" };
	static final int[] TO = { android.R.id.text1 };
	private int intYear;
	private int intMonth;
	private int intDay;
	private String strAccountId = null;
	private long nAccountBalance = 0;
	EditText editBegDate;
	EditText editEndDate;
	Spinner spinCategory;
	Button buttonSubmit;
	Cursor cursorCategories;
	SimpleCursorAdapter adapterCategories;
	KMMDroidApp KMMDapp;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.cashrequirementsoptions);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views
        spinCategory = (Spinner) findViewById(R.id.Account);
        editBegDate = (EditText) findViewById(R.id.editBegDate);
        editEndDate = (EditText) findViewById(R.id.editEndDate);
        buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
        
        // Make it so the user is not able to edit the Category selected without using the Spinner.
        editBegDate.setKeyListener(null);
        editEndDate.setKeyListener(null);
       
        // Set out onClickListener events.
        buttonSubmit.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				Intent i = new Intent(getBaseContext(), CashRequirementsActivity.class);
				i.putExtra("Account", strAccountId);
				i.putExtra("AccountBalance", nAccountBalance);
				i.putExtra("BegDate", editBegDate.getText().toString());
				i.putExtra("EndDate", editEndDate.getText().toString());
				startActivity(i);
				finish();
			}
		});
        
        editBegDate.setOnClickListener(new View.OnClickListener()
        {
			
			public void onClick(View arg0) {
				showDialog(SET_DATE_BEG);
			}
		});
        
        editEndDate.setOnClickListener(new View.OnClickListener()
        {
			
			public void onClick(View arg0) {
				showDialog(SET_DATE_END);
			}
		});
        
        // Set the OnItemSelectedListeners for the spinners.
        spinCategory.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        
        // get the current date
        final Calendar c = Calendar.getInstance();
        intYear = c.get(Calendar.YEAR);
        intMonth = c.get(Calendar.MONTH);
        intDay = c.get(Calendar.DAY_OF_MONTH);
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
        // Update the date for the defaulted Beginning date (today's date).
        updateDisplay(SET_DATE_BEG);
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
		
		cursorCategories = KMMDapp.db.query("kmmAccounts", new String[] { "accountName", "id AS _id", "balanceFormatted" },
				"(accountTypeString='Checking' OR accountTypeString='Savings' OR accountTypeString='Liability' OR " +
				"accountTypeString='Credit Card') AND (balance != '0/1')", null, null, null, "accountName ASC");
		startManagingCursor(cursorCategories);
		
		// Set up the adapters
		adapterCategories = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursorCategories, FROM, TO);
		adapterCategories.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinCategory.setAdapter(adapterCategories);
	}
	
	// the callback received with the user "sets" the opening date in the dialog
	private DatePickerDialog.OnDateSetListener mDateSetListenerBeg = 
			new DatePickerDialog.OnDateSetListener() 
			{				
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) 
				{
					intYear = year;
					intMonth = monthOfYear;
					intDay = dayOfMonth;
					updateDisplay(SET_DATE_BEG);
				}
			};
			
			private DatePickerDialog.OnDateSetListener mDateSetListenerEnd = 
					new DatePickerDialog.OnDateSetListener() 
					{				
						public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) 
						{
							intYear = year;
							intMonth = monthOfYear;
							intDay = dayOfMonth;
							updateDisplay(SET_DATE_END);
						}
					};
			
	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch(id)
		{
			case SET_DATE_BEG:
				return new DatePickerDialog(this, mDateSetListenerBeg, intYear, intMonth, intDay);
			case SET_DATE_END:
				return new DatePickerDialog(this, mDateSetListenerEnd, intYear, intMonth, intDay);
		}
		return null;
	}
	
	public class AccountOnItemSelectedListener implements OnItemSelectedListener
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			switch( parent.getId())
			{
				case R.id.Account:
					Cursor c = (Cursor) parent.getAdapter().getItem(pos);
					strAccountId = c.getString(1).toString();
					nAccountBalance = convertToPennies(c.getString(2).toString());
					break;
				default:
					break;
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// do nothing.
		}		
	}
	
	// **************************************************************************************************
	// ************************************ Helper methods **********************************************
	private void updateDisplay(int date)
	{
		switch(date)
		{
		case SET_DATE_BEG:
			editBegDate.setText(
					new StringBuilder()
						// Month is 0 based so add 1
						.append(intMonth + 1).append("-")
						.append(intDay).append("-")
						.append(intYear));
			break;
		case SET_DATE_END:
			editEndDate.setText(
					new StringBuilder()
						// Month is 0 based so add 1
						.append(intMonth + 1).append("-")
						.append(intDay).append("-")
						.append(intYear));
			break;
		}
	}
	
	/********************************************************************************************
	* Adapted from code found at currency : Java Glossary
	* website: http://mindprod.com/jgloss/currency.html
	********************************************************************************************/
	private long convertToPennies(String numStr)
	{
		numStr = numStr.trim ();
		// strip commas, spaces, + etc
		StringBuffer b = new StringBuffer( numStr.length() );
		boolean negative = false;
		int decpl = -1;
		for ( int i=0; i<numStr.length(); i++ )
		{
			char c = numStr.charAt( i );
		    switch ( c )
		    {
		    	case '-' :
		    		negative = true;
		            break;
		        case '.' :
		        	if ( decpl == -1 )
		            {
		               decpl = 0;
		            }
		            else
		            {
		               throw new NumberFormatException( "more than one decimal point" );
		            }
		            break;
		        case '0' :
		        case '1' :
		        case '2' :
		        case '3' :
		        case '4' :
		        case '5' :
		        case '6' :
		        case '7' :
		        case '8' :
		        case '9' :
		        	if ( decpl != -1 )
		            {
		               decpl++;
		            }
		            b.append(c);
		            break;
		        default:
		        	// ignore junk chars
		            break;
		    }
		    // end switch
		}
		// end for
		if ( numStr.length() != b.length() )
		{
			numStr = b.toString();
		}
		if ( numStr.length() == 0 )
		{
			return 0;
		}
		long num = Long.parseLong( numStr );
		if ( decpl == -1 || decpl == 0 )
		{
			num *= 100;
		}
		else if ( decpl == 1 )
		{
			num *= 10;
		}
		else if ( decpl == 2 )
		{
			/* it is fine as is */
		}
		else
		{
			throw new NumberFormatException( "wrong number of decimal places." );
		}
		if ( negative )
		{
			num = -num;
		}
		return num;
	}

}
