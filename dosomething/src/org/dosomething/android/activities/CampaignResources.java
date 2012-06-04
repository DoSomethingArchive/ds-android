package org.dosomething.android.activities;

import java.util.List;

import org.dosomething.android.R;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.Resource;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.markupartist.android.widget.ActionBar;

public class CampaignResources extends AbstractActivity {
	
	private static final String CAMPAIGN = "campaign";
	
	@Inject private LayoutInflater inflater;
	
	@InjectView(R.id.actionbar) private CustomActionBar actionBar;
	@InjectView(R.id.list) private ListView list;
	
	@Override
	protected String getPageName() {
		return "campaign-resources";
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaign_resources);
        
        actionBar.addAction(Campaigns.getHomeAction(this));
        
        Campaign campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);
        
        list.setAdapter(new MyAdapter(getApplicationContext(), campaign.getResources()));
        list.setOnItemClickListener(itemClickListener);
	}
	
	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign){
		Intent answer = new Intent(context, CampaignResources.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}
	
	private final OnItemClickListener itemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> av, View v, int position,
				long id) {
			Resource resource = (Resource) list.getAdapter().getItem(position);
			
			if(resource.getLinkUrl() != null && resource.getLinkUrl().length() > 0){
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(resource.getLinkUrl()));
				startActivity(i);
			}
		}
	};
	
	private class MyAdapter extends ArrayAdapter<Resource> {

		public MyAdapter(Context context, List<Resource> objects){
			super(context, android.R.layout.simple_list_item_1, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = inflater.inflate(R.layout.resource_row, null);
			}
			
			final Resource resource = getItem(position);
			
			TextView body = (TextView)v.findViewById(R.id.body);
			body.setText(resource.getBody());

			return v;
		}
		
	}
	
}
