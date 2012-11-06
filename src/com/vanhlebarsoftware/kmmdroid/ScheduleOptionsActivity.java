package com.vanhlebarsoftware.kmmdroid;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class ScheduleOptionsActivity extends Activity implements OnCheckedChangeListener
{
	private static final String TAG = ScheduleOptionsActivity.class.getSimpleName();
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private static final int MOVE_BEFORE = 0;
	private static final int MOVE_AFTER = 1;
	private static final int MOVE_NOTHING = 2;
	static final int SET_DATE_ID = 0;
	private int Action = 0;
	private int intYear;
	private int intMonth;
	private int intDay;
	private int intWeekendOption = MOVE_NOTHING;
	private int numberOfPasses = 0;
	private CreateModifyScheduleActivity parentTabHost;
	Spinner spinWeekendOptions;
	CheckBox ckboxEstimate;
	CheckBox ckboxAutoEnter;
	CheckBox ckboxScheduleEnds;
	EditText editEndDate;
	EditText editNumTransactions;
	TextView textRemainingTrans;
	TextView textEndDate;
	ImageButton btnSelectDate;
	
	ArrayAdapter<CharSequence> adapterWeekendOption;
	KMMDroidApp KMMDapp;
	Cursor cursor;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_options);
        
        // Get the tabHost on the parent.
        parentTabHost = ((CreateModifyScheduleActivity) this.getParent());
        
        // Find our views
        spinWeekendOptions = (Spinner) findViewById(R.id.scheduleWeekendOption);
        ckboxEstimate = (CheckBox) findViewById(R.id.checkboxEstimate);
        ckboxAutoEnter = (CheckBox) findViewById(R.id.checkboxAutoEnter);
        ckboxScheduleEnds = (CheckBox) findViewById(R.id.checkboxEnd);
        editEndDate = (EditText) findViewById(R.id.endDate);
        editNumTransactions = (EditText) findViewById(R.id.scheduleNumTransactions);
        btnSelectDate = (ImageButton) findViewById(R.id.buttonEndDate);
        textEndDate = (TextView) findViewById(R.id.titleScheduleEndDate);
        textRemainingTrans = (TextView) findViewById(R.id.titleScheduleNumTransactions);
        
        // Set our OnClickListener events
        btnSelectDate.setOnClickListener(new View.OnClickListener() 
        {	
			public void onClick(View v)
			{
				showDialog(SET_DATE_ID);
				parentTabHost.setIsDirty(true);
			}
		});
        
        // Set the OnItemSelectedListeners for the spinners.
        spinWeekendOptions.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        
        // Hook into our onClickListener Events for the checkboxes.
        ckboxScheduleEnds.setOnCheckedChangeListener(this);
        ckboxEstimate.setOnCheckedChangeListener(this);
        ckboxAutoEnter.setOnCheckedChangeListener(this);
        
        // Set up the other keyListener's for the various editText items.
        editNumTransactions.addTextChangedListener(new TextWatcher()
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
        
        // Set the Number of Transactions, End Date and Select Date items as disabled at the start.
        editEndDate.setEnabled(false);
        editNumTransactions.setEnabled(false);
        btnSelectDate.setEnabled(false);
        textEndDate.setEnabled(false);
        textRemainingTrans.setEnabled(false);
        
        // Make it so the user is not able to edit the date selected without using the Spinner.
        editEndDate.setKeyListener(null);
        
        // get the current date
        final Calendar c = Calendar.getInstance();
        intYear = c.get(Calendar.YEAR);
        intMonth = c.get(Calendar.MONTH);
        intDay = c.get(Calendar.DAY_OF_MONTH);
        
        // display the current date
        updateDisplay();
	}
	@Override
	protected void onDestroy() 
	{
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	@Override
	protected void onResume() 
	{
		super.onResume();
	
		adapterWeekendOption = ArrayAdapter.createFromResource(this, R.array.scheduleWeekendOptions, android.R.layout.simple_spinner_item);
		adapterWeekendOption.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinWeekendOptions.setAdapter(adapterWeekendOption);
		
		// The spinner's value
		spinWeekendOptions.setSelection(intWeekendOption);
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
	
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
	{
		switch( buttonView.getId() )
		{
			case R.id.checkboxEnd:
				if( isChecked )
				{
					// Turn on the ending items
					editEndDate.setEnabled(true);
					editNumTransactions.setEnabled(true);
					btnSelectDate.setEnabled(true);
					textEndDate.setEnabled(true);
					textRemainingTrans.setEnabled(true);					
				}
				else
				{
					// Turn off the ending items
					editEndDate.setEnabled(false);
					editNumTransactions.setEnabled(false);
					btnSelectDate.setEnabled(false);
					textEndDate.setEnabled(false);
					textRemainingTrans.setEnabled(false);
				}
				break;
			default:
				break;	
		}
		parentTabHost.setIsDirty(true);
	}
	
	// the callback received with the user "sets" the opening date in the dialog
	private DatePickerDialog.OnDateSetListener mDateSetListener = 
			new DatePickerDialog.OnDateSetListener() 
			{				
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) 
				{
					intYear = year;
					intMonth = monthOfYear;
					intDay = dayOfMonth;
					updateDisplay();
				}
			};
			
	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch(id)
		{
			case SET_DATE_ID:
				return new DatePickerDialog(this, mDateSetListener, intYear, intMonth, intDay);
		}
		return null;
	}

	public class AccountOnItemSelectedListener implements OnItemSelectedListener
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			if( numberOfPasses > 1)
			{
				switch( parent.getId())
				{
					case R.id.scheduleWeekendOption:
						String str = parent.getAdapter().getItem(pos).toString();
						if( str.equals("Move before") )
							intWeekendOption = MOVE_BEFORE;
						else if( str.equals("Move after") )
							intWeekendOption = MOVE_AFTER;
						else if( str.equals("Do nothing") )
							intWeekendOption = MOVE_NOTHING;
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
	
	// **************************************************************************************************
	// ************************************ Helper methods **********************************************
	public int getScheduleWeekendOption()
	{
		return this.intWeekendOption;
	}
	
	public void setScheduleWeekendOption(int value)
	{
		this.intWeekendOption = value;
	}
	
	public String getScheduleEstimate()
	{
		// The database actually stores this "Fixed" not estimate, so we return just the opp of the checkbox.
		return this.ckboxEstimate.isChecked() == true ? "N" : "Y";
	}
	
	public void setScheduleIsEstimate(String str)
	{
		// The database actually stores this as "Fixed" not estimate, so we have to set the checkbox to the opposite of what is stored.
		if( str.equals( "N" ) )
			this.ckboxEstimate.setChecked(true);
		else if( str.equals( "Y" ) )
			this.ckboxEstimate.setChecked(false);
	}
	
	public String getScheduleAutoEnter()
	{
		return this.ckboxAutoEnter.isChecked() == true ? "Y" : "N";
	}
	
	public void setScheduleAutoEnter(String str)
	{
		if( str.equals("Y") )
			this.ckboxAutoEnter.setChecked(true);
		else if( str.equals("N") )
			this.ckboxAutoEnter.setChecked(false);
	}
	
	public boolean getWillScheduleEnd()
	{
		return this.ckboxScheduleEnds.isChecked();
	}
	
	public String getEndDate()
	{
		// Need to re-format the date to YYY-MM-DD
		String str[] = this.editEndDate.getText().toString().split("-");
		return new StringBuilder()
		// Month is 0 based so add 1
		.append(str[2]).append("-")
		.append(str[0]).append("-")
		.append(str[1]).toString();

	}
	
	public void setEndDate(String str)
	{
		if( str != null)
		{
			// Turn on the ending items
			this.ckboxScheduleEnds.setChecked(true);
			this.editEndDate.setEnabled(true);
			this.editNumTransactions.setEnabled(true);
			this.btnSelectDate.setEnabled(true);
			this.textEndDate.setEnabled(true);
			this.textRemainingTrans.setEnabled(true);

			// Month is 0 based so we need to subtract 1
			String date[] = str.split("-");
			intYear = Integer.valueOf(date[0]);
			intMonth = Integer.valueOf(date[1]) - 1;
			intDay = Integer.valueOf(date[2]);
			
			updateDisplay();
		}
		else
		{
			// Turn off the ending items
			this.ckboxScheduleEnds.setChecked(false);
			this.editEndDate.setEnabled(false);
			this.editNumTransactions.setEnabled(false);
			this.btnSelectDate.setEnabled(false);
			this.textEndDate.setEnabled(false);
			this.textRemainingTrans.setEnabled(false);
		}
	}
	
	public int getRemainingTransactions()
	{
		String str = this.editNumTransactions.getText().toString();
		
		if( !str.isEmpty() )
			return Integer.valueOf(str);
		else
			return 0;
	}
	
	private void updateDisplay()
	{
		String strDay = null;
		String strMonth = null;
		switch(intDay)
		{
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
				strDay = "0" + String.valueOf(intDay);
				break;
			default:
				strDay = String.valueOf(intDay);
			break;
		}
		
		switch(intMonth)
		{
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
				strMonth = "0" + String.valueOf(intMonth + 1);
				break;
			default:
				strMonth = String.valueOf(intMonth + 1);
				break;
		}
		
		editEndDate.setText(
				new StringBuilder()
					// Month is 0 based so add 1
					.append(strMonth).append("-")
					.append(strDay).append("-")
					.append(intYear));
	}
}
