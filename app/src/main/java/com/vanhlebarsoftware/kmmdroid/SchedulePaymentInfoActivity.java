package com.vanhlebarsoftware.kmmdroid;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.vanhlebarsoftware.kmmdroid.CategoriesGeneralActivity.OnSendGeneralDataListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
//import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class SchedulePaymentInfoActivity extends Fragment implements
											AccountFragment.OnSendWidgetIdListener,
											CategoryFragment.OnSendWidgetIdListener,
											CategoryFragment.OnSplitsClickedListener,
											PayeeFragment.OnSendWidgetIdListener
{
	private static final String TAG = SchedulePaymentInfoActivity.class.getSimpleName();
	private static final int SCHPAYMENTACCOUNTS_LOADER = 0x21;
	private static final int SCHPAYMENTPAEES_LOADER = 0x22;
	private static final int SCHPAYMENTCATEGORIES_LOADER = 0x23;
	private static final int ACTION_NEW = 1;
	private OnSendPaymentInfoListener onSendPaymentInfo;
	static final String[] FROM = { "name" };
	static final String[] FROM1 = { "accountName" };
	static final int[] TO = { android.R.id.text1 };
	static final int SET_DATE_ID = 0;
	private static int WITHDRAW = 2;
	private int Action = ACTION_NEW;
	private int intYear;
	private int intMonth;
	private int intDay;
	/***** variables used for keeping track of each spinner position *****/
	private int intSchPaymentMethodPos = 0;
	private int intSchTypePos = 2;
	private int intSchAccountPos = 0;
	private int intSchPayeePos = 0;
	private int intSchStatusPos = 0;
	
	private int intSchType = WITHDRAW;
	private int intSchStatus = 0;
	private int intSchFreq = 0;
	private int intSchFreqDesc = 0;
	private String strSchAccountId = null;
	private String strSchPayeeId = null;
	private String strSchCategoryId = null;		// Only used if we have ONLY one category, use splits if we have more than one.
	private String strSchFreqDesc = null;
	private String strSchPaymentMethod = null;
	private String strCategoryName = null;
	private String fromWidgetId = "9999";
	private static int iNumberofPasses = 0;
	private Activity ParentActivity;
	private int numOfSplits;
	private boolean anySplits = false;
	private boolean ReturningFromSplits = false;
	ArrayList<Split> Splits;
	ArrayList<Split> OrigSplits;
	AccountFragment accountFrag;
	CategoryFragment catFrag;
	PayeeFragment payeeFrag;
//	Button btnCategory;
	EditText editSchName;
	//EditText editCategory;
	EditText editCheckNum;
	EditText editDate;
	EditText editAmount;
	EditText editMemo;
//	ImageButton btnSplits;
	ImageButton btnSetDate;
	Spinner spinFreqNum;
	Spinner spinFreqDesc;
	Spinner spinPaymentMethod;
	Spinner spinSchType;
	Spinner spinSchAccount;
	Spinner spinPayee;
	Spinner spinCategory;
	Spinner spinStatus;
	Cursor cursorPayees;
	Cursor cursorCategories;
	Cursor cursorAccounts;
	ArrayAdapter<CharSequence> adapterSchTypes;
	ArrayAdapter<CharSequence> adapterStatus;
	ArrayAdapter<CharSequence> adapterSchFreqNum;
	ArrayAdapter<CharSequence> adapterSchFreqDesc;
	ArrayAdapter<CharSequence> adapterSchPaymentMethod;
	SimpleCursorAdapter adapterPayees;
	SimpleCursorAdapter adapterCategories;
	SimpleCursorAdapter adapterSchAccounts;
//	KMMDroidApp KMMDapp;
	AccountsLoaderCallbacks accountsLoaderCallback;
	PayeesLoaderCallbacks payeesLoaderCallback;
	CategoriesLoaderCallbacks categoriesLoaderCallback;
	
	
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
			onSendPaymentInfo = (OnSendPaymentInfoListener) activity;
		}
		catch(ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement OnSendPaymentInfoListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
        //setContentView(R.layout.schedule_paymentinfo);
        
        // Get our application
        //KMMDapp = ((KMMDroidApp) getApplication());
        
        // Get the tabHost on the parent.
        //parentTabHost = ((CreateModifyScheduleActivity) this.getParent());
        
        // See if the database is already open, if not open it Read/Write.
 //       if(!KMMDapp.isDbOpen())
 //       {
 //       	KMMDapp.openDB();
 //       }
        
        // Make sure that the KMMDapp.Splits is empty.
 //       KMMDapp.splitsDestroy();
        

        
/*        btnSetDate.setOnClickListener(new View.OnClickListener() 
        {	
			public void onClick(View v)
			{
				showDialog(SET_DATE_ID);
				parentTabHost.setIsDirty(true);
			}
		});
*/
/*        btnSplits.setOnClickListener(new View.OnClickListener() 
        {	
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), CreateModifySplitsActivity.class);
				i.putExtra("Action", Action);
				i.putExtra("TransAmount", editAmount.getText().toString());
				i.putExtra("Memo", editMemo.getText().toString());
				i.putExtra("CategoryId", strSchCategoryId);
				i.putExtra("transType", intSchType);
				startActivity(i);
			}
		});
        
        btnCategory.setOnClickListener(new View.OnClickListener()
        {			
			public void onClick(View arg0)
			{
				spinCategory.refreshDrawableState();
				spinCategory.performClick();
			}
		});
*/        

        
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
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
        
        View view = inflater.inflate(R.layout.schedule_paymentinfo, container, false);
        
        // Find our views.
        editSchName = (EditText) view.findViewById(R.id.editScheduleName);
        //editCategory = (EditText) view.findViewById(R.id.editCategory);
        editCheckNum = (EditText) view.findViewById(R.id.checkNumber);
        editDate = (EditText) view.findViewById(R.id.date);
        editAmount = (EditText) view.findViewById(R.id.amount);
        editMemo = (EditText) view.findViewById(R.id.memo);
        btnSetDate = (ImageButton) view.findViewById(R.id.buttonSetDate);
        spinFreqNum = (Spinner) view.findViewById(R.id.scheduleFrequencyNumber);
        spinFreqDesc = (Spinner) view.findViewById(R.id.scheduleFrequencyDescription);
        spinSchType = (Spinner) view.findViewById(R.id.scheduleType);
        spinStatus = (Spinner) view.findViewById(R.id.status);
        spinPaymentMethod = (Spinner) view.findViewById(R.id.schedulePaymentMethod);
        accountFrag = (AccountFragment) getFragmentManager().findFragmentById(R.id.accountFragment);        
        catFrag = (CategoryFragment) getFragmentManager().findFragmentById(R.id.categoryFragment);
        payeeFrag = (PayeeFragment) getFragmentManager().findFragmentById(R.id.payeeFragment);
        
        // Make it so the user is not able to edit the Category selected without using the Spinner.
        //editCategory.setKeyListener(null);
        
        // Set our OnClickListener events
        btnSetDate.setOnClickListener(new View.OnClickListener() 
        {	
			public void onClick(View v)
			{
				DialogFragment dateFrag = new KMMDDatePickerFragment(editDate);
				dateFrag.show(getFragmentManager(), "datePicker");
			}
		});
        
        // Set the OnItemSelectedListeners for the spinners.
        spinSchType.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        spinStatus.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        spinFreqNum.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        spinFreqDesc.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        spinPaymentMethod.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        
        // Set up the other keyListener's for the various editText items.
        editSchName.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				((CreateModifyScheduleActivity) ParentActivity).setIsDirty(true);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        editCheckNum.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				((CreateModifyScheduleActivity) ParentActivity).setIsDirty(true);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        editAmount.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				((CreateModifyScheduleActivity) ParentActivity).setIsDirty(true);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        editMemo.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				((CreateModifyScheduleActivity) ParentActivity).setIsDirty(true);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        
        // get the current date
        final Calendar c = Calendar.getInstance();
        intYear = c.get(Calendar.YEAR);
        intMonth = c.get(Calendar.MONTH);
        intDay = c.get(Calendar.DAY_OF_MONTH);
        
        // display the current date
        updateDisplay();
        
        // Initialize our Splits ArrayList.
        Splits = new ArrayList<Split>();
        OrigSplits = new ArrayList<Split>();
        
        // Set default values for a new schedule.
        strSchFreqDesc = "Once";
        
        return view;
	}
	
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
	}
	
	@Override
	public void onResume() 
	{
		super.onResume();
	
//		cursorPayees = KMMDapp.db.query("kmmpayees", new String[] { "name", "id AS _id" }, 
//				null, null, null, null, "name ASC");
//		startManagingCursor(cursorPayees);
		
		/*cursorCategories = KMMDapp.db.query("kmmAccounts", new String[] { "accountName", "id AS _id" },
				"(accountTypeString='Expense' OR accountTypeString='Income')", null, null, null, "accountName ASC");*/
//		cursorCategories = KMMDapp.db.query("kmmAccounts", new String[] { "accountName",  "id AS _id" }, "accountType=? OR accountType=?",
//				new String[] { String.valueOf(Account.ACCOUNT_EXPENSE), String.valueOf(Account.ACCOUNT_INCOME) }, null, null, "accountName ASC");
//		startManagingCursor(cursorCategories);

		/*cursorAccounts = KMMDapp.db.query("kmmAccounts", new String[] { "accountName", "id AS _id" },
				"(accountTypeString='Checking' OR accountTypeString='Savings' OR accountTypeString='Liability' OR " +
						"accountTypeString='Credit Card') AND (balance != '0/1')", null, null, null, "accountName ASC");*/
//		cursorAccounts = KMMDapp.db.query("kmmAccounts", new String[] { "accountName", "id AS _id" },
//				"(accountType=? OR accountType=? OR accountType=? OR accountType=?) AND (balance != '0/1')",
//				new String[] { String.valueOf(Account.ACCOUNT_CHECKING), String.valueOf(Account.ACCOUNT_SAVINGS), 
//				String.valueOf(Account.ACCOUNT_LIABILITY), String.valueOf(Account.ACCOUNT_CREDITCARD) }, null, null, "accountName ASC");
//		startManagingCursor(cursorAccounts);
		
		// Set up the adapters
		adapterPayees = new SimpleCursorAdapter(getActivity().getBaseContext(), android.R.layout.simple_spinner_item, cursorPayees, FROM, TO);
		adapterPayees.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinPayee.setAdapter(adapterPayees);
		
		adapterCategories = new SimpleCursorAdapter(getActivity().getBaseContext(), android.R.layout.simple_spinner_item, cursorCategories, FROM1, TO);
		adapterCategories.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinCategory.setAdapter(adapterCategories);
		
		adapterSchAccounts = new SimpleCursorAdapter(getActivity().getBaseContext(), android.R.layout.simple_spinner_item, cursorAccounts, FROM1, TO);
		adapterSchAccounts.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinSchAccount.setAdapter(adapterSchAccounts);
		
		adapterSchTypes = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.TransactionTypes, android.R.layout.simple_spinner_item);
		adapterSchTypes.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinSchType.setAdapter(adapterSchTypes);
		adapterStatus = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.TransactionStatus, android.R.layout.simple_spinner_item);
		adapterStatus.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinStatus.setAdapter(adapterStatus);
		adapterSchFreqNum = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.scheduleFrequency, android.R.layout.simple_spinner_item);
		adapterSchFreqNum.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinFreqNum.setAdapter(adapterSchFreqNum);
		adapterSchFreqDesc = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.scheduleFreqDescription, android.R.layout.simple_spinner_item);
		adapterSchFreqDesc.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinFreqDesc.setAdapter(adapterSchFreqDesc);
		adapterSchPaymentMethod = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.SchedulePaymentMethod, android.R.layout.simple_spinner_item);
		adapterSchPaymentMethod.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinPaymentMethod.setAdapter(adapterSchPaymentMethod);
		
		// Set the default items for the type, status, freq, freq desc, account, category and payment method spinners.
		spinSchType.setSelection(intSchTypePos);
		spinStatus.setSelection(intSchStatusPos);
		spinFreqNum.setSelection(intSchFreq);
		spinFreqDesc.setSelection(intSchFreqDesc);
		spinPaymentMethod.setSelection(intSchPaymentMethodPos);
