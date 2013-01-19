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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;

public class PayeeDefaultIncFragment extends Fragment implements
										LoaderManager.LoaderCallbacks<Cursor>,
										OnCheckedChangeListener
{
	private static final String TAG = PayeeDefaultIncFragment.class.getSimpleName();
	public static final int PAYEE_INCOME_LOADER = 0x25;
	private OnUseDefaultIncomeListener onUseDefaultIncome;
	private Activity ParentActivity;
	static final String[] FROM = { "accountName" };
	static final int[] TO = { android.R.id.text1 };
	private String strIncAccountSelected = null;
	private int numberOfPasses = 0;
	private int spinnerPos = -1;
	Spinner spinIncome;
	CheckBox checkboxInc;
	SimpleCursorAdapter adapterInc;

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) 
	{
		super.onAttach(activity);
		
		// Save our ParentActivity
		ParentActivity = activity;
		
		try
		{
			onUseDefaultIncome = (OnUseDefaultIncomeListener) activity;
		}
		catch(ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement OnUseDefaultIncomeListener");
		}
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.fragment_payeedefaultincome, container, false);
		
        // Find our views
        spinIncome = (Spinner) view.findViewById(R.id.payeeDefaultIncome);
        checkboxInc = (CheckBox) view.findViewById(R.id.checkboxPayeeDefaultIncome);
        
        // Hook into our onClickListener Events for the checkboxes.
        checkboxInc.setOnCheckedChangeListener(this);
        
        // Make the spinners and Income/Expense checkboxes disabled.
        spinIncome.setEnabled(false);
        checkboxInc.setEnabled(false);
        
        // Set the OnItemSelectedListeners for the spinners.
        spinIncome.setOnItemSelectedListener(new DefaultIncomeOnItemSelectedListener());
        
		// Set up the adapter
		adapterInc = new SimpleCursorAdapter(getActivity().getBaseContext(), android.R.layout.simple_spinner_item, null, FROM, TO, 0);
		spinIncome.setAdapter(adapterInc);
		
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(PAYEE_INCOME_LOADER, null, this);
        
		return view;
	}

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
	{
		onUseDefaultIncome.onUseDefaultIncome(isChecked);
	}
	
	public Loader<Cursor> onCreateLoader(int id, Bundle args) 
	{
		String[] dbColumns = { "accountName", "id AS _id"};
		String strSelectionInc = "accountType=? AND (balance != '0/1')";
		String strOrderBy = "accountName ASC";
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, frag);
		u = Uri.parse(u.toString());
		return new CursorLoader(getActivity().getBaseContext(), u, dbColumns, strSelectionInc, 
												new String[] { String.valueOf(Account.ACCOUNT_INCOME) }, strOrderBy);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor income) 
	{
		adapterInc.swapCursor(income);
		
		if( this.spinnerPos != -1)
			spinIncome.setSelection(spinnerPos);
		else
			spinIncome.setSelection(0);
	}

	public void onLoaderReset(Loader<Cursor> loader) 
	{
		Log.d(TAG, "Income loader was reset.");
		adapterInc.swapCursor(null);
	}	
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onDestroyView()
	 */
	@Override
	public void onDestroyView() 
	{
		super.onDestroyView();
		Log.d(TAG, "Destroying the income loader.......");
		getLoaderManager().destroyLoader(PAYEE_INCOME_LOADER);
	}

	// *******************************************************************************************************
	// *********************************** Public Event Interfaces *******************************************
	public interface OnUseDefaultIncomeListener
	{
		public void onUseDefaultIncome(boolean flag);
	}
	
	public class DefaultIncomeOnItemSelectedListener implements OnItemSelectedListener
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			if( numberOfPasses > 0 )
			{
				Cursor c = (Cursor) parent.getAdapter().getItem(pos);
			
				switch ( parent.getId() )
				{
					case R.id.payeeDefaultIncome:
						strIncAccountSelected = c.getString(c.getColumnIndex("_id"));
						//((CreateModifyPayeeActivity) ParentTab).setIsDirty(true);
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
		this.strIncAccountSelected = id;
	}
	
	public String getIncomeId()
	{
		return this.strIncAccountSelected;
	}
}
