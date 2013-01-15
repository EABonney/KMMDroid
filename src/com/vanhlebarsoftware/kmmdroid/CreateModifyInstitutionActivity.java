package com.vanhlebarsoftware.kmmdroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

public class CreateModifyInstitutionActivity extends FragmentActivity
{
	private static final String TAG = "CreateModifyInstitutionsActivity";
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private int Action = 0;
	private boolean returnFromDelete = false;
	private boolean isDirty = false;
	private Institution institution = null;
	EditText instName;
	EditText instCity;
	EditText instStreet;
	EditText instPostalCode;
	EditText instPhone;
	EditText instRoutingNumber;
	EditText instBIC;
	KMMDroidApp KMMDapp;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.createmod_institution);
        
        // Find out views
        instName = (EditText) findViewById(R.id.institutionName);
        instCity = (EditText) findViewById(R.id.institutionCity);
        instStreet = (EditText) findViewById(R.id.institutionStreet);
        instPostalCode = (EditText) findViewById(R.id.institutionPostalCode);
        instPhone = (EditText) findViewById(R.id.institutionPhone);
        instRoutingNumber = (EditText) findViewById(R.id.institutionRoutingNumber);
        instBIC = (EditText) findViewById(R.id.institutionBIC);
        
        // Set up the other keyListener's for the various editText items.
        instName.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				isDirty = true;
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        instCity.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				isDirty = true;
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        instStreet.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				isDirty = true;
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        instPostalCode.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				isDirty = true;
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        instPhone.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				isDirty = true;
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        instRoutingNumber.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				isDirty = true;
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        instBIC.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				isDirty = true;
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        // Get the action the user is doing.
        Bundle extras = getIntent().getExtras();
        Action = extras.getInt("Action");
        
        if( Action == ACTION_EDIT )
        	institution = new Institution(this, extras.getString("instId"));
        else
        	institution = new Institution(this, null);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());

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
		
		if( !returnFromDelete )
		{
			if(Action == ACTION_EDIT)
			{
				if( institution.getId() != null )
				{		
					instName.setText(institution.getName());
					instBIC.setText(institution.getManager());
					instRoutingNumber.setText(institution.getRoutingCode());
					instStreet.setText(institution.getStreet());
					instCity.setText(institution.getCity());
					instPostalCode.setText(institution.getZipcode());
					instPhone.setText(institution.getTelephone());
					// Need to reset the isDirty flag to false.
					isDirty = false;
				}
				else
					Log.d(TAG, "Error! Nothing returned from our query!");
			}
		}
		else
		{
			returnFromDelete = false;
			finish();
		}
	}
	
	// Called first time the user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.save_menu, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu)
	{
		int rows = 0;
		
		// Can't delete if we are creating a new item.
		// Can't delete if we have any accounts that are associated with this institution.
		if( Action == ACTION_EDIT )
		{
			String frag = "#9999";
			Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, frag);
			u = Uri.parse(u.toString());
			Cursor c = getContentResolver().query(u, new String[] { "id" }, "institutionId=?", new String[] { institution.getId() }, null);

			if( c != null )
			{
				c.moveToFirst();
				rows = c.getCount();
			}
			c.close();
		}
		
		if( Action == ACTION_NEW || rows > 0 )
			menu.getItem(1).setVisible(false);
		
		// See if we need to show the save item or not.
		if( isDirty )
			menu.getItem(0).setVisible(true);
		else
			menu.getItem(0).setVisible(false);
		
		return true;
	}

	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.itemsave:
				// Get our data changes if any.
				institution.getDataChanges();

				if (Action == ACTION_NEW)
					institution.createId();
				
				switch (Action)
				{
					case ACTION_NEW:
						// Attempt to insert the newly created Institution.
						institution.Save();
						break;
					case ACTION_EDIT:
						// Attempt to update the Institution.
						institution.Update();
						break;
				}
				KMMDapp.updateFileInfo("lastModified", 0);
				
				//Mark file as dirty
				KMMDapp.markFileIsDirty(true, "9999");
				
				finish();
				break;
			case R.id.itemDelete:
				AlertDialog.Builder alertDel = new AlertDialog.Builder(this);
				alertDel.setTitle(R.string.delete);
				alertDel.setMessage(getString(R.string.deletemsg) + " " + instName.getText().
						toString() + "?");

				alertDel.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String frag = "#9999";
						Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_INSTITUTION_URI, frag);
						u = Uri.parse(u.toString());
						getContentResolver().delete(u, "id=?", new String[] { institution.getId() });
						frag = "#9999";
						u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
						u = Uri.parse(u.toString());
						getContentResolver().update(u, null, "institutions", new String[] { "-1" });
						returnFromDelete = true;
						KMMDapp.updateFileInfo("lastModified", 0);
						
						//Mark file as dirty
						KMMDapp.markFileIsDirty(true, "9999");
						
						finish();
					}
				});
				alertDel.setNegativeButton(getString(R.string.titleButtonCancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
						Log.d(TAG, "User cancelled delete.");
					}
					});				
				alertDel.show();
				break;
			case R.id.itemCancel:
				if( isDirty )
				{
					alertDel = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogNoTitle));
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
					finish();
				break;
		}
		return true;
	}
	
	@Override
	public void onBackPressed()
	{
		Log.d(TAG, "User clicked the back button");
		if( isDirty )
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
			finish();
	}
	// ************************************************************************************************
	// ******************************* Helper Functions ***********************************************
}