//		spinSchAccount.setSelection(intSchAccountPos);
//		spinPayee.setSelection(intSchPayeePos);
		
		// Setup the category Name
/*		if(strSchCategoryId != null)
		{
			spinCategory.setSelection(setCategoryUsed(strSchCategoryId));		
		}
*/
        // Prepare the loader for the accounts spinner.  Either re-connect with an existing one or start a new one.
		accountsLoaderCallback = new AccountsLoaderCallbacks();
        getLoaderManager().initLoader(SCHPAYMENTACCOUNTS_LOADER, null, accountsLoaderCallback);
        
        // Prepare the loader for the payees spinner. Either re-connect with an existing one or start a new one.
        payeesLoaderCallback = new PayeesLoaderCallbacks();
        getLoaderManager().initLoader(SCHPAYMENTPAEES_LOADER, null, payeesLoaderCallback);
        
        // Prepare the loader for the categories spinner. Either re-connect with an existing one or start a new one.
        categoriesLoaderCallback = new CategoriesLoaderCallbacks();
        getLoaderManager().initLoader(SCHPAYMENTCATEGORIES_LOADER, null, categoriesLoaderCallback);
	}
	
/*	@Override
	public void onBackPressed()
	{
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
				}
			});				
			alertDel.show();
		}
		else
		{
			finish();
		}
	}
*/
	// the callback received with the user "sets" the opening date in the dialog
