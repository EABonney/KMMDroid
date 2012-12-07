package com.vanhlebarsoftware.kmmdroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

public class PayeeDefaultAccountActivity extends FragmentActivity implements OnCheckedChangeListener
{
	private static final String TAG = "PayeeDefaultAccountActivity";
	private static final String dbTable = "kmmAccounts";
	private static final String[] dbColumns = { "accountName", "id AS _id"};
	private static final String strSelectionInc = "accountTypeString=? AND (balance != '0/1')";
	private static final String strSelectionExp = "accountTypeString=? AND (balance != '0/1')";
	private static final String strOrderBy = "accountName ASC";
	static final String[] FROM = { "accountName" };
	static final int[] TO = { android.R.id.text1 };
	String strIncAccountSelected = null;
	String strExpAccountSelected = null;
	int IncSpinnerPos = 0;
	int ExpSpinnerPos = 0;
	private int numberOfPasses = 0;
	private CreateModifyPayeeActivity parentTabHost;
	KMMDroidApp KMMDapp;
	Cursor cursorInc;
	Cursor cursorExp;
	Spinner spinIncome;
	Spinner spinExpense;
	CheckBox checkboxInc;
	CheckBox checkboxExp;
	CheckBox checkboxDefaultEnabled;
	SimpleCursorAdapter adapterInc;
	SimpleCursorAdapter adapterExp;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payee_defaultaccount);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Get the tabHost on the parent.
        parentTabHost = ((CreateModifyPayeeActivity) this.getParent());
        
        // Find our views
        spinIncome = (Spinner) findViewById(R.id.payeeDefaultIncome);
        spinExpense = (Spinner) findViewById(R.id.payeeDefaultExpense);
        checkboxInc = (CheckBox) findViewById(R.id.checkboxPayeeDefaultIncome);
        checkboxExp = (CheckBox) findViewById(R.id.checkboxPayeeDefaultExpense);
        checkboxDefaultEnabled = (CheckBox) findViewById(R.id.payeeUseDefault);
        
        // Hook into our onClickListener Events for the checkboxes.
        checkboxDefaultEnabled.setOnCheckedChangeListener(this);
        checkboxInc.setOnCheckedChangeListener(this);
        checkboxExp.setOnCheckedChangeListener(this);
        
        // Make the spinners and Income/Expense checkboxes disabled.
        spinIncome.setEnabled(false);
        spinExpense.setEnabled(false);
        checkboxInc.setEnabled(false);
        checkboxExp.setEnabled(false);
        
        // Set the OnItemSelectedListeners for the spinners.
        spinIncome.setOnItemSelectedListener(new PayeeDefaultOnItemSelectedListener());
        spinExpense.setOnItemSelectedListener(new PayeeDefaultOnItemSelectedListener());
        
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
		
		//Get all the accounts to be displayed.
		cursorInc = KMMDapp.db.query(dbTable, dbColumns, strSelectionInc, new String[] { getString(R.string.Income) }, null, null, strOrderBy);
		startManagingCursor(cursorInc);
		cursorExp = KMMDapp.db.query(dbTable, dbColumns, strSelectionExp, new String[] { getString(R.string.Expense) }, null, null, strOrderBy);
		startManagingCursor(cursorExp);
		
		// Set up the adapter
		adapterInc = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursorInc, FROM, TO);
		adapterExp = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursorExp, FROM, TO);
		adapterInc.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		adapterExp.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		
		//adapter.setViewBinder(VIEW_BINDER);
		spinIncome.setAdapter(adapterInc);
		spinExpense.setAdapter(adapterExp);
		
		// Set the spinners location.
		spinIncome.setSelection(IncSpinnerPos);
		spinExpense.setSelection(ExpSpinnerPos);
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
				}
			});				
			alertDel.show();
		}
		else
		{
			finish();
		}
	}
	
	public void onCheckedChanged(CompoundButton btn, boolean arg1) 
	{
		switch ( btn.getId() )
		{
			case R.id.payeeUseDefault:
				// if the user is selecting the checkbox, turn on the Income/Exp checkboxes
				if( btn.isChecked() )
				{
			        checkboxInc.setEnabled(true);
			        checkboxExp.setEnabled(true);					
				}
				else
				{
					checkboxInc.setEnabled(false);
					checkboxExp.setEnabled(false);
					
					// Need to also ensure that the spinners are disabled as well.
					spinIncome.setEnabled(false);
					spinExpense.setEnabled(false);
					
					// Return both Income/Expense checkboxes to unchecked status.
					checkboxInc.setChecked(false);
					checkboxExp.setChecked(false);
				}
				break;
			case R.id.checkboxPayeeDefaultExpense:
				//If the user is selecting the checkbox, turn on the spinner.
				if ( btn.isChecked() )
				{
					spinExpense.setEnabled(true);
					
					// also ensure that the user can only check Income OR Expense for default.
					checkboxInc.setChecked(false);
				}
				else
					spinExpense.setEnabled(false);
				break;
			case R.id.checkboxPayeeDefaultIncome:
				//If the user is selecting the checkbox, turn on the spinner.
				if( btn.isChecked() )
				{
					spinIncome.setEnabled(true);
					
					// also ensure that the user can only check Income OR Expense for default.
					checkboxExp.setChecked(false);
				}
				else
					spinIncome.setEnabled(false);
				break;
		}
		parentTabHost.setIsDirty(true);
	}
	
	public boolean getUseDefaults()
	{
		return checkboxDefaultEnabled.isChecked();
	}
	
	public boolean getUseIncome()
	{
		return checkboxInc.isChecked();
	}
	
	public boolean getUseExpense()
	{
		return checkboxExp.isChecked();
	}
	
	public String getIncomeAccount()
	{
		return strIncAccountSelected;
	}
	
	public String getExpenseAccount()
	{
		return strExpAccountSelected;
	}
	
	public void putUseDefaults(boolean b)
	{
		checkboxDefaultEnabled.setChecked(b);
	}
	
	public void putUseIncome(boolean b)
	{
		checkboxInc.setChecked(b);
		checkboxInc.setEnabled(b);
	}
	
	public void putUseExpense(boolean b)
	{
		checkboxExp.setChecked(b);
		checkboxExp.setEnabled(b);
	}
	
	public void putIncomeAccount(int position)
	{
		IncSpinnerPos = position;
	}
	
	public void putExpenseAccount(int position)
	{
		ExpSpinnerPos = position;
	}
	
	public class PayeeDefaultOnItemSelectedListener implements OnItemSelectedListener
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			if( numberOfPasses > 1 )
			{
				Cursor c = (Cursor) parent.getAdapter().getItem(pos);
			
				switch ( parent.getId() )
				{
					case R.id.payeeDefaultIncome:
						strIncAccountSelected = c.getString(1);
						parentTabHost.setIsDirty(true);
						break;
					case R.id.payeeDefaultExpense:
						strExpAccountSelected = c.getString(1);
						parentTabHost.setIsDirty(true);
						break;
					default:
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
}
