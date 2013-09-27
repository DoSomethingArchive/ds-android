package org.dosomething.android.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.dosomething.android.DSConstants;
import org.dosomething.android.FadeInResizeBitmapDisplayer;
import org.dosomething.android.R;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.HowTo;
import org.dosomething.android.widget.CustomActionBar;

import java.util.List;

import roboguice.inject.InjectView;

public class CampaignHowTo extends AbstractActivity {
	
	private static final String CAMPAIGN = "campaign";
	
	@Inject private LayoutInflater inflater;
	@Inject private ImageLoader imageLoader;
	@Inject @Named("DINComp-CondBold")Typeface headerTypeface;
	
	@InjectView(R.id.actionbar) private CustomActionBar actionBar;
	@InjectView(R.id.list) private ListView list;
	
	@Override
	protected String getPageName() {
		return "campaign-how-to";
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaign_how_to);
        
        actionBar.addAction(Campaigns.getHomeAction(this));
        
        Campaign campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);

        list.setAdapter(new MyAdapter(getApplicationContext(), campaign.getHowTos()));
    }
	
	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign){
		Intent answer = new Intent(context, CampaignHowTo.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}
	
	
	private class MyAdapter extends ArrayAdapter<HowTo> {

		public MyAdapter(Context context, List<HowTo> objects){
			super(context, android.R.layout.simple_list_item_1, objects);
		}
		
		@Override
		public boolean isEnabled(int position) {
			return false;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = inflater.inflate(R.layout.hib_row, null);
			}
			
			HowTo howTo = getItem(position);
			
			TextView header = (TextView)v.findViewById(R.id.header);
			header.setTypeface(headerTypeface, Typeface.BOLD);
			header.setText(howTo.getHeader());
			
			ImageView image = (ImageView)v.findViewById(R.id.image);
			DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
				.displayer(new FadeInResizeBitmapDisplayer(DSConstants.IMAGE_LOADER_FADE_IN_TIME))
				.build();
			imageLoader.displayImage(howTo.getImageUrl(), image, imageOptions);
			
			TextView body = (TextView)v.findViewById(R.id.body);
			body.setText(howTo.getBody());

			return v;
		}
		
	}
}