/*	private DatePickerDialog.OnDateSetListener mDateSetListener = 
			new DatePickerDialog.OnDateSetListener() 
			{				
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) 
				{
					intYear = year;
					intMonth = monthOfYear;
					intDay = dayOfMonth;
					updateDisplay();
				}
			};
			
	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch(id)
		{
			case SET_DATE_ID:
				return new DatePickerDialog(this, mDateSetListener, intYear, intMonth, intDay);
		}
		return null;
	}
*/	
	public class AccountOnItemSelectedListener implements OnItemSelectedListener
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			if( iNumberofPasses > 7 )
			{
				switch( parent.getId())
				{
					case R.id.scheduleAccount:
						intSchAccountPos = parent.getSelectedItemPosition();
						Cursor c = (Cursor) parent.getAdapter().getItem(pos);
						strSchAccountId = c.getString(1).toString();
						((CreateModifyScheduleActivity) ParentActivity).setIsDirty(true);
						break;
					case R.id.scheduleFrequencyNumber:
						intSchFreq = parent.getSelectedItemPosition();	
						((CreateModifyScheduleActivity) ParentActivity).setIsDirty(true);
						break;
					case R.id.scheduleFrequencyDescription:
						strSchFreqDesc = parent.getAdapter().getItem(pos).toString();
						intSchFreqDesc = parent.getSelectedItemPosition();
						((CreateModifyScheduleActivity) ParentActivity).setIsDirty(true);
						break;
					case R.id.schedulePaymentMethod:
						intSchPaymentMethodPos = parent.getSelectedItemPosition();
						strSchPaymentMethod = parent.getAdapter().getItem(pos).toString();
						((CreateModifyScheduleActivity) ParentActivity).setIsDirty(true);
						break;						
					case R.id.scheduleType:
						intSchTypePos = parent.getSelectedItemPosition();
						String str = parent.getAdapter().getItem(pos).toString();
						if( str.matches("Deposit") )
						{
							intSchType = Schedule.TYPE_DEPOSIT;
							// Populate the categories spinner with the income statement accounts instead of balance sheet.
							//cursorCategories = KMMDapp.db.query("kmmAccounts", new String[] { "accountName", "id AS _id" },
							//		"(accountType=? OR accountType=?)", new String[] { String.valueOf(Account.ACCOUNT_EXPENSE),
							//			String.valueOf(Account.ACCOUNT_INCOME) }, null, null, "accountName ASC");
							//adapterCategories.changeCursor(cursorCategories);
							//adapterCategories.notifyDataSetChanged();
						}
						if( str.matches("Transfer") )
						{
							intSchType = Schedule.TYPE_TRANSFER;
							// Populate the categories spinner with the balance sheet accounts instead of income statement.
							//cursorCategories = KMMDapp.db.query("kmmAccounts", new String[] { "accountName", "id AS _id" },
							//		"(accountType=? OR accountType=?)", new String[] { String.valueOf(Account.ACCOUNT_ASSET),
							//			String.valueOf(Account.ACCOUNT_LIABILITY) }, null, null, "accountName ASC");
							//adapterCategories.changeCursor(cursorCategories);
							//adapterCategories.notifyDataSetChanged();
						}
						if( str.matches("Withdrawal") )
						{
							intSchType = Schedule.TYPE_BILL;
							// Populate the categories spinner with the income statement accounts instead of balance sheet.
							//cursorCategories = KMMDapp.db.query("kmmAccounts", new String[] { "accountName", "id AS _id" },
							//		"(accountType=? OR accountType=?)", new String[] { String.valueOf(Account.ACCOUNT_EXPENSE),
							//			String.valueOf(Account.ACCOUNT_INCOME) }, null, null, "accountName ASC");
							//adapterCategories.changeCursor(cursorCategories);
							//adapterCategories.notifyDataSetChanged();
						}
						((CreateModifyScheduleActivity) ParentActivity).setIsDirty(true);
						break;
					case R.id.payee:
						intSchPayeePos = parent.getSelectedItemPosition();
						c = (Cursor) parent.getAdapter().getItem(pos);
						strSchPayeeId = c.getString(1).toString();
						((CreateModifyScheduleActivity) ParentActivity).setIsDirty(true);
						break;
					case R.id.category:
						c = (Cursor) parent.getAdapter().getItem(pos);
						strCategoryName = c.getString(0).toString();
						//editCategory.setText(strCategoryName);
						catFrag.setCategoryName(strCategoryName);
						strSchCategoryId = c.getString(1).toString();
						((CreateModifyScheduleActivity) ParentActivity).setIsDirty(true);
						break;
					case R.id.status:
						intSchStatusPos = parent.getSelectedItemPosition();
						str = parent.getAdapter().getItem(pos).toString();
						if( str.matches( "Reconciled" ) )
							intSchStatus = 0;
						if( str.matches( "Cleared" ) )
							intSchStatus = 1;
						if( str.matches( "Not reconciled" ) )
							intSchStatus = 2;
						((CreateModifyScheduleActivity) ParentActivity).setIsDirty(true);
						break;
					default:
						break;
				}
			}
				if( iNumberofPasses < 8 )
					iNumberofPasses = iNumberofPasses + 1;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// do nothing.
		}		
	}
	
	public void onSplitsClicked(String categoryId) 
	{
		// TODO Auto-generated method stub
		
	}

	public void onSendWidgetId(int fromFrag) 
	{
		switch( fromFrag )
		{
		case AccountFragment.ACCOUNTS_LOADER:
			accountFrag.setWidgetId(this.fromWidgetId);
			break;
		case PayeeFragment.PAYEE_LOADER:
			payeeFrag.setWidgetId(this.fromWidgetId);
			break;
		case CategoryFragment.WITHDRAW_CATEGORY_LOADER:
			catFrag.setWidgetId(this.fromWidgetId);
			break;
		default:
			break;
		}		
	}
	// *******************************************************************************************************
	// *********************************** Public Event Interfaces *******************************************
	public interface OnSendPaymentInfoListener
	{
		public void onSendPaymentInfo();
	}
	// **************************************************************************************************
	// ************************************ Helper methods **********************************************
	public void sendPaymentInfo()
	{
		onSendPaymentInfo.onSendPaymentInfo();
	}
	
	public void setAction(int action)
	{
		this.Action = action;
	}
	
