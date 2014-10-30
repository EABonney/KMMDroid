package com.vanhlebarsoftware.kmmdroid;

import com.vanhlebarsoftware.kmmdroid.KMMDCustomFastScrollView.SectionIndexer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TransactionAdapter extends ArrayAdapter<Transaction> implements SectionIndexer
{
	private final static String TAG = TransactionAdapter.class.getSimpleName();
	private Context context;
	boolean bChangeBackground = false;
	private int textViewResourceId;
	private List<Transaction> objects;

	private SectionIndexer sectionIndexer;
	
	public TransactionAdapter(Context context, int textViewResourceId,
			List<Transaction> objects) 
	{
		super(context, textViewResourceId, objects);
		this.context = context;
		this.textViewResourceId = textViewResourceId;
		this.objects = objects;
	}
	
	public void setData(List<Transaction> transactions)
	{
        clear();
        if (transactions != null) 
        {
        	for(Transaction transaction : transactions)
        	{
        		add(transaction);
        	}
        }		
	}
	
	@Override
	public View getView(int position, View view, ViewGroup parent) 
	{
		if (view == null) 
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(textViewResourceId, parent, false);
		}

		Transaction item = objects.get(position);
		// Load the items into the view now for this schedule.
		if(item != null)
		{
			TextView DatePaid = (TextView) view.findViewById(R.id.lrDate);
			TextView Payee = (TextView) view.findViewById(R.id.lrDetails);
			TextView Amount = (TextView) view.findViewById(R.id.lrAmount);
			TextView Balance = (TextView) view.findViewById(R.id.lrBalance);
			LinearLayout row = (LinearLayout) view.findViewById(R.id.row);

			if(bChangeBackground)
			{
				row.setBackgroundColor(Color.rgb(0x62, 0xB1, 0xF6));
				bChangeBackground = false;
			}
			else
			{
				row.setBackgroundColor(Color.rgb(0x62, 0xa1, 0xc6));
				bChangeBackground = true;
			}
			
			// See if this is a future transaction, if so change to italics.
			if(item.getTransId().equals("999999"))
			{
				DatePaid.setTypeface(Typeface.DEFAULT);
				DatePaid.setTextColor(Color.BLACK);
				Payee.setTypeface(Typeface.DEFAULT);
				Payee.setTextColor(Color.BLACK);
				Amount.setTypeface(Typeface.DEFAULT);
				Amount.setTextColor(Color.BLACK);
				Balance.setTypeface(Typeface.DEFAULT);
				Balance.setTextColor(Color.BLACK);	
				DatePaid.setText("");
				Payee.setText(getContext().getString(R.string.loadMoreRow));
				Amount.setText("");
				Balance.setText("");
			}
			else
			{
				if(item.isFuture())
				{
					DatePaid.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC), Typeface.ITALIC);
					DatePaid.setTextColor(Color.LTGRAY);
					Payee.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC), Typeface.ITALIC);
					Payee.setTextColor(Color.LTGRAY);
					Amount.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC), Typeface.ITALIC);
					Amount.setTextColor(Color.LTGRAY);
					Balance.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC), Typeface.ITALIC);
					Balance.setTextColor(Color.LTGRAY);
				}
				else
				{
					DatePaid.setTypeface(Typeface.DEFAULT);
					DatePaid.setTextColor(Color.BLACK);
					Payee.setTypeface(Typeface.DEFAULT);
					Payee.setTextColor(Color.BLACK);
					Amount.setTypeface(Typeface.DEFAULT);
					Amount.setTextColor(Color.BLACK);
					Balance.setTypeface(Typeface.DEFAULT);
					Balance.setTextColor(Color.BLACK);
				}
			
				DatePaid.setText(item.formatDateString());
				Payee.setText(item.splits.get(0).getPayeeName());
				Amount.setText(Transaction.convertToDollars(item.getAmount(), true, false));
				Balance.setText(Transaction.convertToDollars(item.getBalance(), true, false));
			}
		}
		else
			Log.d(TAG, "Never got a Schedule!");
		
		return view;
	}


	public int getPositionForSection(int section) 
	{
		return getSectionIndexer().getPositionForSection(section);
	}

	public int getSectionForPosition(int position)
	{
		return getSectionIndexer().getSectionForPosition(position);
	}

	public Object[] getSections()
	{
		return getSectionIndexer().getSections();
	}
	
	private SectionIndexer getSectionIndexer()
	{
		if (sectionIndexer == null)
		{
			sectionIndexer = createSectionIndexer(objects);
		}
		return sectionIndexer;
	}
	
	private SectionIndexer createSectionIndexer(List<Transaction> transactions)
	{
		return createSectionIndexer(transactions, new Function<Transaction,String>()
			   {
					public String apply(Transaction input) 
					{
			            String shortDate = null;
			            
			            if( input == null)
			            {
			            	Log.d(TAG, "input was null!");
			            }
			            else if( !input.getTransId().equals("999999") )
		            	{
		            		Calendar date = input.getDate();
		            	    String month = context.getResources().getStringArray(R.array.ShortMonthNames)[date.get(Calendar.MONTH)];
		            		String year = String.valueOf(date.get(Calendar.YEAR));
		            		shortDate = month + " " + year;
		            	}

						return shortDate;
					}
				});
	}


	/**
	 * Create a SectionIndexer given an arbitrary function mapping countries to their section name.
	 * @param countries
	 * @param sectionFunction
	 * @return
	 */
	private SectionIndexer createSectionIndexer(List<Transaction> transactions, Function<Transaction, String> sectionFunction)
	{
	
		List<String> sections = new ArrayList<String>();
		final List<Integer> sectionsToPositions = new ArrayList<Integer>();
		final List<Integer> positionsToSections = new ArrayList<Integer>();
		
		
		// assume the transactions are properly sorted
		for (int i = 1; i < transactions.size(); i++)
		{
			Transaction transaction = transactions.get(i);
			String section = sectionFunction.apply(transaction);
			if (sections.isEmpty() || !sections.get(sections.size() - 1).equals(section))
			{
				// add a new section
				sections.add(section);
				// map section to position
				sectionsToPositions.add(i);
			}
			
			// map position to section
			positionsToSections.add(sections.size() - 1);
		}
		
		final String[] sectionsArray = sections.toArray(new String[sections.size()]);
		
		return new SectionIndexer()
		{
			public Object[] getSections()
			{
				return sectionsArray;
			}
			
			public int getSectionForPosition(int position)
			{
				return positionsToSections.get(position);
			}
			
			public int getPositionForSection(int section)
			{
				return sectionsToPositions.get(section);
			}
		};
	}

	public void refreshSections()
	{
		sectionIndexer = null;
		getSectionIndexer();
	}
}
