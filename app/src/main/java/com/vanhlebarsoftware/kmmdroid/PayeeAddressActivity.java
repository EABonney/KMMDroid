package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class PayeeAddressActivity extends Fragment
{
	private final String TAG = PayeeAddressActivity.class.getSimpleName();
	private Activity ParentActivity;
	private OnSendPayeeAddressListener onSendPayeeAddressData;
	private String strName = null;
	private String strAddress = null;
	private String strPostalCode = null;
	private String strPhone = null;
	private String strEmail = null;
	private String strNotes = null;
	EditText payeeName;
	EditText payeeAddress;
	EditText payeePostalCode;
	EditText payeePhone;
	EditText payeeEmail;
	EditText payeeNotes;
	
	
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
			onSendPayeeAddressData = (OnSendPayeeAddressListener) activity;
		}
		catch(ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement OnSendpayeeAddressListener");
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
        
        View view = inflater.inflate(R.layout.payee_address, container, false);
        
        // Get our Views
        payeeName = (EditText) view.findViewById(R.id.payeeName);
        payeeAddress = (EditText) view.findViewById(R.id.payeeAddress);
        payeePostalCode = (EditText) view.findViewById(R.id.payeePostalCode);
        payeePhone = (EditText) view.findViewById(R.id.payeeTelephone);
        payeeEmail = (EditText) view.findViewById(R.id.payeeEmail);
        payeeNotes = (EditText) view.findViewById(R.id.payeeNotes); 
        
        // Set up the other keyListener's for the various editText items.
        payeeName.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				((CreateModifyPayeeActivity) ParentActivity).setIsDirty(true);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        payeeAddress.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				((CreateModifyPayeeActivity) ParentActivity).setIsDirty(true);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        payeePostalCode.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				((CreateModifyPayeeActivity) ParentActivity).setIsDirty(true);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        payeePhone.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				((CreateModifyPayeeActivity) ParentActivity).setIsDirty(true);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        payeeEmail.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				((CreateModifyPayeeActivity) ParentActivity).setIsDirty(true);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        payeeNotes.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				((CreateModifyPayeeActivity) ParentActivity).setIsDirty(true);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
		return view;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
    }
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		sendPayeeAddressData();
		
		updateUIElements();
	}
	// *******************************************************************************************************
	// *********************************** Public Event Interfaces *******************************************
	public interface OnSendPayeeAddressListener
	{
		public void onSendPayeeAddressData();
	}
	// ************************************************************************************************
	// ******************************* Helper Functions ***********************************************
	public String getPayeeName()
	{
		return payeeName.getText().toString();
	}
	
	public String getPayeeAddress()
	{
		return payeeAddress.getText().toString();
	}
	
	public String getPayeePostalCode()
	{
		return payeePostalCode.getText().toString();
	}
	
	public String getPayeePhone()
	{
		return payeePhone.getText().toString();
	}
	
	public String getPayeeEmail()
	{
		return payeeEmail.getText().toString();
	}
	
	public String getPayeeNotes()
	{
		return payeeNotes.getText().toString();
	}
	
	public void putPayeeName(String name)
	{
		this.strName = name;
	}
	
	public void putPayeeAddress(String address)
	{
		this.strAddress = address;
	}
	
	public void putPayeePostalCode(String postalcode)
	{
		this.strPostalCode = postalcode;
	}
	
	public void putPayeePhone(String phone)
	{
		this.strPhone = phone;
	}
	
	public void putPayeeEmail(String email)
	{
		this.strEmail = email;
	}
	
	public void putPayeeNotes(String notes)
	{
		this.strNotes = notes;
	}
	
	public void sendPayeeAddressData()
	{
		onSendPayeeAddressData.onSendPayeeAddressData();
	}
	
	private void updateUIElements()
	{
		SharedPreferences prefs = getActivity().getPreferences(Activity.MODE_PRIVATE);
		String name = prefs.getString("Name", null);
		String address = prefs.getString("Address", null);
		String postalCode = prefs.getString("PostalCode", null);
		String telephone = prefs.getString("Telephone", null);
		String email = prefs.getString("Email", null);
		String notes = prefs.getString("Notes", null);

		if( name != null )
			payeeName.setText(name);
		else
			payeeName.setText(this.strName);
		
		if( address != null )
			payeeAddress.setTag(address);
		else
			payeeAddress.setText(this.strAddress);
		
		if( postalCode != null )
			payeePostalCode.setText(postalCode);
		else
			payeePostalCode.setText(this.strPostalCode);
		
		if( telephone != null )
			payeePhone.setText(telephone);
		else
			payeePhone.setText(this.strPhone);
		
		if( email != null )
			payeeEmail.setText(email);
		else
			payeeEmail.setText(this.strEmail);
		
		if( notes != null )
			payeeNotes.setText(notes);
		else
			payeeNotes.setText(this.strNotes);
	}
}