/*	public void editSchedule() 
	{
		// If we are coming back from splits entry screen follow this path.
		if( this.ReturningFromSplits )
		{
			//setupSplitInfo();
			editCategory.setText(R.string.splitTransaction);
			//btnCategory.setEnabled(false);
			//spinPayee.setSelection(setPayee(strSchPayeeId));
			iNumberofPasses = 0;
		}
		else
		{	
			// load the schedule details into the form.
			// set the spinner for the frequency and freq description of the schedule.
			spinFreqNum.setSelection(intSchFreq);
			spinFreqDesc.setSelection(intSchFreqDesc);
			
			// set the spinner for payment method
			spinPaymentMethod.setSelection(intSchPaymentMethodPos);
			
			// set the spinner for the transaction type
			spinSchType.setSelection(intSchTypePos);
			
			//intSchPayeePos = setPayee(strSchPayeeId);
			payeeFrag.setPayeeId(strSchPayeeId);
			//spinPayee.setSelection(intSchPayeePos);
		
			// See if we have only used one category or if we have multiple.
			if( Splits.size() == 2 )
			{
				Cursor c = KMMDapp.db.query("kmmAccounts", new String[] { "accountName" }, "id=?",
										new String[] { Splits.get(1).getAccountId() }, null, null, null);
				startManagingCursor(c);
				c.moveToFirst();
				editCategory.setText(c.getString(0));
				iNumberofPasses = 0;
				intSchStatus = Integer.valueOf(Splits.get(0).getReconcileFlag());

				c.close();
				numOfSplits = 2;
				anySplits = false;
				
				// Populate the category used for this split only.
				strSchCategoryId = Splits.get(1).getAccountId();
				//spinCategory.setSelection(setCategoryUsed(strSchCategoryId));
			}
			else
			{
				// need to put the splits into the KMMDapp.Splits object so user may edit the split details.
				KMMDapp.splitsInit();
				for(int i = 1; i < Splits.size(); i++)
					KMMDapp.Splits.add(Splits.get(i));
			
				for(int i = 0; i < KMMDapp.Splits.size(); i++)
					 KMMDapp.Splits.get(i).dump();
				iNumberofPasses = 0;
				editCategory.setText(R.string.splitTransaction);
				//btnCategory.setEnabled(false);
				numOfSplits = Splits.size();
				anySplits = true;
			}
		
			float amount = Float.valueOf(Splits.get(0).getValueFormatted());
			if( amount < 0 )
			{
				intSchType = Schedule.TYPE_BILL;
				amount = amount * -1;		//change the sign of the amount for the form only.
			}
			else
				intSchType = Schedule.TYPE_DEPOSIT;
			
			editAmount.setText(String.valueOf(amount));
			
			// Need to populate the Account used for this transaction.
			//intSchAccountPos = setAccountUsed(strSchAccountId);
			accountFrag.setAccountId(strSchAccountId);
			//spinSchAccount.setSelection(intSchAccountPos);
		
			// Make a copy of the original transactions split for later use if we modify anything.
			for(int i=0; i < Splits.size(); i++)
				OrigSplits.add(Splits.get(i));
			
			updateDisplay();
		}		
	}
*/	
	public String getScheduleName()
	{
		return this.editSchName.getText().toString();
	}
	
	public void setScheduleName(String name)
	{
		this.editSchName.setText(name);
	}
	
	public int getScheduleFrequency()
	{
		// Since we are storing the actual array index value we need to increase by one for database storage.
		return this.intSchFreq + 1;
	}
	
	public void setScheduleFrequency(int freq)
	{
		this.intSchFreq = freq - 1;
	}
	
	public String getScheduleFrequencyDescription()
	{
		return this.strSchFreqDesc;
	}
	
	public void setScheduleFrequencyDescription(int occurence)
	{
		switch(occurence)
		{
		case Schedule.OCCUR_ONCE:
			this.intSchFreqDesc = 0;
			this.strSchFreqDesc = "Once";
			break;
		case Schedule.OCCUR_DAILY:
			this.intSchFreqDesc = 1;
			this.strSchFreqDesc = "Day";
			break;
		case Schedule.OCCUR_WEEKLY:
			this.intSchFreqDesc = 2;
			this.strSchFreqDesc = "Week";
			break;
		case Schedule.OCCUR_EVERYHALFMONTH:
			this.intSchFreqDesc = 3;
			this.strSchFreqDesc = "Half-month";
			break;
		case Schedule.OCCUR_MONTHLY:
			this.intSchFreqDesc = 4;
			this.strSchFreqDesc = "Month";
			break;
		case Schedule.OCCUR_YEARLY:
			this.intSchFreqDesc = 5;
			this.strSchFreqDesc = "Year";
			break;
		default:
			this.intSchFreqDesc = 0;
			this.strSchFreqDesc = "Once";
			break;
		}
	}
	
	public int getSchedulePaymentMethod()
	{
		switch(this.intSchPaymentMethodPos)
		{
		case 0:
			return Schedule.PAYMENT_TYPE_OTHER;
		case 1:
			return Schedule.PAYMENT_TYPE_BANKTRANSFER;
		case 2:
			return Schedule.PAYMENT_TYPE_STANDINGORDER;
		case 3:
			return Schedule.PAYMENT_TYPE_MANUALDEPOSIT;
		case 4:
			return Schedule.PAYMENT_TYPE_DIRECTDEPOSIT;
		default:
			return Schedule.PAYMENT_TYPE_ANY;
		}
	}
	
	public void setSchedulePaymentMethod(int type)
	{
		switch(type)
		{
		case Schedule.PAYMENT_TYPE_OTHER:
			this.intSchPaymentMethodPos = 0;
			break;
		case Schedule.PAYMENT_TYPE_BANKTRANSFER:
			this.intSchPaymentMethodPos = 1;
			break;
		case Schedule.PAYMENT_TYPE_STANDINGORDER:
			this.intSchPaymentMethodPos = 2;
			break;
		case Schedule.PAYMENT_TYPE_MANUALDEPOSIT:
			this.intSchPaymentMethodPos = 3;
			break;
		case Schedule.PAYMENT_TYPE_DIRECTDEPOSIT:
			this.intSchPaymentMethodPos = 4;
			break;
		case Schedule.PAYMENT_TYPE_ANY:
			this.intSchPaymentMethodPos = 0;
			break;
		}
	}
	
	public int getScheduleType()
	{
		return this.intSchType;
	}

	public String getScheduleTypeString()
	{
		return this.spinSchType.getItemAtPosition(this.intSchType).toString();
	}
	
	public void setScheduleType(int type)
	{
		switch(type)
		{
		case Schedule.TYPE_ANY:
			this.intSchTypePos = 2;
			break;
		case Schedule.TYPE_BILL:
			this.intSchTypePos = 2;
			break;
		case Schedule.TYPE_DEPOSIT:
			this.intSchTypePos = 0;
			break;
		case Schedule.TYPE_LOANPAYMENT:
			this.intSchTypePos = 2;
			break;
		case Schedule.TYPE_TRANSFER:
			this.intSchTypePos = 1;
			break;
		default:
			this.intSchTypePos = 2;
			break;
		}
		
		this.intSchType = type;
	}
	
	public String getAccountTypeId()
	{
		return this.strSchAccountId;
	}
	
	public void setAccountTypeId(String acctTypeId)
	{
		this.strSchAccountId = acctTypeId;
	}
	
	public String getPayeeId()
	{
		return this.strSchPayeeId;
	}
	
	public void setPayeeId(String payeeId)
	{
		this.strSchPayeeId = payeeId;
	}
	
	public String getCategoryId()
	{
		return this.strSchCategoryId;
	}
	
	public void setCategoryId(String categoryId)
	{
		this.strSchCategoryId = categoryId;
	}
	
	public String getCheckNumber()
	{
		return this.editCheckNum.getText().toString();
	}
	
	public void setCheckNumber(String checkNumber)
	{
		this.editCheckNum.setText(checkNumber);
	}
	
	public String getStartDate()
	{
		// Need to re-format the date to YYY-MM-DD
		String str[] = this.editDate.getText().toString().split("-");
		return new StringBuilder()
		// Month is 0 based so add 1
		.append(str[2]).append("-")
		.append(str[0]).append("-")
		.append(str[1]).toString();
	}
	
	public void setStartDate(String startDate)
	{
		// Month is 0 based so we need to subtract 1
		String date[] = startDate.split("-");
		intYear = Integer.valueOf(date[0]);
		intMonth = Integer.valueOf(date[1]) - 1;
		intDay = Integer.valueOf(date[2]);
	}
	
	public String getScheduleAmount()
	{
		DecimalFormat decimal = new DecimalFormat();
		char decChar = decimal.getDecimalFormatSymbols().getDecimalSeparator();
		return Transaction.convertToDollars(Transaction.convertToPennies(this.editAmount.getText().toString().replace('.', decChar)), false, false);
	}
	
	public void setScheduleAmount(String amount)
	{
		this.editAmount.setText(amount);
	}
	
	public int getScheduleStatus()
	{
		return this.intSchStatus;
	}
	
	public void setScheduleStatus(int status)
	{
		this.intSchStatus = status;
	}
	
	public String getScheduleMemo()
	{
		return this.editMemo.getText().toString();
	}
	
	public void setScheduleMemo(String memo)
	{
		this.editMemo.setText(memo);
	}
	
	public void setSplits(	ArrayList<Split> Splits)
	{
		this.Splits = Splits;
	}
	
	private void updateDisplay()
	{
		String strDay = null;
		String strMonth = null;
		switch(intDay)
		{
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
				strDay = "0" + String.valueOf(intDay);
				break;
			default:
				strDay = String.valueOf(intDay);
			break;
		}
		
		switch(intMonth)
		{
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			//case 9:
				strMonth = "0" + String.valueOf(intMonth + 1);
				break;
			default:
				strMonth = String.valueOf(intMonth + 1);
				break;
		}
		
		editDate.setText(
				new StringBuilder()
					// Month is 0 based so add 1
					.append(strMonth).append("-")
					.append(strDay).append("-")
					.append(intYear));
	}
	
