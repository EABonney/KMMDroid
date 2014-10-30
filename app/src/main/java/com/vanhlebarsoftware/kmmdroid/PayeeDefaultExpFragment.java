package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class PayeeDefaultExpFragment extends Fragment implements
										LoaderManager.LoaderCallbacks<Cursor>,
										OnCheckedChangeListener
{
	private static final String TAG = PayeeDefaultExpFragment.class.getSimpleName();
	public static final int PAYEE_EXPENSE_LOADER = 0x25;
	private OnUseDefaultExpenseListener onUseDefaultExpense;
	static final String[] FROM = { "accountName" };
	static final int[] TO = { android.R.id.text1 };
	private String strExpAccountSelected = null;
	private int numberOfPasses = 0;
	private int spinnerPos = -1;
	Spinner spinExpense;
	CheckBox checkboxExp;
	SimpleCursorAdapter adapterExp;

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */	
	@Override
	public void onAttach(Activity activity) 
	{
		super.onAttach(activity);
		
		try
		{
			onUseDefaultExpense = (OnUseDefaultExpenseListener) activity;
		}
		catch(ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement OnUseDefaultExpenseListener");
		}
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.fragment_payeedefaultexpense, container, false);

		// Find our views
		spinExpense = (Spinner) view.findViewById(R.id.payeeDefaultExpense);
		checkboxExp = (CheckBox) view.findViewById(R.id.checkboxPayeeDefaultExpense);

		// Hook into our onClickListener Events for the checkboxes.
		checkboxExp.setOnCheckedChangeListener(this);

		// Make the spinners and Income/Expense checkboxes disabled.
		spinExpense.setEnabled(false);
		checkboxExp.setEnabled(false);
		
		// Set the OnItemSelectedListeners for the spinners.
		spinExpense.setOnItemSelectedListener(new DefaultExpenseOnItemSelectedListener());

		// Set up the adapter
		adapterExp = new SimpleCursorAdapter(getActivity().getBaseContext(), android.R.layout.simple_spinner_item, null, FROM, TO, 0);
		spinExpense.setAdapter(adapterExp);

		// Prepare the loader.  Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(PAYEE_EXPENSE_LOADER, null, this);

		return view;
	}

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
	{
		onUseDefaultExpense.onUseDefaultExpense(isChecked);
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) 
	{
		String[] dbColumns = { "accountName", "id AS _id"};
		String strSelectionExp = "accountType=? AND (balance != '0/1')";
		String strOrderBy = "accountName ASC";
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, frag);
		u = Uri.parse(u.toString());
		return new CursorLoader(getActivity().getBaseContext(), u, dbColumns, strSelectionExp, 
											new String[] { String.valueOf(Account.ACCOUNT_EXPENSE) }, strOrderBy);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor expense) 
	{
		adapterExp.swapCursor(expense);
		
		if( this.spinnerPos != -1)
			spinExpense.setSelection(spinnerPos);
		else
			spinExpense.setSelection(0);
	}

	public void onLoaderReset(Loader<Cursor> loader) 
	{
		adapterExp.swapCursor(null);
	}	
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onDestroyView()
	 */
	@Override
	public void onDestroyView() 
	{
		super.onDestroyView();
		Log.d(TAG, "Destroying the expense loader.......");
		getLoaderManager().destroyLoader(PAYEE_EXPENSE_LOADER);
	}
	// *******************************************************************************************************
	// *********************************** Public Event Interfaces *******************************************
	public interface OnUseDefaultExpenseListener
	{
		public void onUseDefaultExpense(boolean flag);
	}
	
	public class DefaultExpenseOnItemSelectedListener implements OnItemSelectedListener
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			if( numberOfPasses > 0 )
			{
				Cursor c = (Cursor) parent.getAdapter().getItem(pos);

				switch ( parent.getId() )
				{
					case R.id.payeeDefaultExpense:
						strExpAccountSelected = c.getString(c.getColumnIndex("_id"));
						break;
					default:
						break;
				}
			}
			else
				numberOfPasses++;
		}

		public void onNothingSelected(AdapterView<?> arg0) 
		{
			// do nothing.
		}		
	}
	// ************************************************************************************************
	// ******************************* Helper Functions ***********************************************
	public void setSpinnerPos(int pos, String id)
	{
		this.spinnerPos = pos;
		this.strExpAccountSelected = id;
	}
	
	public String getExpensId()
	{
		return this.strExpAccountSelected;
	}
}
