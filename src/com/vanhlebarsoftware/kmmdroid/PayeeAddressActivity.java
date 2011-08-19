package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

public class PayeeAddressActivity extends Activity
{
	private String strAddress = null;
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
        
        // Get our Views
        payeeAddress = (EditText) findViewById(R.id.payeeAddress);
        payeePostalCode = (EditText) findViewById(R.id.payeePostalCode);
        payeePhone = (EditText) findViewById(R.id.payeeTelephone);
        payeeEmail = (EditText) findViewById(R.id.payeeEmail);
        payeeNotes = (EditText) findViewById(R.id.payeeNotes);       
        
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
}
