package com.vanhlebarsoftware.kmmdroid;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import android.content.Context;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Log;
import android.util.Xml;

public class KMMDSplitParser extends BaseSplitParser
{
	private final static String TAG = KMMDSplitParser.class.getSimpleName();
	
	public KMMDSplitParser(String xmlFile, Context c)
	{
		super(xmlFile, c);
	}
	
	public Transaction parse()
	{
		final Transaction currentTransaction = new Transaction(getContext(), null, null);
		final Split currentSplit = new Split(getContext());
		RootElement root = new RootElement(KMMDCACHE);
		Element item = root.getChild(TRANS);
		
		item.setStartElementListener(new StartElementListener()
		{
			public void start(Attributes attributes)
			{
	            currentTransaction.setTransId(attributes.getValue("id"));
	            currentTransaction.setTxType(attributes.getValue("txType"));
	            currentTransaction.setMemo(attributes.getValue("memo"));
	            currentTransaction.setCurrencyId(attributes.getValue("currencyId"));
	            currentTransaction.setBankId(attributes.getValue("bankId"));
	            currentTransaction.setDate(attributes.getValue("postDate"));
	            currentTransaction.setEntryDate(attributes.getValue("entryDate"));
	            currentTransaction.setWidgetId(attributes.getValue("widgetId"));
			}
		});
		
		item.setEndElementListener(new EndElementListener()
		{
			public void end()
			{
				Log.d(TAG, "Reached the end of a transaction in XML file");
			}
		});
		
		item.getChild(SPLIT).setStartElementListener(new StartElementListener()
		{

			public void start(Attributes attributes) 
			{
				currentSplit.setTransactionId(attributes.getValue("transactionid"));
				currentSplit.setTxType(attributes.getValue("txType"));
				currentSplit.setSplitId(attributes.getValue("splitId"));
				currentSplit.setPayeeId(attributes.getValue("payeeId"));
				currentSplit.setReconcileDate(attributes.getValue("reconcileDate"));
				currentSplit.setAction(attributes.getValue("action"));
				currentSplit.setReconcileFlag(attributes.getValue("reconcileFlag"));
				currentSplit.setValue(attributes.getValue("value"));
				currentSplit.setValueFormatted(attributes.getValue("valueFormatted"));
				currentSplit.setShares(attributes.getValue("shares"));
				currentSplit.setSharesFormatted(attributes.getValue("sharesFormatted"));
				currentSplit.setPrice(attributes.getValue("price"));
				currentSplit.setPriceFormatted(attributes.getValue("priceFormatted"));
				currentSplit.setMemo(attributes.getValue("memo"));
				currentSplit.setAccountId(attributes.getValue("accountId"));
				currentSplit.setCheckNumber(attributes.getValue("checkNumber"));
				currentSplit.setPostDate(attributes.getValue("postDate"));
				currentSplit.setBankId(attributes.getValue("bankId"));
			}
			
		});
		
		item.getChild(SPLIT).setEndElementListener(new EndElementListener()
		{
			public void end()
			{
				currentTransaction.splits.add(currentSplit.copy());
			}
		});
		
		try
		{
			Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
		}
		catch (Exception e)
		{
			Log.d(TAG, "Error occurred: " + e.getMessage());
		}
		
		return currentTransaction;
	}
}

abstract class BaseSplitParser implements KMMDSplitStateParser
{
	// names of our XML tags
	static final String KMMDCACHE = "KMMDCache";
	static final String TRANS = "Transaction";
	static final String SPLIT = "Split";
	final String xmlTransactionSplits;
	private Context context;
	
	protected BaseSplitParser(String kmmdCacheFile, Context context)
	{
		this.xmlTransactionSplits = kmmdCacheFile;
		this.context = context;
	}
	
	protected FileInputStream getInputStream()
	{
		try
		{
			return new FileInputStream(this.xmlTransactionSplits);	
		}
        catch (FileNotFoundException e) 
        {
        	return null;
		} 
	}
	
	public Context getContext()
	{
		return this.context;
	}
}
