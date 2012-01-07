package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class CreateModifySplitsActivity extends Activity implements OnClickListener
{
	private static final String TAG = "CreateModifySplitsActivity";
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	static final int[] TO = { android.R.id.text1 };
	static final String[] FROM = { "accountName" };
	private int Action = ACTION_NEW;
	String strCategoryName = null;
	String strAccountId = null;
	String strTranAmount = null;
	String strLabelTotal =null;
	String strLabelSumofSplits = null;
	String strLabelUnassigned = null;
	int intInsertRowAt = 1;
	int rowClicked = 0;
	ArrayList<String> AccountIdList;
	TextView txtSumSplits;
	TextView txtUnassigned;
	TextView txtTransAmount;
	EditText editSplitMemo;
	EditText editSplitAmount;
	TableLayout tableSplits;
	TableRow rowSplitEntry;
	Spinner spinCategory;
	Cursor cursorCategories;
	SimpleCursorAdapter adapterCategories;
	KMMDroidApp KMMDapp;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.createmod_splits);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views
        editSplitMemo = (EditText) findViewById(R.id.splitMemo);
        editSplitAmount = (EditText) findViewById(R.id.splitAmount);
        spinCategory = (Spinner) findViewById(R.id.splitCategory);
        tableSplits = (TableLayout) findViewById(R.id.TableAddSplits);
        rowSplitEntry = (TableRow) findViewById(R.id.splitEntryRow);
        txtSumSplits = (TextView) findViewById(R.id.SumofSplits);
        txtUnassigned = (TextView) findViewById(R.id.Unassigned);
        txtTransAmount = (TextView) findViewById(R.id.TransAmount);
        
        // Get the action the user is doing.
        Bundle extras = getIntent().getExtras();
        Action = extras.getInt("Action");
        strTranAmount = extras.getString("TransAmount");
        
        // Set the OnItemSelectedListeners for the spinners.
        spinCategory.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
        // Make the column non-stretchable and shrinkable.
        tableSplits.setColumnShrinkable(0, true);
        tableSplits.setColumnStretchable(0, false);
        
        // Get our constants for the labels.
        strLabelSumofSplits = this.getString(R.string.titleSumofSplits);
        strLabelUnassigned = this.getString(R.string.titleUnassigned);
        strLabelTotal = this.getString(R.string.titleTransactionAmount);
        
        // Update the totals for the splits, unassigned and transaction amount.
        // check to see if strTranAmount is empty (user didn't enter an amount yet) if so make it 0.00
        if( strTranAmount.isEmpty() )
        	strTranAmount = "0.00";
        updateTotals();
        
        //Initialize our array list.
        AccountIdList = new ArrayList<String>();
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
		
		cursorCategories = KMMDapp.db.query("kmmAccounts", new String[] { "accountName", "id AS _id" },
				"(accountTypeString='Expense' OR accountTypeString='Income')", null, null, null, "accountName ASC");
		startManagingCursor(cursorCategories);
		
		// Set up the adapters
		adapterCategories = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursorCategories, FROM, TO);
		adapterCategories.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinCategory.setAdapter(adapterCategories);
		
		if(Action == ACTION_EDIT)
		{
			// Create the rows for the splits that need to be displayed
			for(int i=0; i < KMMDapp.Splits.size(); i++)
			{
				String strCategory = getCategoryName(KMMDapp.Splits.get(i).getAccountId());
				insertNewRow(strCategory, KMMDapp.Splits.get(i).getMemo(), KMMDapp.Splits.get(i).getValueFormatted());
			}
		}
	}
	
	// Called first time the user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.splits_menu, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu)
	{
		// See if we need to display the Clear All menu item.
		if(intInsertRowAt == 1)
			menu.getItem(4).setVisible(false);
		else
			menu.getItem(4).setVisible(true);
		
		// See if we need to display the Delete option.
		if(rowClicked != 0)
			menu.getItem(3).setVisible(true);
		else
			menu.getItem(3).setVisible(false);
		
		return true;
	}
	
	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.itemInsertRow:
				insertNewRow(strCategoryName, editSplitMemo.getText().toString(), editSplitAmount.getText().toString());
				AccountIdList.add(strAccountId);
				strAccountId = null;
				updateTotals();
				break;
			case R.id.itemClearAll:
				for(int i = intInsertRowAt; i > 1; i--)
				{
					tableSplits.removeViewAt(intInsertRowAt - 1);
					intInsertRowAt = intInsertRowAt - 1;
				}
				updateTotals();
				break;
			case R.id.itemDelete:
				tableSplits.removeViewAt(rowClicked);
				AccountIdList.remove(rowClicked - 1);
				rowClicked = 0;
				intInsertRowAt = intInsertRowAt - 1;
				updateTotals();
				break;
			case R.id.itemCancel:
				finish();
				break;
			case R.id.itemsave:
				float amount = updateTotals();
				if( amount != 0 )
				{
					AlertDialog.Builder alertDel = new AlertDialog.Builder(this);
					alertDel.setTitle(R.string.titleSplitConfirmation);
					alertDel.setMessage(getString(R.string.titleSplitsMessage));

					alertDel.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							saveSplits();
							KMMDapp.flSplitsTotal = updateTotals();
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
				}
				else
				{
					saveSplits();
					finish();
				}
				break;
		}
		return true;
	}
	
	public class AccountOnItemSelectedListener implements OnItemSelectedListener
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			switch( parent.getId())
			{
				case R.id.splitCategory:
					Cursor c = (Cursor) parent.getAdapter().getItem(pos);
					strCategoryName = c.getString(0).toString();
					strAccountId = c.getString(1).toString();
					//c.close();
					break;
				default:
					Log.d(TAG, "parentId: " + String.valueOf(parent.getId()));
					break;
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			// do nothing.
		}		
	}
	
	public void onClick(View v) 
	{
		// TODO Auto-generated method stub
		View p = (View) v.getParent().getParent();
		if( p instanceof TableLayout )
		{
			for(int i=1; i < ((TableLayout) p).getChildCount(); i++)
			{
				if( ((TableLayout) p).getChildAt(i).hasFocus() )
				{
					rowClicked = i;
					TableRow row = (TableRow)((TableLayout) p).getChildAt(i);
					EditText e = (EditText) row.getChildAt(1);
					editSplitMemo.setText(e.getText().toString());
					e = (EditText) row.getChildAt(2);
					editSplitAmount.setText(e.getText().toString());
					e = (EditText) row.getChildAt(0);
					spinCategory.setSelection(setCategory(e.getText().toString()));
					
					// Remove the row selected for edit.
					tableSplits.removeViewAt(i);
					// Update the next location for the next row
					intInsertRowAt = intInsertRowAt - 1;
					// remove the parent associated with the edit row first.
					tableSplits.removeView(rowSplitEntry);
					tableSplits.addView(rowSplitEntry, i);
					break;
				}
			}
		}
		else
			Toast.makeText(getApplicationContext(), "Have an issue!", Toast.LENGTH_LONG).show();
	}
	
	// **************************************************************************************************
	// ************************************ Helper methods **********************************************
	private int setCategory(String categoryName)
	{
		int i = 0;
		cursorCategories.moveToFirst();
		
		if( categoryName != null )
		{
			while(!categoryName.equals(cursorCategories.getString(0)))
			{
				cursorCategories.moveToNext();
			
				//check to see if we have moved past the last item in the cursor, if so return current i.
				if(cursorCategories.isAfterLast())
					return i;
			
				i++;
			}
		}
		return i;
	}
	
	private void insertNewRow(String CatName, String Memo, String Amount)
	{
		TableRow tr = new TableRow(this);
		EditText et2 = new EditText(this);
		EditText et3 = new EditText(this);
		EditText et4 = new EditText(this);
		
		// make it so the new rows elements are not editable.
		et2.setKeyListener(null);
		et3.setKeyListener(null);
		et4.setKeyListener(null);
		
		// set the OnClickListener for each EditText
		et2.setOnClickListener(this);
		et3.setOnClickListener(this);
		et4.setOnClickListener(this);
		
		// move the current rows data to the newly created row.
		et2.setText(CatName);
		et3.setText(Memo);
		et4.setText(Amount);
		
		// remove the current rows data.
		spinCategory.setSelection(0);
		editSplitMemo.setText("");
		editSplitAmount.setText("");
		
		// Create the new row and insert it into the table.
		tr.addView(et2);
		tr.addView(et3);
		tr.addView(et4);
		
		tableSplits.addView(tr, intInsertRowAt);
		intInsertRowAt = intInsertRowAt + 1;
		
		// Remove the old entry row.
		tableSplits.removeView(rowSplitEntry);
		
		// add the entry row at the bottom of the table.
		tableSplits.addView(rowSplitEntry, intInsertRowAt);
		
		// ensure that the Delete menu option is disabled.
		rowClicked = 0;
	}
	
	private String getCategoryName(String CategoryId)
	{
		Cursor c = KMMDapp.db.query("kmmAccounts", new String[] { "accountName" }, "id=?", new String[] { CategoryId }, null, null, null);
		startManagingCursor(c);
		c.moveToFirst();
		String name = c.getString(0);
		c.close();
		return name;
	}
	
	// **************************************************************************************************
	// ************************************ Helper methods **********************************************
	private float updateTotals()
	{
		float flSumofSplits = 0;
		float flUnassigned = 0;
		float flTotal = Float.valueOf(strTranAmount);
		
		// Calculate the sum of all splits entered.
		if( tableSplits.getChildCount() > 2 )
		{
			for(int i=1; i < tableSplits.getChildCount() - 1; i++)
			{
				TableRow row = (TableRow) tableSplits.getChildAt(i);
				EditText splitAmount = (EditText) row.getChildAt(2);
				flSumofSplits = flSumofSplits + Float.valueOf(splitAmount.getText().toString());
			}
		}
		
		flUnassigned = flTotal - flSumofSplits;
		
		// Finally append the new values to the TextViews.
		txtSumSplits.setText(strLabelSumofSplits + " " + String.valueOf(flSumofSplits));
		txtUnassigned.setText(strLabelUnassigned + " " + String.valueOf(flUnassigned));
		txtTransAmount.setText(strLabelTotal + " " + String.valueOf(flTotal));
		
		return flSumofSplits;
	}
	
	private void saveSplits()
	{
		//String strCategory = null;
		String strMemo = null;
		String strAmount = null;
		// See if we need clear out the KMMDapp.Splits ArrayList
		if(Action == ACTION_EDIT)
			KMMDapp.Splits.clear();
	
		for(int i=1; i < tableSplits.getChildCount() - 1; i++)
		{
			TableRow row = (TableRow) tableSplits.getChildAt(i);
			EditText e = (EditText) row.getChildAt(0);
			//strCategory = e.getText().toString();
			e = (EditText) row.getChildAt(1);
			strMemo = e.getText().toString();
			e = (EditText) row.getChildAt(2);
			strAmount = e.getText().toString();
			// Create the split in the Splits Array.
			KMMDapp.Splits.add(new Split("", "N", i+1, "", "", "", "", "", strAmount, "", strAmount, "", strAmount, 
					strMemo, AccountIdList.get(i-1), "", "", ""));
		}
	}
}