/*	private void setupSplitInfo()
	{
		// need to take into account the actual accounts entry as well as the splits.
		numOfSplits = KMMDapp.Splits.size() + 1;
		anySplits = true;
		if( KMMDapp.flSplitsTotal != 0  )
		{
			long tmp = 0;
			if( KMMDapp.flSplitsTotal < 0 )
				tmp = KMMDapp.flSplitsTotal * -1;
			else
				tmp = KMMDapp.flSplitsTotal;
			editAmount.setText(Transaction.convertToDollars((tmp), true));
		}
		
		// Clear the Splits ArrayList out.
		Splits.clear();
	}
*/	
	// Take the occurence value and return the index to our internal array for the description.
	/*private int getOccurenceDescFromOccurence(int occ)
	{
		switch(occ)
		{
		case Schedule.OCCUR_ANY:
			return 0;
		case Schedule.OCCUR_ONCE:
			return 0;
		case Schedule.OCCUR_DAILY:
			return 1;
		case Schedule.OCCUR_WEEKLY:
		case Schedule.OCCUR_FORTNIGHTLY:
		case Schedule.OCCUR_EVERYOTHERWEEK:
		case Schedule.OCCUR_EVERYTHREEWEEKS:
		case Schedule.OCCUR_EVERYTHIRTYDAYS:
			return 2;
		case Schedule.OCCUR_MONTHLY:
		case Schedule.OCCUR_EVERYFOURWEEKS:
		case Schedule.OCCUR_EVERYEIGHTWEEKS:
		case Schedule.OCCUR_EVERYOTHERMONTH:
		case Schedule.OCCUR_EVERYTHREEMONTHS:
		case Schedule.OCCUR_TWICEYEARLY:
		case Schedule.OCCUR_QUARTERLY:
		case Schedule.OCCUR_EVERYFOURMONTHS:
			return 4;
		case Schedule.OCCUR_EVERYOTHERYEAR:
		case Schedule.OCCUR_YEARLY:
			return 5;
		default:
			return 0;
		}
	}*/
	
