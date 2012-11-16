package com.vanhlebarsoftware.kmmdroid;

import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends Activity
{
	private static final String TAG = AboutActivity.class.getSimpleName();
    public static final String LOG_COLLECTOR_PACKAGE_NAME = "com.xtralogic.android.logcollector";//$NON-NLS-1$
    public static final String ACTION_SEND_LOG = "com.xtralogic.logcollector.intent.action.SEND_LOG";//$NON-NLS-1$
    public static final String EXTRA_SEND_INTENT_ACTION = "com.xtralogic.logcollector.intent.extra.SEND_INTENT_ACTION";//$NON-NLS-1$
    public static final String EXTRA_DATA = "com.xtralogic.logcollector.intent.extra.DATA";//$NON-NLS-1$
    public static final String EXTRA_ADDITIONAL_INFO = "com.xtralogic.logcollector.intent.extra.ADDITIONAL_INFO";//$NON-NLS-1$
    public static final String EXTRA_SHOW_UI = "com.xtralogic.logcollector.intent.extra.SHOW_UI";//$NON-NLS-1$
    public static final String EXTRA_FILTER_SPECS = "com.xtralogic.logcollector.intent.extra.FILTER_SPECS";//$NON-NLS-1$
    public static final String EXTRA_FORMAT = "com.xtralogic.logcollector.intent.extra.FORMAT";//$NON-NLS-1$
    public static final String EXTRA_BUFFER = "com.xtralogic.logcollector.intent.extra.BUFFER";//$NON-NLS-1$
	private static int VERSION = 0;
	private static int CREATED = 1;
	private static int LASTMODIFIED = 2;
	private static int INSTITUTIONS = 3;
	private static int ACCOUNTS = 4;
	private static int PAYEES = 5;
	private static int TRANSACTIONS = 6;
	private static int SPLITS = 7;
	private static int SECURITIES = 8;
	private static int PRICES = 9;
	private static int CURRENCIES = 10;
	private static int SCHEDULES = 11;
	private static int REPORTS = 12;
	private static int KVPS = 13;
	private static int BUDGETS = 14;
	TextView txtAboutVers;
	TextView txtVersion;
	TextView txtCreated;
	TextView txtLastMod;
	TextView txtInst;
	TextView txtAccounts;
	TextView txtPayees;
	TextView txtTrans;
	TextView txtSplits;
	TextView txtSecurities;
	TextView txtPrices;
	TextView txtCurrencies;
	TextView txtSchedules;
	TextView txtReports;
	TextView txtKVPS;
	TextView txtBudgets;
	Cursor cursor;
	KMMDroidApp KMMDapp;
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        
        // Get our views
        txtAboutVers = (TextView) findViewById(R.id.aboutVerNumber);
        txtVersion = (TextView) findViewById(R.id.aboutFileVersion);
        txtCreated = (TextView) findViewById(R.id.aboutFileCreated);
        txtLastMod = (TextView) findViewById(R.id.aboutLastModified);
        txtInst = (TextView) findViewById(R.id.aboutNumInstitutions);
        txtAccounts = (TextView) findViewById(R.id.aboutNumAccounts);
        txtPayees = (TextView) findViewById(R.id.aboutNumPayees);
        txtTrans = (TextView) findViewById(R.id.aboutNumTransactions);
        txtSplits = (TextView) findViewById(R.id.aboutNumSplits);
        txtSecurities = (TextView) findViewById(R.id.aboutNumSecurities);
        txtPrices = (TextView) findViewById(R.id.aboutNumPrices);
        txtCurrencies = (TextView) findViewById(R.id.aboutNumCurrencies);
        txtSchedules = (TextView) findViewById(R.id.aboutNumSchedules);
        txtReports = (TextView) findViewById(R.id.aboutNumReports);
        txtKVPS = (TextView) findViewById(R.id.aboutNumKVPS);
        txtBudgets = (TextView) findViewById(R.id.aboutNumBudgets);
        
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
		
		cursor = KMMDapp.db.query("kmmFileInfo", new String[] { "version", "created", "lastModified", "institutions",
																"accounts", "payees", "transactions", "splits",
																"securities", "prices", "currencies", "schedules",
																"reports", "kvps", "budgets"}, 
				null, null, null, null, null);
		startManagingCursor(cursor);
		cursor.moveToFirst();
		
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
		txtVersion.setText(txtVersion.getText().toString() + " " + cursor.getString(VERSION));
		txtCreated.setText(txtCreated.getText().toString() + " " + cursor.getString(CREATED));
		txtLastMod.setText(txtLastMod.getText().toString() + " " + cursor.getString(LASTMODIFIED));
		txtInst.setText(txtInst.getText().toString() + " " + cursor.getString(INSTITUTIONS));
		txtAccounts.setText(txtAccounts.getText().toString() + " " + cursor.getString(ACCOUNTS));
		txtPayees.setText(txtPayees.getText().toString() + " " + cursor.getString(PAYEES));
		txtTrans.setText(txtTrans.getText().toString() + " " + cursor.getString(TRANSACTIONS));
		txtSplits.setText(txtSplits.getText().toString() + " " + cursor.getString(SPLITS));
		txtSecurities.setText(txtSecurities.getText().toString() + " " + cursor.getString(SECURITIES));
		txtPrices.setText(txtPrices.getText().toString() + " " + cursor.getString(PRICES));
		txtCurrencies.setText(txtCurrencies.getText().toString() + " " + cursor.getString(CURRENCIES));
		txtSchedules.setText(txtSchedules.getText().toString() + " " + cursor.getString(SCHEDULES));
		txtReports.setText(txtReports.getText().toString() + " " + cursor.getString(REPORTS));
		txtKVPS.setText(txtKVPS.getText().toString() + " " + cursor.getString(KVPS));
		txtBudgets.setText(txtBudgets.getText().toString() + " " + cursor.getString(BUDGETS));
	}
}
