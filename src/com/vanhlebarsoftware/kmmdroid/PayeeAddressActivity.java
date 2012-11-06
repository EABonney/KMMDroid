package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.EditText;

public class PayeeAddressActivity extends Activity
{
	private final String TAG = PayeeAddressActivity.class.getSimpleName();
	private String strAddress = null;
	private CreateModifyPayeeActivity parentTabHost;
	EditText payeeName;
	EditText payeeAddress;
	EditText payeePostalCode;
	EditText payeePhone;
	EditText payeeEmail;
	EditText payeeNotes;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payee_address);
        
        // Get the tabHost on the parent.
        parentTabHost = ((CreateModifyPayeeActivity) this.getParent());
        
        // Get our Views
        payeeName = (EditText) findViewById(R.id.payeeName);
        payeeAddress = (EditText) findViewById(R.id.payeeAddress);
        payeePostalCode = (EditText) findViewById(R.id.payeePostalCode);
        payeePhone = (EditText) findViewById(R.id.payeeTelephone);
        payeeEmail = (EditText) findViewById(R.id.payeeEmail);
        payeeNotes = (EditText) findViewById(R.id.payeeNotes); 
        
        // Set up the other keyListener's for the various editText items.
        payeeName.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				parentTabHost.setIsDirty(true);
				Log.d(TAG, "changing isDirty from accountName!");
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
				parentTabHost.setIsDirty(true);
				Log.d(TAG, "changing isDirty from accountName!");
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
				parentTabHost.setIsDirty(true);
				Log.d(TAG, "changing isDirty from accountName!");
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
				parentTabHost.setIsDirty(true);
				Log.d(TAG, "changing isDirty from accountName!");
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
				parentTabHost.setIsDirty(true);
				Log.d(TAG, "changing isDirty from accountName!");
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
				parentTabHost.setIsDirty(true);
				Log.d(TAG, "changing isDirty from accountName!");
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
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
		payeeName.setText(name);
	}
	
	public void putPayeeAddress(String address)
	{
		payeeAddress.setText(address);
	}
	
	public void putPayeePostalCode(String postalcode)
	{
		payeePostalCode.setText(postalcode);
	}
	
	public void putPayeePhone(String phone)
	{
		payeePhone.setText(phone);
	}
	
	public void putPayeeEmail(String email)
	{
		payeeEmail.setText(email);
	}
	
	public void putPayeeNotes(String notes)
	{
		payeeNotes.setText(notes);
	}
}
