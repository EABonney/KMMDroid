package com.vanhlebarsoftware.kmmdroid;

import java.util.Calendar;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

public class CreateAccountAccountActivity extends Fragment implements
LoaderManager.LoaderCallbacks<Cursor>
{
	private static final String TAG = "CreateAccountAccountActivity";
	private static final int CAACCOUNT_LOADER = 0x13;
	static final String[] FROM = { "name" };
	static final int[] TO = { android.R.id.text1 };
	static final int SET_DATE_ID = 0;
	private int TypeSelected = 0;
	private int numberOfPasses = 0;
	private int intYear;
	private int intMonth;
	private int intDay;
	private String strTypeSelected = null;
	private String currencySelected = null;
	private String strAccountName = null;
	private String strOpenDate = null;
	private String strOpenBalance = null;
	private boolean bPreferred = false;
	private OnAccountPreferredCheckedListener onAccountPreferredCheckedListener;
	private OnSendAccountDataListener onSendAccountData;
	private Activity ParentActivity;
	Button buttonDate;
	EditText accountName;
	EditText openDate;
	EditText openBalance;
	Spinner spinType;
	Spinner spinCurrency;
	CheckBox checkPreferred;
	TextView txtTotTrans;
	SimpleCursorAdapter adapterCurrency;
	ArrayAdapter<CharSequence> adapterTypes;
	
	@Override
	public void onCreate(Bundle savedState)
	{
		super.onCreate(savedState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.createaccount_account, container, false);
		
        if (container == null) 
        {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
        	Log.d(TAG, "Container was null!!!");
            return null;
        }
        
        // Find our views
        spinCurrency = (Spinner) view.findViewById(R.id.accountCurrency);
        spinType = (Spinner) view.findViewById(R.id.accountType);
        accountName = (EditText) view.findViewById(R.id.accountName);
        openDate = (EditText) view.findViewById(R.id.accountOpenDate);
        openBalance = (EditText) view.findViewById(R.id.accountOpenBalance);
        checkPreferred = (CheckBox) view.findViewById(R.id.checkboxAccountPreferred);
        buttonDate = (Button) view.findViewById(R.id.buttonSetDate);
        txtTotTrans = (TextView) view.findViewById(R.id.titleAccountTransactions);
        
        // Set our OnClickListener events
        buttonDate.setOnClickListener(new View.OnClickListener() 
        {	
			public void onClick(View v)
			{
				DialogFragment dateFrag = new KMMDDatePickerFragment(openDate);
				dateFrag.show(getActivity().getSupportFragmentManager(), "datePicker");
				((CreateModifyAccountActivity) ParentActivity).setIsAccountDirty(true);
			}
		});
        
        openDate.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				if( s.length() > 0 )
				{
					Log.d(TAG, "openDate: " + s.toString());
					String tmp[] = s.toString().split("-");
					intYear = Integer.valueOf(tmp[2]);
					intMonth = Integer.valueOf(tmp[0]) - 1;
					intDay = Integer.valueOf(tmp[1]);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        checkPreferred.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) 
			{
				switch( buttonView.getId() )
				{
					case R.id.checkboxAccountPreferred:
						((CreateModifyAccountActivity) ParentActivity).setIsAccountDirty(true);
						break;
				}
			}
        });
        
        // Set the OnItemSelectedListeners for the spinners.
        spinType.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        spinCurrency.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        
        // Set up the other keyListener's for the various editText items.
        accountName.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				((CreateModifyAccountActivity) ParentActivity).setIsAccountDirty(true);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        openBalance.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				((CreateModifyAccountActivity) ParentActivity).setIsAccountDirty(true);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
		// Set up the adapters
		adapterTypes = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.arrayAccountTypes, android.R.layout.simple_spinner_item);
		adapterTypes.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinType.setAdapter(adapterTypes);

		adapterCurrency = new SimpleCursorAdapter(getActivity().getBaseContext(), android.R.layout.simple_spinner_item, null, FROM, TO, 0);
		adapterCurrency.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinCurrency.setAdapter(adapterCurrency);
		
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(CAACCOUNT_LOADER, null, this);
        
        // get the current date
        final Calendar c = Calendar.getInstance();
        intYear = c.get(Calendar.YEAR);
        intMonth = c.get(Calendar.MONTH);
        intDay = c.get(Calendar.DAY_OF_MONTH);
        
        Log.d(TAG, "Inside onCreateView()");
        return view;
    }
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) 
	{
		super.onAttach(activity);
		
		// Save our parent activity.
		ParentActivity = activity;
		
		try 
		{
			onAccountPreferredCheckedListener = (OnAccountPreferredCheckedListener) activity;
		} 
		catch (ClassCastException e) 
		{
			throw new ClassCastException(activity.toString() + "must implment OnAccountPreferredCheckedListener");
		}
		
		try
		{
			onSendAccountData = (OnSendAccountDataListener) activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + "must implement OnSendAccountDataListener");
		}
		
		Log.d(TAG, "onAttach()");
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
	}
	
	public class AccountOnItemSelectedListener implements OnItemSelectedListener
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			if( numberOfPasses > 1)
			{
				switch( parent.getId())
				{
					case R.id.accountType:
						strTypeSelected = parent.getAdapter().getItem(pos).toString();
						Log.d(TAG, "itemSelected: " + strTypeSelected);
			
						if( strTypeSelected.matches(getString(R.string.Asset)) )
						{	
							TypeSelected = 0;
						}
						else if( strTypeSelected.matches(getString(R.string.Checking)) )
						{
							TypeSelected = 1;
						}
						else if( strTypeSelected.matches(getString(R.string.Equity)) )
						{
							TypeSelected = 2;
						}
						else if( strTypeSelected.matches(getString(R.string.Liability)) )
						{	
							TypeSelected = 3;
						}
						else if( strTypeSelected.matches(getString(R.string.Savings)) )
						{
							TypeSelected = 4;
						}
						else
							Log.d(TAG, "ERROR!!!");
						((CreateModifyAccountActivity) ParentActivity).setIsAccountDirty(true);
						break;
					case R.id.accountCurrency:
						Cursor c = (Cursor) parent.getAdapter().getItem(pos);
						currencySelected = c.getString(0);
						((CreateModifyAccountActivity) ParentActivity).setIsAccountDirty(true);
						break;
				}
			}
			else
			{
				numberOfPasses++;
				Log.d(TAG, "Number of passes: " + numberOfPasses);
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// do nothing.
		}		
	}


	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		switch( buttonView.getId() )
		{
			case R.id.checkboxAccountPreferred:
				((CreateModifyAccountActivity) ParentActivity).setIsAccountDirty(true);
				break;
		}
	}
	
	public Loader<Cursor> onCreateLoader(int id, Bundle args) 
	{
		Log.d(TAG, "Initializing the loader.......");
		String dbColumns[] = { "ISOCode AS _id", "name" };
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_CURRENCY_URI, frag);
		u = Uri.parse(u.toString());

		return new CursorLoader(getActivity().getBaseContext(), u, dbColumns, null, null, null);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor currencies) 
	{
		Log.d(TAG, "loader is done and we have our cursor");
		adapterCurrency.swapCursor(currencies);
		
		// Notify parent activity to send the Account data.
		sendAccountData();
		
		updateUIElements();
	}

	public void onLoaderReset(Loader<Cursor> loader) 
	{
		adapterCurrency.swapCursor(null);
	}
	// *******************************************************************************************************
	// *********************************** Public Event Interfaces *******************************************
	public interface OnAccountPreferredCheckedListener
	{
		public void onAccountPreferredChecked(CompoundButton btn, boolean arg1);
	}
	
	public interface OnSendAccountDataListener
	{
		public void onSendAccountData();
	}
	// **************************************************************************************************
	// ************************************ Helper methods **********************************************	
	private String getBaseCurrency()
	{
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
		u = Uri.parse(u.toString());
		Cursor c = getActivity().getBaseContext().getContentResolver().query(u, new String[] { "baseCurrency" }, null, null, null);
		//Cursor c = KMMDapp.db.query("kmmFileInfo", new String[] { "baseCurrency" }, null, null, null, null, null);
		c.moveToFirst();
		String currency = c.getString(0);
		c.close();
		return currency;
	}
	
	private int setCurrency(String baseCur)
	{
		int i = 0;
		Cursor c = adapterCurrency.getCursor();
		c.moveToFirst();
		
		if( baseCur != null )
		{
			while(!baseCur.equals(c.getString(0)))
			{
				c.moveToNext();
			
				//check to see if we have moved past the last item in the cursor, if so return current i.
				if(c.isAfterLast())
					return i;
			
				i++;
			}
		}
		return i;
	}
	
	public String getAccountName()
	{
		if( accountName != null )
			return accountName.getText().toString();
		else
			return this.strAccountName;
	}
	
	public int getAccountType()
	{
		switch ( TypeSelected )
		{
			case 0:
				return 9;
			case 1:
				return 1;
			case 2:
				return 16;
			case 3:
				return 10;
			case 4:
				return 2;
			default:
				return 0;
		}
	}
	
	public String getAccountTypeString()
	{
		return strTypeSelected;
	}
	
	public String getCurrency()
	{
		return currencySelected;
	}
	
	public String getOpeningDate()
	{
		// We need to reverse the order of the date to be YYYY-MM-DD for SQL
		String tmp = null;
		if( openDate != null )
			tmp = openDate.getText().toString();
		else
			tmp = this.strOpenDate;
		
		if(tmp != null)
		{
			String dates[] = tmp.split("-");
		
			return new StringBuilder()
			.append(dates[2]).append("-")
			.append(dates[0]).append("-")
			.append(dates[1]).toString();
		}
		else
			return tmp;
	}
	
	public String getOpeningBalance()
	{
		String tmp = null;
		if( openBalance != null )
			tmp = openBalance.getText().toString();
		else
			tmp = this.strOpenBalance;
		
		if( tmp.isEmpty() )
			tmp = "0.00";
		
		return tmp;
	}
	
	public boolean getPreferredAccount()
	{
		if( checkPreferred != null )
			return checkPreferred.isChecked();
		else
			return this.bPreferred;
	}
	
	public void putAccountName(String name)
	{
		//accountName.setText(name);
		this.strAccountName = name;
	}
	
	public void putAccountType(int type)
	{
		switch ( type )
		{
			case 1:
				TypeSelected = 1;
				break;
			case 2:
				TypeSelected = 4;
				break;
			case 9:
				TypeSelected = 0;
				break;
			case 10:
				TypeSelected = 3;
				break;
			case 16:
				TypeSelected = 2;
				break;
		}
	}
	
	public void putAccountTypeString(String type)
	{
		strTypeSelected = type;
	}
	
	public void putCurrency(String currency)
	{
		currencySelected = currency;
	}
	
	public void putOpeningDate(String date)
	{	
		// need to see if we have a null string for some reason, if so do nothing.
		if( date != null )
		{
			date = date.trim();
			String dates[] = date.split("-");	
			
			this.strOpenDate = dates[1] + "-" + dates[2] + "-" + dates[0];
		}
	}
	
	public void putOpeningBalance(String balance)
	{
		//openBalance.setText(balance);
		this.strOpenBalance = balance;
	}
	
	public void putPreferredAccount(boolean preferred)
	{
		//checkPreferred.setChecked(preferred);
		this.bPreferred = preferred;
	}
	
	public void putTransactionCount(String strCount)
	{
		txtTotTrans.setText(txtTotTrans.getText().toString() + " " + strCount);
	}
	
	public void sendAccountData()
	{
		Log.d(TAG, "Asking for data from the parent...");
		onSendAccountData.onSendAccountData();
	}
	
	public void updateUIElements()
	{
		Log.d(TAG, "Updating UI");
		SharedPreferences prefs = getActivity().getPreferences(Activity.MODE_PRIVATE);
		
		String accountName = prefs.getString("AccountName", null);
		int accountType = prefs.getInt("AccountType", 0);
		String currencyId = prefs.getString("CurrencyId", null);
		String openDate = prefs.getString("OpenDate", null);
		String openBalance = prefs.getString("OpenBalance", null);
		boolean preferredAcct = prefs.getBoolean("PreferredAccount", false);
		String acctTypeString = prefs.getString("AccountTypeString", null);
		
        // Set the base currency from the file.
        currencySelected = getBaseCurrency();
        
		if( accountName != null )
			this.accountName.setText(accountName);
		else
			this.accountName.setText(this.strAccountName);
		if( accountType != 0 )
			this.putAccountType(accountType);
		if( acctTypeString != null)
			this.putAccountTypeString(acctTypeString);
		else
			this.putAccountTypeString(this.strTypeSelected);
		
		this.spinType.setSelection(this.TypeSelected);
		if( currencyId != null )
			this.spinCurrency.setSelection(setCurrency(currencyId));
		else
			this.spinCurrency.setSelection(setCurrency(this.currencySelected));
		if( openDate != null )
			this.openDate.setText(openDate);
		else
			this.openDate.setText(this.strOpenDate);
		if( openBalance != null )
			this.openBalance.setText(openBalance);
		else
			this.openBalance.setText(this.strOpenBalance);
		if( prefs.contains("PreferredAccount") )
			this.checkPreferred.setChecked(preferredAcct);
		else
			this.checkPreferred.setChecked(this.bPreferred);
		
        // display the current date
        updateDisplay();
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
				strMonth = "0" + String.valueOf(intMonth + 1);
				break;
			default:
				strMonth = String.valueOf(intMonth + 1);
				break;
		}
		
		openDate.setText(
				new StringBuilder()
					// Month is 0 based so add 1
					.append(strMonth).append("-")
					.append(strDay).append("-")
					.append(intYear));
	}
	public Bundle getAccountBundle()
	{
		Bundle bundleAcct = new Bundle();
		
		bundleAcct.putBoolean("preferred", this.getPreferredAccount());
		bundleAcct.putString("name", this.getAccountName());
		bundleAcct.putString("openDate", this.getOpeningDate());
		bundleAcct.putString("openBalance", this.getOpeningBalance());
		bundleAcct.putString("currencySelected", this.getCurrency());
		bundleAcct.putString("strTypeSelected", this.getAccountTypeString());
		bundleAcct.putInt("intTypeSelected", this.getAccountType());
		bundleAcct.putString("accountTypeString", this.strTypeSelected);
		Log.d(TAG, "accountTypeString: " + this.strTypeSelected);
		
		return bundleAcct;
	}
}
