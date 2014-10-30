package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
import android.widget.Toast;

public class CreateAccountInstitutionActivity extends Fragment implements
								LoaderManager.LoaderCallbacks<Cursor>
{
	private static final String TAG = "CreateAccountInstitutionActivity";
	private static final int CAINSTITUTIONS_LOADER = 0x11;
	private static final int ACTION_NEW = 1;
	static final String[] FROM = { "name" };
	static final int[] TO = { android.R.id.text1 };
	private String institutionSelected = null;
	private String institutionId = null;
	private String strAccountNumber = null;
	private String strIBAN = null;
	private boolean bUseInst = false;
	private int columnUsed = 1;
	private int numberOfPasses = 0;
	private OnNewInstitutionClickedListener onNewInstitutionClickedListener;
	private OnNoInstitutionCheckedListener onNoInstitutionCheckedListener;
	private OnInstitutionSelectedListener onInstitutionSelectedListener;
	private OnSendInstitutionData onSendInstitutionData;
	private Activity ParentActivity;
	EditText accountNumber;
	EditText accountIBAN;
	Spinner spinInstitutions;
	Button buttonNewInstitution;
	CheckBox checkboxNoInstitution;
	TextView textInstitutions;
	SimpleCursorAdapter adapterInst;
	//KMMDroidApp KMMDapp;
	
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
			onNewInstitutionClickedListener = (OnNewInstitutionClickedListener) activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + "must implment OnNewInstitutionClickedListener");
		}
		
		try
		{
			onNoInstitutionCheckedListener = (OnNoInstitutionCheckedListener) activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + "must implment OnNoInstitutionCheckedListener");
		}
		
		try
		{
			onSendInstitutionData = (OnSendInstitutionData) activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + "must implment OnInstitutionData");
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstance)
	{
        super.onCreate(savedInstance);
	}
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{ 
    	Log.d(TAG, "CreateAccountInstitutionActivity::onCreateView()");
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
        
        View view = inflater.inflate(R.layout.createaccount_institution, container, false);
   
        // Find our views
        spinInstitutions = (Spinner) view.findViewById(R.id.accountInstitution);
        accountNumber = (EditText) view.findViewById(R.id.accountNumber);
        accountIBAN = (EditText) view.findViewById(R.id.accountIBAN);
        buttonNewInstitution = (Button) view.findViewById(R.id.buttonNewInstitution);
        checkboxNoInstitution = (CheckBox) view.findViewById(R.id.checkboxNoInstitution);
        textInstitutions = (TextView) view.findViewById(R.id.titleAccountInstitution);
        
        // Set our listeners for our items.
        buttonNewInstitution.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View v) 
			{
				switch(v.getId())
				{
					case R.id.buttonNewInstitution:
						Intent i = new Intent(getActivity().getBaseContext(), CreateModifyInstitutionActivity.class);
						i.putExtra("Action", ACTION_NEW);
						startActivity(i);
						// Since we are adding a new Institution, make sure we have the spinner displayed.
						checkboxNoInstitution.setChecked(false);
						spinInstitutions.setVisibility(0);
						textInstitutions.setVisibility(0);				
						break;
				}
			}
		});
        
        checkboxNoInstitution.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) 
			{
				switch( buttonView.getId() )
				{
					case R.id.checkboxNoInstitution:
						// if the user is selecting the checkbox, turn off the Institutions spinner
						if( buttonView.isChecked() )
						{
							spinInstitutions.setVisibility(View.GONE);
							textInstitutions.setVisibility(View.GONE);
							buttonNewInstitution.setVisibility(View.GONE);
							// Remove the institution information from this account
							institutionId = null;
							institutionSelected = null;
						}
						else
						{
							spinInstitutions.setVisibility(View.VISIBLE);
							textInstitutions.setVisibility(View.VISIBLE);
							buttonNewInstitution.setVisibility(View.VISIBLE);
							spinInstitutions.setSelection(0);
							Cursor c = (Cursor) spinInstitutions.getSelectedItem();
							if(c != null )
							{
								c.moveToFirst();
								institutionId = c.getString(0);
								institutionSelected = c.getString(1);
								Log.d(TAG, "institudtionId: " + institutionId);
							}
						}
						break;
				}
				((CreateModifyAccountActivity) ParentActivity).setIsInstitutionDirty(true);
			}
        });
        
        spinInstitutions.setOnItemSelectedListener(new AccountInstitutionOnItemSelectedListener());
        
        // Set up the other keyListener's for the various editText items.
        accountNumber.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				((CreateModifyAccountActivity) ParentActivity).setIsInstitutionDirty(true);
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
				((CreateModifyAccountActivity) ParentActivity).setIsInstitutionDirty(true);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        // Make sure we start by default with no Institutions showing.
        checkboxNoInstitution.setChecked(true);	
        
		
		// Set up the adapters
		adapterInst = new SimpleCursorAdapter(getActivity().getBaseContext(), android.R.layout.simple_spinner_item, null, FROM, TO, 0);
		adapterInst.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinInstitutions.setAdapter(adapterInst);
		
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(CAINSTITUTIONS_LOADER, null, this);
        
        Log.d(TAG, "Inside onCreateView()");
        return view;
    }		

	@Override
	public void onResume()
	{
		super.onResume();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
	
	public class AccountInstitutionOnItemSelectedListener implements OnItemSelectedListener
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			if( numberOfPasses > 0 )
			{
				Cursor c = (Cursor) parent.getAdapter().getItem(pos);
			
				institutionId = c.getString(0);
				institutionSelected = c.getString(1);
				Log.d(TAG, "institudtionId: " + institutionId);
				((CreateModifyAccountActivity) ParentActivity).setIsDirty(true);
			}
			else
				numberOfPasses++;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// do nothing.
		}		
	}
	
	public Loader<Cursor> onCreateLoader(int id, Bundle args) 
	{
		Log.d(TAG, "staring loader.....");
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_INSTITUTION_URI, frag);
		u = Uri.parse(u.toString());
		String dbColumns[] = { "id AS _id", "name" };
		String strOrderBy = "name ASC";
		return new CursorLoader(getActivity().getBaseContext(), u, dbColumns, null, null, strOrderBy);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor institutions) 
	{
		adapterInst.swapCursor(institutions);
		Log.d(TAG, "loader is done and we have our cursor.");
		
		// Notify the ParentActivity we need it's data if any.
		sendInstitutionData();
		
		updateUIElements();
	}

	public void onLoaderReset(Loader<Cursor> loader) 
	{
		adapterInst.swapCursor(null);		
	}
	// *******************************************************************************************************
	// *********************************** Public Event Interfaces *******************************************
	public interface OnNewInstitutionClickedListener
	{
		public void onNewInstitutionClicked(View view);
	}
	
	public interface OnNoInstitutionCheckedListener
	{
		public void onNoInstitutionChecked(CompoundButton btn, boolean arg1);
	}
	
	public interface OnInstitutionSelectedListener
	{
		public void onInstitutionSelected(AdapterView<?> parent, View view, int pos, long id);
	}
	
	public interface OnSendInstitutionData
	{
		public void onSendInstitutionData();
	}
	// ************************************************************************************************
	// *********************************** Helper functions *******************************************
	
	private int setInstitution(String institution, int columUsed)
	{
		int i = 0;
		Cursor c = adapterInst.getCursor();
		c.moveToFirst();
		
		if( institution != null )
		{
			while(!institution.equals(c.getString(columUsed)))
			{
				c.moveToNext();
			
				//check to see if we have moved past the last item in the cursor, if so return current i.
				if(c.isAfterLast())
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
	
	public String getInstitution()
	{
		return institutionSelected;
	}
	
	public String getInstitutionId()
	{
		return institutionId;
	}
	
	public String getAccountNumber()
	{
		String acctNumber = this.accountNumber.getText().toString();
		
		return acctNumber;
	}
	
	public String getIBAN()
	{
		String iban = this.accountIBAN.getText().toString();
		
		return iban;
	}
	
	public void putUseInstitution(boolean useInst)
	{
		this.bUseInst = useInst;
		//checkboxNoInstitution.setChecked(useInst);
	}
	
	public void putInstitutionId(String id)
	{
		institutionId = id;
		columnUsed = 0;
	}
	
	public void putAccountNumber(String ACNumber)
	{
		this.strAccountNumber = ACNumber;
		//accountNumber.setText(ACNumber);
	}
	
	public void putIBAN(String iban)
	{
		this.strIBAN = iban;
		//accountIBAN.setText(iban);
	}
	
	public void sendInstitutionData()
	{
		Log.d(TAG, "Asking for data from the parent...");
		onSendInstitutionData.onSendInstitutionData();
	}
	
	private void updateUIElements()
	{
		Log.d(TAG, "Updating UI");
		SharedPreferences prefs = getActivity().getPreferences(Activity.MODE_PRIVATE);
		String strAN = prefs.getString("AccountNumber", null);
		String strIB = prefs.getString("IBAN", null);
		String strId = prefs.getString("InstitutionId", null);
		boolean buseIns = prefs.getBoolean("UseInstitution", true);
			
		// We need to do the inverse of whatever buseIns is.
		if( prefs.contains("UseInstitution") )
			checkboxNoInstitution.setChecked(!buseIns);
		else
			checkboxNoInstitution.setChecked(!this.bUseInst);
		if( strId != null )
			spinInstitutions.setSelection(setInstitution(strId, columnUsed));
		else
			spinInstitutions.setSelection(setInstitution(this.institutionId, columnUsed));
		if( strAN != null )
			accountNumber.setText(strAN);
		else
			accountNumber.setText(this.strAccountNumber);
		if( strIB != null )
			accountIBAN.setText(strIB);
		else
			accountIBAN.setText(this.strIBAN);
	}
	
	public Bundle getInstitutionBunde()
	{
		Bundle bundleInst = new Bundle();
		
		bundleInst.putString("institutionSelected", this.getInstitution());
		bundleInst.putString("institutionId", this.getInstitutionId());
		bundleInst.putString("strAccountNumber", this.getAccountNumber());
		bundleInst.putString("strIBAN", this.getIBAN());
		bundleInst.putBoolean("UseInst", this.getUseInstitution());
		
		return bundleInst;
	}
}