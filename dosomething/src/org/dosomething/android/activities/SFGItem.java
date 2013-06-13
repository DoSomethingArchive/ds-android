package org.dosomething.android.activities;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.SFGGalleryItem;
import org.dosomething.android.widget.CustomActionBar;
import org.dosomething.android.widget.ProgressBarImageLoadingListener;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SFGItem extends AbstractActivity {
	
	@Inject private ImageLoader imageLoader;
	@Inject @Named("DINComp-CondBold")Typeface dinTypeface;

	@InjectView(R.id.actionbar) private CustomActionBar actionBar;
	@InjectView(R.id.image) private ImageView imageView;
	@InjectView(R.id.share_count) private TextView shareCountView;
	@InjectView(R.id.shelter) private TextView shelterView;
	@InjectView(R.id.state) private TextView stateView;
	@InjectView(R.id.story) private TextView storyView; 
	@InjectView(R.id.progressBar) private ProgressBar progressBar;
	
	private Campaign campaign;
	private SFGGalleryItem sfgItem;
	
	@Override
	protected String getPageName() {
		return "SFGItem";
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sfgitem);
		
		sfgItem = (SFGGalleryItem)getIntent().getExtras().get(DSConstants.EXTRAS_KEY.SFGITEM.getValue());
		if (sfgItem != null) {
			actionBar.setTitle(sfgItem.getName());
		}
		
		campaign = (Campaign)getIntent().getExtras().get(DSConstants.EXTRAS_KEY.CAMPAIGN.getValue());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (sfgItem != null) {
			if (campaign != null && imageView != null && progressBar != null) {
				String imageUrl = campaign.getSFGData().getGalleryUrl() + sfgItem.getImageURL();
				imageLoader.displayImage(imageUrl, imageView, new ProgressBarImageLoadingListener(progressBar));
			}
			
			if (shareCountView != null) {
				shareCountView.setTypeface(dinTypeface, Typeface.BOLD);
				shareCountView.setText("Share Count: "+sfgItem.getShareCount());
			}
			
			if (shelterView != null) {
				shelterView.setTypeface(dinTypeface, Typeface.BOLD);
				shelterView.setText(sfgItem.getShelter());
			}
			
			if (stateView != null) {
				stateView.setTypeface(dinTypeface, Typeface.BOLD);
				stateView.setText(sfgItem.getState());
			}
			
			if (storyView != null) {
				storyView.setTypeface(dinTypeface, Typeface.BOLD);
				storyView.setText(sfgItem.getStory());
			}
		}
	}
	
	public static Intent getIntent(Context context, SFGGalleryItem item, Campaign camp) {
		Intent answer = new Intent(context, SFGItem.class);
		answer.putExtra(DSConstants.EXTRAS_KEY.SFGITEM.getValue(), item);
		answer.putExtra(DSConstants.EXTRAS_KEY.CAMPAIGN.getValue(), camp);
		return answer;
	}

}
