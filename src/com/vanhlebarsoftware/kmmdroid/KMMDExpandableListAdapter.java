package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.graphics.*;

public class KMMDExpandableListAdapter extends BaseExpandableListAdapter 
{
    private Context context;
    private ArrayList<String> groups;
    private ArrayList<ArrayList<Account>> children;

    @Override
    public boolean areAllItemsEnabled()
    {
        return true;
    }

    public KMMDExpandableListAdapter(Context context, ArrayList<String> groups, ArrayList<ArrayList<Account>> children) 
    {
        this.context = context;
        this.groups = groups;
        this.children = children;
    }
    
	public void setData(List<Account> data) 
    {
        clear();
        if (data != null) 
        {
        	for(Account account : data)
        	{
        		addItem(account.getAccountTypeString(), account);
        	}
        }
    }
	
	private void clear()
	{
		groups.clear();
		children.clear();
	}

    /**
     * A general add method, that allows you to add an Account to this list
     * 
     * Depending on if the parentId of the account is present or not,
     * the corresponding item will either be added to an existing group if it 
     * exists, else the group will be created and then the item will be added
     * 
     */
    public void addItem(String strParentId, Account account) 
    {
        if ( !groups.contains(strParentId) ) 
        {
            groups.add(strParentId);
        }
        int index = groups.indexOf(strParentId);
        if (children.size() < index + 1) 
        {
            children.add(new ArrayList<Account>());
        }
        children.get(index).add(account);
    }

    public Object getChild(int groupPosition, int childPosition) 
    {
        return children.get(groupPosition).get(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) 
    {
        return childPosition;
    }
    
    // Return a child view. You can load your custom layout here.
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) 
    {   	
        if (convertView == null) 
        {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.categories_row, null);
        }    

		LinearLayout row = (LinearLayout) convertView.findViewById(R.id.crRow);
		if( childPosition % 2 == 0)
			row.setBackgroundColor(Color.rgb(0x62, 0xB1, 0xF6));
		else
			row.setBackgroundColor(Color.rgb(0x62, 0xa1, 0xc6));
		
    	Account account = (Account) getChild(groupPosition, childPosition); 
    	ImageView imgView = (ImageView) convertView.findViewById(R.id.expandIcon);
    	String accountName = null;
    	if( account.getIsParent() )
        {
        	imgView.setVisibility(View.VISIBLE);
        	accountName = account.getName() + " (" + account.getNumberSubAccounts(context) + ")";
        }
        else
        {
        	imgView.setVisibility(View.GONE);
        	accountName = account.getName();
        }
    	TextView tvName = (TextView) convertView.findViewById(R.id.crAccountName);
    	TextView tvBalance = (TextView) convertView.findViewById(R.id.crAccountBalance);
        tvName.setText(accountName);
        tvBalance.setText(account.getBalance());
        return convertView;
    }

    public int getChildrenCount(int groupPosition) 
    {
        return children.get(groupPosition).size();
    }

    public Object getGroup(int groupPosition) 
    {
        return groups.get(groupPosition);
    }

    public int getGroupCount() 
    {
        return groups.size();
    }

    public long getGroupId(int groupPosition) 
    {
        return groupPosition;
    }

    // Return a group view. You can load your custom layout here.
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) 
    {
        String group = (String) getGroup(groupPosition);
        if (convertView == null) 
        {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.group_layout, null);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.tvGroup);
        tv.setText(group + " (" + getChildrenCount(groupPosition) + ")");
        return convertView;
    }

    public boolean hasStableIds() 
    {
        return true;
    }

    public boolean isChildSelectable(int arg0, int arg1) 
    {
        return true;
    }

}
