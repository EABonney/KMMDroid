package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class PayeeDefaultAccountActivity extends Fragment implements 
													OnCheckedChangeListener
{
	private static final String TAG = PayeeDefaultAccountActivity.class.getSimpleName();
	private OnUseDefaultCheckedListener onUseDefaultChecked;
	private OnSendDefaultDataListener onSendDefaultData;
	private Activity ParentTab;
	Fragment expFrag = null;
	Fragment incFrag = null;
	CheckBox checkboxDefaultEnabled;
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) 
	{
		super.onAttach(activity);
		
		// Save our ParentActivity
		ParentTab = activity;
		
		try
		{
			onUseDefaultChecked = (OnUseDefaultCheckedListener) activity;
		}
		catch(ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement OnUseDefaultCheckedListener");
		}
		
		try
		{
			onSendDefaultData = (OnSendDefaultDataListener) activity;
		}
		catch(ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement OnSendDefaultDataListener");
		}
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
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
        
        View view = inflater.inflate(R.layout.payee_defaultaccount, container, false);
        
        // Find our views
        checkboxDefaultEnabled = (CheckBox) view.findViewById(R.id.payeeUseDefault);
        
        // Hook into our onClickListener Events for the checkboxes.
        checkboxDefaultEnabled.setOnCheckedChangeListener(this);
        
        // Add the fragments for the expense and income spinners.
        if( expFrag == null)
        {
        	expFrag = new PayeeDefaultExpFragment();
        	incFrag = new PayeeDefaultIncFragment();
        	FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        	ft.add(R.id.incFragment, incFrag, "incomeFragment");
        	ft.add(R.id.expFragment, expFrag, "expenseFragment");
        	ft.commit();
        	this.getChildFragmentManager().executePendingTransactions();
        }
        
		return view;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
    }

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		sendDefaultData();
		
		updateUIElements();
	}
	
	public void onCheckedChanged(CompoundButton btn, boolean arg1) 
	{
		switch ( btn.getId() )
		{
			case R.id.payeeUseDefault:
				// if the user is selecting the checkbox, turn on the Income/Exp checkboxes
				onUseDefaultChecked.onUseDefaultChecked(btn.isChecked());
				break;
		}
		((CreateModifyPayeeActivity) ParentTab).setIsDirty(true);
	}
	// *******************************************************************************************************
	// *********************************** Public Event Interfaces *******************************************
	public interface OnUseDefaultCheckedListener
	{
		public void onUseDefaultChecked(boolean flag);
	}
	
	public interface OnSendDefaultDataListener
	{
		public void onSendDefaultData();
	}
	// ************************************************************************************************
	// ******************************* Helper Functions ***********************************************
	private void sendDefaultData()
	{
		onSendDefaultData.onSendDefaultData();
	}
	
	public boolean getUseDefaults()
	{
		return checkboxDefaultEnabled.isChecked();
	}

	public void putUseDefaults(boolean b)
	{
		checkboxDefaultEnabled.setChecked(b);
	}
	
	public void putDefaultAccountId(String id)
	{
		boolean idFound = false;
		// Get our cursors to look up the id.
		String[] dbColumns = { "accountName", "id AS _id"};
		String strSelectionInc = "accountType=? AND (balance != '0/1')";
		String strOrderBy = "accountName ASC";
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, frag);
		u = Uri.parse(u.toString());
		Cursor incCur = getActivity().getBaseContext().getContentResolver().query(u, dbColumns, strSelectionInc, 
										new String[] { String.valueOf(Account.ACCOUNT_INCOME) }, strOrderBy);
		Cursor expCur = getActivity().getBaseContext().getContentResolver().query(u, dbColumns, strSelectionInc, 
				new String[] { String.valueOf(Account.ACCOUNT_EXPENSE) }, strOrderBy);
		PayeeDefaultIncFragment incFrag = (PayeeDefaultIncFragment) this.getChildFragmentManager().findFragmentByTag("incomeFragment");
		PayeeDefaultExpFragment expFrag = (PayeeDefaultExpFragment) this.getChildFragmentManager().findFragmentByTag("expenseFragment");
		incCur.moveToFirst();
		expCur.moveToFirst();
		incFrag.adapterInc.getCursor();
		for(int i=0; i < incCur.getCount(); i++)
		{
			if( id.equals(incCur.getString(incCur.getColumnIndex("_id"))) )
			{
				idFound = true;
				
				// Let's set the spinner to the correct location and turn off the expense fragment
				this.checkboxDefaultEnabled.setChecked(true);
				incFrag.checkboxInc.setChecked(true);
				incFrag.setSpinnerPos(i, id);
				expFrag.checkboxExp.setChecked(false);
				expFrag.checkboxExp.setEnabled(false);
				expFrag.spinExpense.setEnabled(false);
				break;
			}
			else
				incCur.moveToNext();
		}
		
		if( !idFound )
		{
			for(int i=0; i < expCur.getCount(); i++)
			{
				if( id.equals(expCur.getString(expCur.getColumnIndex("_id"))) )
				{
					idFound = true;
					
					// Let's set the spinner to the correct location and turn off the income fragment
					this.checkboxDefaultEnabled.setChecked(true);
					expFrag.checkboxExp.setChecked(true);
					expFrag.setSpinnerPos(i, id);
					incFrag.checkboxInc.setChecked(false);
					incFrag.checkboxInc.setEnabled(false);
					incFrag.spinIncome.setEnabled(false);
					break;					
				}
				else
					expCur.moveToNext();
			}
		}
		
		// Clean up our Cursors
		expCur.close();
		incCur.close();
	}
	
	public boolean getUseIncome()
	{
		PayeeDefaultIncFragment incFrag = (PayeeDefaultIncFragment) this.getChildFragmentManager().findFragmentByTag("incomeFragment");
		return incFrag.checkboxInc.isChecked();
	}
	
	public boolean getUseExpense()
	{
		PayeeDefaultExpFragment expFrag = (PayeeDefaultExpFragment) this.getChildFragmentManager().findFragmentByTag("expenseFragment");
		return expFrag.checkboxExp.isChecked();
	}
	
	public String getIncomeAccount()
	{
		PayeeDefaultIncFragment incFrag = (PayeeDefaultIncFragment) this.getChildFragmentManager().findFragmentByTag("incomeFragment");

		return incFrag.getIncomeId();
	}
	
	public String getExpenseAccount()
	{
		PayeeDefaultExpFragment expFrag = (PayeeDefaultExpFragment) this.getChildFragmentManager().findFragmentByTag("expenseFragment");

		return expFrag.getExpensId();
	}
	
	private void updateUIElements()
	{
		SharedPreferences prefs = getActivity().getPreferences(Activity.MODE_PRIVATE);
		int useDefault = prefs.getInt("UseDefault", -1);
		int useIncome = prefs.getInt("UseIncome", -1);
		int useExpense = prefs.getInt("UseExpense", -1);
		String incId = prefs.getString("IncId", null);
		String expId = prefs.getString("ExpId", null);

		if( useDefault != -1 )
		{
			this.checkboxDefaultEnabled.setChecked(false);
			if( useIncome > 0 )
				this.putDefaultAccountId(incId);
			else if( useExpense > 0 )
				this.putDefaultAccountId(expId);
		}
		
	}
}
