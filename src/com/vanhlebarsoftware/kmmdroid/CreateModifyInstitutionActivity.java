package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

public class CreateModifyInstitutionActivity extends Activity
{
	private static final String TAG = "CreateModifyInstitutionsActivity";
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private static final int INSTUTION_ID = 0;
	private static final int INSTUTION_NAME = 1;
	private static final int INSTUTION_MANAGER = 2;
	private static final int INSTUTION_ROUTINGCODE = 3;
	private static final int INSTUTION_ADDRESSSTREET = 4;
	private static final int INSTUTION_ADDRESSCITY = 5;
	private static final int INSTUTION_ADDRESSZIPCODE = 6;
	private static final int INSTUTION_TELEPHONE = 7;
	private int Action = 0;
	private String instId = null;
	private boolean returnFromDelete = false;
	private boolean isDirty = false;
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
        	instId = extras.getString("instId");
        
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
				Log.d(TAG, "instId: " + instId);
				// Get the attributes for the selected institution and populate the views.
				Cursor cursor = KMMDapp.db.query("kmmInstitutions", new String[] { "*" }, "id=?", 
						new String[] { instId }, null, null, null);
				startManagingCursor(cursor);
				if( cursor.getCount() == 1)
				{
					cursor.moveToFirst();
		
					instName.setText(cursor.getString(INSTUTION_NAME));
					instBIC.setText(cursor.getString(INSTUTION_MANAGER));
					instRoutingNumber.setText(cursor.getString(INSTUTION_ROUTINGCODE));
					instStreet.setText(cursor.getString(INSTUTION_ADDRESSSTREET));
					instCity.setText(cursor.getString(INSTUTION_ADDRESSCITY));
					instPostalCode.setText(cursor.getString(INSTUTION_ADDRESSZIPCODE));
					instPhone.setText(cursor.getString(INSTUTION_TELEPHONE));
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
			Cursor c = KMMDapp.db.query("kmmAccounts", new String[] { "id" }, 
				"institutionId=?", new String[] { instId }, null, null, null);
			startManagingCursor(c);
			rows = c.getCount();
			c.deactivate();
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
				// create the ContentValue pairs
				ContentValues valuesInst = new ContentValues();
				valuesInst.put("name", instName.getText().toString());
				valuesInst.put("routingCode", instRoutingNumber.getText().toString());
				valuesInst.put("addressStreet", instStreet.getText().toString());
				valuesInst.put("addressCity", instCity.getText().toString());
				valuesInst.put("addressZipcode", instPostalCode.getText().toString());
				valuesInst.put("telephone", instPhone.getText().toString());
				valuesInst.put("manager", instBIC.getText().toString());
				
				String id = null;
				if (Action == ACTION_NEW)
					id = createId();
				else
					id = instId;
				valuesInst.put("id", id);
				
				switch (Action)
				{
					case ACTION_NEW:
						// Attempt to insert the newly created Payee.
						try 
						{
							KMMDapp.db.insertOrThrow("kmmInstitutions", null, valuesInst);
							KMMDapp.updateFileInfo("hiInstitutionId", 1);
							KMMDapp.updateFileInfo("institutions", 1);
						} 
						catch (SQLException e) 
						{
							// TODO Auto-generated catch block
							Log.d(TAG, "Insert error: " + e.getMessage());
						}
						increaseId();
						break;
					case ACTION_EDIT:
						KMMDapp.db.update("kmmInstitutions", valuesInst, "id=?", new String[] { instId });
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
						KMMDapp.db.delete("kmmInstitutions", "id=?", new String[] { instId });
						KMMDapp.updateFileInfo("institutions", -1);
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
	private String createId()
	{
		final String[] dbColumns = { "hiInstitutionId"};
		final String strOrderBy = "hiInstitutionId DESC";
		// Run a query to get the Institution ids so we can create a new one.
		Cursor cursor = KMMDapp.db.query("kmmFileInfo", dbColumns, null, null, null, null, strOrderBy);
		startManagingCursor(cursor);
		
		cursor.moveToFirst();

		// Since id is in I000000 format, we need to pick off the actual number then increase by 1.
		int lastId = cursor.getInt(0);
		lastId = lastId +1;
		String nextId = Integer.toString(lastId);
		
		// Need to pad out the number so we get back to our P000000 format
		String newId = "I";
		for(int i= 0; i < (6 - nextId.length()); i++)
		{
			newId = newId + "0";
		}
		
		// Tack on the actual number created.
		newId = newId + nextId;
		
		return newId;
	}
	
	private void increaseId()
	{
		final String[] dbColumns = { "hiInstitutionId" };
		final String strOrderBy = "hiInstitutionId DESC";
		
		Cursor cursor = KMMDapp.db.query("kmmFileInfo", dbColumns, null, null, null, null, strOrderBy);
		startManagingCursor(cursor);
		cursor.moveToFirst();
		
		int id = cursor.getInt(0);
		id = id + 1;
		
		ContentValues values = new ContentValues();
		values.put("hiInstitutionId", id);
		
		KMMDapp.db.update("kmmFileInfo", values, null, null);		
	}
}
