package com.vanhlebarsoftware.kmmdroid;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
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
	private static final int ACTION_ENTER_SCHEDULE = 3;
	static final int[] TO = { android.R.id.text1 };
	static final String[] FROM = { "accountName" };
	private int Action = ACTION_NEW;
	String strCategoryName = null;
	String strAccountId = null;
	String strTranAmount = null;
	String strLabelTotal =null;
	String strLabelSumofSplits = null;
	String strLabelUnassigned = null;
	String strOrigCategoryId = null;
	String strOrigMemo = null;
	int nTransType = 0;
	int intInsertRowAt = 1;
	int rowClicked = 0;
	boolean needUpdateRows = false;
	boolean isDirty = false;
	int numOfPasses = 0;
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
        strOrigCategoryId = extras.getString("CategoryId");
        strOrigMemo = extras.getString("Memo");
        nTransType = extras.getInt("transType");
        
        // Set the OnItemSelectedListeners for the spinners and OnChangeEvents.
        spinCategory.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        editSplitAmount.addTextChangedListener(new TextWatcher()
        {
        	public void afterTextChanged(Editable arg0) 
        	{
        		isDirty = true;
        	}

        	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
        			int arg3) {}

        	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
        	{
        		needUpdateRows = true;
        	}        	
        });
        
        editSplitMemo.addTextChangedListener(new TextWatcher()
        {
        	public void afterTextChanged(Editable arg0) 
        	{
        		isDirty = true;
        	}

        	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
        			int arg3) {}

        	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}        	
        });
        
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
        updateTotals(false);
        
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
				"(accountType=? OR accountType=?)", new String[] { String.valueOf(Account.ACCOUNT_EXPENSE),
					String.valueOf(Account.ACCOUNT_INCOME) }, null, null, "accountName ASC");
		startManagingCursor(cursorCategories);
		
		// Set up the adapters
		adapterCategories = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursorCategories, FROM, TO);
		adapterCategories.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinCategory.setAdapter(adapterCategories);
		
		if(Action == ACTION_EDIT || Action == ACTION_ENTER_SCHEDULE)
		{
			// If KMMDapp.Splits.size() == 0 we didn't have any splits originally but now the user wants to add some.
			// So let's add the original category as the first row.
			if(KMMDapp.Splits.size() == 0)
			{
				insertNewRow(getCategoryName(strOrigCategoryId), strOrigMemo, strTranAmount);
				AccountIdList.add(strOrigCategoryId);
			}
			else
			{
				// Create the rows for the splits that need to be displayed
				for(int i=0; i < KMMDapp.Splits.size(); i++)
				{
					String strCategory = getCategoryName(KMMDapp.Splits.get(i).getAccountId());
					AccountIdList.add(KMMDapp.Splits.get(i).getAccountId());
					insertNewRow(strCategory, KMMDapp.Splits.get(i).getMemo(), KMMDapp.Splits.get(i).getValueFormatted());
				}
			}
			
			// Need to reset the isDirty flag back to false.
			isDirty = false;
		}
		
        // Set up the footer correctly.
		updateTotals(false);
        txtTransAmount.setText(strLabelTotal + " " + strTranAmount);
        Log.d(TAG, "isDirty: " + isDirty);
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
		
		// See if we need to display the Save menu item.
		if(isDirty)
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
			case R.id.itemInsertRow:
				if( rowClicked == 0 )
					AccountIdList.add(strAccountId);
				else
					AccountIdList.set(rowClicked - 1, strAccountId);
				
				strAccountId = null;
				insertNewRow(strCategoryName, editSplitMemo.getText().toString(), editSplitAmount.getText().toString());
				updateTotals(false);
				needUpdateRows = false;
				isDirty = true;
		        Log.d(TAG, "At itemInsertRow, isDirty: " + isDirty);
				break;
			case R.id.itemClearAll:
				for(int i = intInsertRowAt; i > 1; i--)
				{
					tableSplits.removeViewAt(intInsertRowAt - 1);
					intInsertRowAt = intInsertRowAt - 1;
				}
				updateTotals(false);
				isDirty = true;
		        Log.d(TAG, "At itemClearAll, isDirty: " + isDirty);
				break;
			case R.id.itemDelete:
				tableSplits.removeViewAt(rowClicked);
				AccountIdList.remove(rowClicked - 1);
				rowClicked = 0;
				intInsertRowAt = intInsertRowAt - 1;
				updateTotals(false);
				isDirty = true;
		        Log.d(TAG, "At itemDelete, isDirty: " + isDirty);
				break;
			case R.id.itemCancel:
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
				break;
			case R.id.itemsave:
				// See if we need to update the rows because user has not "inserted a new row yet"
				if( needUpdateRows )
				{
					if( rowClicked == 0 )
						AccountIdList.add(strAccountId);
					else
						AccountIdList.set(rowClicked - 1, strAccountId);
					
					strAccountId = null;					
					insertNewRow(strCategoryName, editSplitMemo.getText().toString(), editSplitAmount.getText().toString());
					updateTotals(false);
					needUpdateRows = false;					
				}
				
				long amount = updateTotals(false);
				if( amount != 0 )
				{
					AlertDialog.Builder alertDel = new AlertDialog.Builder(this);
					alertDel.setTitle(R.string.titleSplitConfirmation);
					alertDel.setMessage(getString(R.string.titleSplitsMessage));

					alertDel.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							saveSplits();
							KMMDapp.flSplitsTotal = updateTotals(true);
							KMMDapp.setSplitsAryDirty(true);
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
					KMMDapp.setSplitsAryDirty(true);
					finish();
				}
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
					if( numOfPasses > KMMDapp.Splits.size() )
						isDirty = true;
					else
						numOfPasses++;
			        Log.d(TAG, "At splitCateogry, isDirty: " + isDirty);
					break;
				default:
					Log.d(TAG, "parentId: " + String.valueOf(parent.getId()));
					break;
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// do nothing.
		}		
	}
	
	public void onClick(View v) 
	{
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
			needUpdateRows = true;
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

	private long updateTotals(boolean bfinal)
	{
		long lSumofSplits = 0;
		long lUnassigned = 0;
		long lTotal = Transaction.convertToPennies(strTranAmount);
		
		// Calculate the sum of all splits entered.
		if( tableSplits.getChildCount() > 2 )
		{
			for(int i=1; i < tableSplits.getChildCount() - 1; i++)
			{
				TableRow row = (TableRow) tableSplits.getChildAt(i);
				EditText splitAmount = (EditText) row.getChildAt(2);
				lSumofSplits = lSumofSplits + Transaction.convertToPennies(splitAmount.getText().toString());
			}
		}
		
		if( nTransType == Transaction.DEPOSIT)
			lUnassigned = lTotal + lSumofSplits;
		else
			lUnassigned = lTotal - lSumofSplits;
		
		// Finally append the new values to the TextViews.
		txtSumSplits.setText(strLabelSumofSplits + " " + Transaction.convertToDollars(lSumofSplits, true));
		txtUnassigned.setText(strLabelUnassigned + " " + Transaction.convertToDollars(lUnassigned, true));

		
		// If we are finished with our editing and we are out of balance we need to return the new sum of the transaction.
		if(bfinal)
			return lSumofSplits;
		else
			return lUnassigned;
	}
	
	private void saveSplits()
	{
		DecimalFormat decimal = new DecimalFormat();
		char decChar = decimal.getDecimalFormatSymbols().getDecimalSeparator();
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
			// We need to take our editAmount string which "may" contain a '.' as the decimal and replace it with the localized seperator.
			strAmount = e.getText().toString().replace('.', decChar);

			// We need to strip out an formatting the user put in except the "decimal" indicator.
			String strFormattedAmt = Transaction.convertToDollars(Transaction.convertToPennies(strAmount), false);
			// Need to take the user's amount and create the reduced fraction.
			String fraction = Account.createBalance(Transaction.convertToPennies(strAmount));
			// Create the split in the Splits Array.
			KMMDapp.Splits.add(new Split("", "N", i+1, "", "", "", "", fraction, strFormattedAmt, fraction, strFormattedAmt,
											fraction, strFormattedAmt,	strMemo, AccountIdList.get(i-1), "", "", ""));
			
			// Mark splits as dirty.
			KMMDapp.setSplitsAryDirty(true);
		}
	}
}
