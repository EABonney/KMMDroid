package com.vanhlebarsoftware.kmmdroid;

import com.vanhlebarsoftware.kmmdroid.CategoriesHierarchyActivity.CategoryHierarchyOnItemSelectedListener;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class CategoriesGeneralActivity extends Activity
{
	private static final String TAG = "CategoriesGeneralActivity";
	private static final String[] dbColumns = { "name", "ISOcode AS _id"};
	private static final String strSelectionType = "accountTypeString='Expense' AND (balance != '0/1')";
	private static final String strOrderBy = "name ASC";
	static final String[] FROM = { "name" };
	static final int[] TO = { android.R.id.text1 };
	EditText editCategoryName;
	EditText editCategoryBalance;
	EditText editCategoryCheckNumber;
	EditText editCategoryNotes;
	Spinner spinCategoryType;
	Spinner spinCategoryCurrency;
	DatePicker dateCategoryOpenDate;
	Cursor cursorCurrency;
	SimpleCursorAdapter adapterCurrency;
	ArrayAdapter<CharSequence> adapterTypes;
	KMMDroidApp KMMDapp;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.categories_general);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views.
        editCategoryName = (EditText) findViewById(R.id.categoryName);
        editCategoryBalance = (EditText) findViewById(R.id.categoryBalance);
        editCategoryCheckNumber = (EditText) findViewById(R.id.categoryCheckNumber);
        editCategoryNotes = (EditText) findViewById(R.id.categoryNotes);
        spinCategoryType = (Spinner) findViewById(R.id.categoryType);
        spinCategoryCurrency = (Spinner) findViewById(R.id.categoryCurrency);
        dateCategoryOpenDate = (DatePicker) findViewById(R.id.categoryDate);
        
        // Set the OnItemSelectedListeners for the spinners.
        spinCategoryType.setOnItemSelectedListener(new CategoryGeneralOnItemSelectedListener());
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		//Get all the currencies to be displayed.
		cursorCurrency = KMMDapp.db.query("kmmCurrencies", dbColumns, null, null, null, null, strOrderBy);
		startManagingCursor(cursorCurrency);
		
		// Set up the adapters
		adapterCurrency = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursorCurrency, FROM, TO);
		adapterCurrency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adapterTypes = ArrayAdapter.createFromResource(this, R.array.arrayTypes, android.R.layout.simple_spinner_dropdown_item);
		
		spinCategoryCurrency.setAdapter(adapterCurrency);
		spinCategoryType.setAdapter(adapterTypes);
	}
	
	public class CategoryGeneralOnItemSelectedListener implements OnItemSelectedListener
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			Cursor c = (Cursor) parent.getAdapter().getItem(pos);
			
			//Activity parentCategoryActivity = 
			// TODO Auto-generated method stub
			/*switch ( parent.getId() )
			{
				default:
					Log.d(TAG, "Somehow it thinks we did not select an account but we did!");
					break;
			}*/
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			// do nothing.
		}		
	}
}
