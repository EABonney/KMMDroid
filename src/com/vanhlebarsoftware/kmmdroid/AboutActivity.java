package com.vanhlebarsoftware.kmmdroid;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.View;

public class AboutActivity extends FragmentActivity
{
	AboutFragment aboutFrag;
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle arg0) 
	{
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.about);
		
		// Find our fragment
		aboutFrag = (AboutFragment) getFragmentManager().findFragmentById(R.id.aboutFragment);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() 
	{
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() 
	{
		// TODO Auto-generated method stub
		super.onResume();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateView(android.view.View, java.lang.String, android.content.Context, android.util.AttributeSet)
	 */
	@Override
	public View onCreateView(View parent, String name, Context context, AttributeSet attrs) 
	{
		// TODO Auto-generated method stub
		return super.onCreateView(parent, name, context, attrs);
	}

}