/*	private int setPayee(String payeeId)
	{
		int i = 0;
		cursorPayees.moveToFirst();

		if( payeeId != null )
		{
			while(!payeeId.equals(cursorPayees.getString(1)))
			{
				cursorPayees.moveToNext();
			
				//check to see if we have moved past the last item in the cursor, if so return current i.
				if(cursorPayees.isAfterLast())
					return i;
			
				i++;
			}
		}
		return i;
	}
	
	private int setAccountUsed(String accountId)
	{
		int i = 0;
		cursorAccounts.moveToFirst();

		if( accountId != null )
		{
			while(!accountId.equals(cursorAccounts.getString(1)))
			{
				cursorAccounts.moveToNext();
				
				//check to see if we have moved past the last item in the cursor, if so return current i.
				if(cursorAccounts.isAfterLast())
					return i;
				
				i++;
			}
		}
		return i;
	}
	
	private int setCategoryUsed(String categoryId)
	{
		int i = 0;
		cursorCategories.moveToFirst();

		if( categoryId != null )
		{
			while(!categoryId.equals(cursorCategories.getString(1)))
			{
				cursorCategories.moveToNext();
				
				//check to see if we have moved past the last item in the cursor, if so return current i.
				if(cursorCategories.isAfterLast())
				{
					return i;
				}
				i++;
			}
		}
		return i;		
	}
	*/
	
	// Private implementation of the Loader Callbacks required for this activity.
	// AccountsLoaderCallbacks - handles the loading of the accounts into the Spinner
	// PayeesLoaderCallbacks - handles the loading of the payees into the Spinner
	// CategoriesLoaderCallbacks - handles the loading of the categories into the Spinner
	
    private class AccountsLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor>
    {  

		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) 
		{
			Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI,"#" + "9999");
			u = Uri.parse(u.toString());
			String[] dbColumns = new String[] { "accountName", "id AS _id" };
			String dbSelection = "(accountType=? OR accountType=? OR accountType=? OR accountType=?) AND (balance != '0/1')";
			String[] dbSelectionArgs = { String.valueOf(Account.ACCOUNT_CHECKING), String.valueOf(Account.ACCOUNT_SAVINGS), 
					String.valueOf(Account.ACCOUNT_LIABILITY), String.valueOf(Account.ACCOUNT_CREDITCARD) };

			return new CursorLoader(getActivity().getBaseContext(), u, dbColumns, dbSelection, dbSelectionArgs, "acountName ASC");
		} 
		
        public void onLoadFinished(Loader<Cursor> loader, Cursor accounts) 
        {
            // Set the new data in the adapter.
        	adapterSchAccounts.swapCursor(accounts);
        	//setProgressBarIndeterminateVisibility(false);
        }
        
        public void onLoaderReset(Loader<Cursor> loader) 
        {
            // clear the data in the adapter.
        	adapterSchAccounts.swapCursor(null);
        }   	
    }
    
    private class PayeesLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor>
    {

		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) 
		{
			Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_PAYEE_URI,"#" + "999");
			u = Uri.parse(u.toString());
			String[] dbColumns = new String[] { "name", "id AS _id" };

			return new CursorLoader(getActivity().getBaseContext(), u, dbColumns, null, null, "acountName ASC");
		}

		public void onLoadFinished(Loader<Cursor> loader, Cursor payees) 
		{
            // Set the new data in the adapter.
			adapterPayees.swapCursor(payees);
		}

		public void onLoaderReset(Loader<Cursor> loader) 
		{
            // clear the data in the adapter.
			adapterPayees.swapCursor(null);
		}    	
    }

    private class CategoriesLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor>
    {

		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) 
		{
			Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI,"#" + "9999");
			u = Uri.parse(u.toString());
			String[] dbColumns = new String[] { "accountName", "id AS _id" };
			String dbSelection = "(accountType=? OR accountType=?)";
			String[] dbSelectionArgs = new String[] { String.valueOf(Account.ACCOUNT_EXPENSE), String.valueOf(Account.ACCOUNT_INCOME) };
			
			return new CursorLoader(getActivity().getBaseContext(), u, dbColumns, dbSelection, dbSelectionArgs,	"accountName ASC");

		}

		public void onLoadFinished(Loader<Cursor> loader, Cursor categories) 
		{
            // Set the new data in the adapter.
			adapterCategories.swapCursor(categories);
		}

		public void onLoaderReset(Loader<Cursor> loader) 
		{
            // clear the data in the adapter.
			adapterCategories.swapCursor(null);
		}    	
    }
}
