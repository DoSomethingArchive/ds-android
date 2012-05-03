package org.dosomething.android.activities;

import java.util.List;

import org.dosomething.android.R;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.Faq;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.markupartist.android.widget.ActionBar;

public class CampaignFAQ extends RoboActivity {
	
	private static final String CAMPAIGN = "campaign";
	
	@Inject private LayoutInflater inflater;
	
	@InjectView(R.id.actionbar) private ActionBar actionBar;
	@InjectView(R.id.list) private ExpandableListView list;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.campaign_faq);
	    
	    actionBar.setHomeAction(Campaigns.getHomeAction(this));
	    
	    Campaign campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);
	    
	    list.setAdapter(new MyAdapter(campaign.getFaqs()));
	}
	
	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign){
		Intent answer = new Intent(context, CampaignFAQ.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}
	
	
	private class MyAdapter extends BaseExpandableListAdapter {
		
		private List<Faq> faqs;
		
		public MyAdapter(List<Faq> faqs) {
			this.faqs = faqs;
		}
		
		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return faqs.get(groupPosition);
		}
	
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}
	
		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = inflater.inflate(R.layout.faq_row_child, null);
			}
			
			Faq faq = (Faq) getGroup(groupPosition);
			
			TextView body = (TextView)v.findViewById(R.id.body);
			body.setText(faq.getBody());

			return v;
		}
	
		@Override
		public int getChildrenCount(int groupPosition) {
			return 1;
		}
	
		@Override
		public Object getGroup(int groupPosition) {
			return faqs.get(groupPosition);
		}
	
		@Override
		public int getGroupCount() {
			return faqs.size();
		}
	
		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}
	
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = inflater.inflate(R.layout.faq_row, null);
			}
			
			Faq faq = (Faq) getGroup(groupPosition);
			
			TextView header = (TextView)v.findViewById(R.id.header);
			header.setText(faq.getHeader());

			return v;
		}
	
		@Override
		public boolean hasStableIds() {
			return true;
		}
	
		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}
		
	}
	
}
