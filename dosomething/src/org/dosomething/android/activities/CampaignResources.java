package org.dosomething.android.activities;

import java.util.ArrayList;
import java.util.List;

import org.dosomething.android.R;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.MoreInfo;
import org.dosomething.android.transfer.MoreInfoItem;
import org.dosomething.android.transfer.Resource;
import org.dosomething.android.widget.CustomActionBar;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CampaignResources extends AbstractActivity {
	
	private static final String CAMPAIGN = "campaign";
	
	private enum IRIType {
		MOREINFO,
		RESOURCE
	};
	
	@Inject private LayoutInflater inflater;
	@Inject private ImageLoader imageLoader;
	@Inject @Named("DINComp-CondBold")Typeface headerTypeface;
	
	@InjectView(R.id.actionbar) private CustomActionBar actionBar;
	@InjectView(R.id.intro) private TextView introText;
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
        
        // Array holding the items to be displayed in the list
        List<InfoResourceItem> listItems = new ArrayList<InfoResourceItem>();
        
        // First listed are the More Info items, if any
        MoreInfo mi = campaign.getMoreInfo();
        if (mi != null && mi.getIntro() != null && mi.getIntro().length() > 0) {
        	introText.setVisibility(View.VISIBLE);
        	introText.setText(mi.getIntro());
        	
        	List<MoreInfoItem> infoItems = mi.getItems();
            for (int i = 0; i < infoItems.size(); i++) {
            	MoreInfoItem miItem = infoItems.get(i);
            	InfoResourceItem irItem = new InfoResourceItem(miItem);
            	listItems.add(irItem);
            }
        }
        
        // Also appended to the list are Resource items, if any
        List<Resource> resources = campaign.getResources();
        if (resources != null) {
	        for (int i = 0; i < resources.size(); i++) {
	        	Resource res = resources.get(i);
	        	InfoResourceItem irItem = new InfoResourceItem(res);
	        	listItems.add(irItem);
	        }
        }
        
        list.setAdapter(new ResourceListAdapter(getApplicationContext(), listItems));
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
			InfoResourceItem irItem = (InfoResourceItem) list.getAdapter().getItem(position);
			
			if (irItem.getType() == IRIType.RESOURCE) {
				Resource resource = irItem.getResource();
			
				if (resource.getLinkUrl() != null && resource.getLinkUrl().length() > 0) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(resource.getLinkUrl()));
					startActivity(i);
				}
			}
		}
	};
	
	private class InfoResourceItem {
		
		private IRIType type;
		private MoreInfoItem moreInfo;
		private Resource resource;
		
		public InfoResourceItem(MoreInfoItem _moreInfo) {
			type = IRIType.MOREINFO;
			moreInfo = _moreInfo;
		}
		
		public InfoResourceItem(Resource _resource) {
			type = IRIType.RESOURCE;
			resource = _resource;
		}
		
		public IRIType getType() {
			return type;
		}
		
		public MoreInfoItem getMoreInfo() {
			return moreInfo;
		}
		
		public Resource getResource() {
			return resource;
		}
	}
	
	private class ResourceListAdapter extends ArrayAdapter<InfoResourceItem> {

		public ResourceListAdapter(Context context, List<InfoResourceItem> objects){
			super(context, android.R.layout.simple_list_item_1, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			InfoResourceItem irItem = getItem(position);
			
			View v = convertView;
			if (irItem.getType() == IRIType.MOREINFO) {
				v = inflater.inflate(R.layout.more_info_row, null);
				
				MoreInfoItem mi = irItem.getMoreInfo();
				
				TextView header = (TextView)v.findViewById(R.id.header);
				header.setTypeface(headerTypeface, Typeface.BOLD);
				header.setText(mi.getHeader());
				
				ImageView image = (ImageView)v.findViewById(R.id.image);
				imageLoader.displayImage(mi.getImageUrl(), image);
				
				TextView body = (TextView)v.findViewById(R.id.body);
				body.setText(mi.getBody());
			}
			else if (irItem.getType() == IRIType.RESOURCE) {
				v = inflater.inflate(R.layout.resource_row, null);
				
				Resource resource = irItem.getResource();
				
				TextView body = (TextView)v.findViewById(R.id.body);
				body.setText(resource.getBody());
			}

			return v;
		}
		
	}
	
}
