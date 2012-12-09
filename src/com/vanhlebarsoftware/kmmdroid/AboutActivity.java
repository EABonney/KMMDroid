package com.vanhlebarsoftware.kmmdroid;

import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.widget.TextView;
import android.support.v4.content.*;
import android.net.*;
import android.widget.*;
import android.view.Window;

public class AboutActivity extends FragmentActivity implements
								LoaderManager.LoaderCallbacks<Cursor>
{
	private static final String TAG = AboutActivity.class.getSimpleName();
	private static final String[] FROM = { "version", "created", "lastModified", "institutions",
											"accounts", "payees", "transactions", "splits",
											"securities", "prices", "currencies", "schedules",
											"reports", "kvps", "budgets" };
	private static final int[] TO = { R.id.aboutFileVersion, R.id.aboutFileCreated, R.id.aboutLastModified, R.id.aboutNumInstitutions,
		R.id.aboutNumAccounts, R.id.aboutNumPayees, R.id.aboutNumTransactions, R.id.aboutNumSplits,
		R.id.aboutNumSecurities, R.id.aboutNumPrices, R.id.aboutNumCurrencies, R.id.aboutNumSchedules,
		R.id.aboutNumReports, R.id.aboutNumKVPS, R.id.aboutNumBudgets };
	private static final int ABOUT_LOADER = 0x09;
	TextView txtAboutVers;
	ListView lvAbout;
	Cursor cursor;
	SimpleCursorAdapter adapter;
	KMMDroidApp KMMDapp;

	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.about);
        
        // Get our views
        txtAboutVers = (TextView) findViewById(R.id.aboutVerNumber);
        lvAbout = (ListView) findViewById(R.id.aboutList);
		
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
		
        // Create an empty adapter we will use to display the loaded data.
        adapter = new SimpleCursorAdapter(this,R.layout.about_row, null, FROM, TO, 0);
		lvAbout.setAdapter(adapter);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getSupportLoaderManager().initLoader(ABOUT_LOADER, null, this);
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
		
		// append the file Info into the various text views.
		String versionName = null;
		try
		{
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		}
		catch (NameNotFoundException e)
		{
			Log.e(TAG, e.getMessage());
		}
		txtAboutVers.setText(txtAboutVers.getText().toString() + " " + versionName);
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) 
	{
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
		u = Uri.parse(u.toString());
		setProgressBarIndeterminateVisibility(true);
	
		return new CursorLoader(AboutActivity.this, u,
								new String[] { "version", "created", "lastModified", "institutions",
											   "accounts", "payees", "transactions", "splits",
											   "securities", "prices", "currencies", "schedules",
											   "reports", "kvps", "budgets", "fixLevel AS _id" }, null, null, null);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) 
	{
		adapter.swapCursor(cursor);
		setProgressBarIndeterminateVisibility(false);
	}

	public void onLoaderReset(Loader<Cursor> loader) 
	{
		adapter.swapCursor(null);
	}
}
