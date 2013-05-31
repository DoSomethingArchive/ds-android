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
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.inject.Inject;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SFGItem extends AbstractActivity {
	
	@Inject private ImageLoader imageLoader;

	@InjectView(R.id.actionbar) private CustomActionBar actionBar;
	@InjectView(R.id.top_text) private TextView topTextView;
	@InjectView(R.id.bottom_text) private TextView bottomTextView;
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
			topTextView.setText(sfgItem.getTopText());
			bottomTextView.setText(sfgItem.getBottomText());
			if (campaign != null) {
				String imageUrl = campaign.getSFGGalleryUrl() + sfgItem.getImageURL();
				imageLoader.displayImage(imageUrl, imageView, new ProgressBarImageLoadingListener(progressBar));
			}
			shareCountView.setText("Share Count: "+sfgItem.getShareCount());
			shelterView.setText(sfgItem.getShelter());
			stateView.setText(sfgItem.getState());
			storyView.setText(sfgItem.getStory());
		}
	}
	
	public static Intent getIntent(Context context, SFGGalleryItem item, Campaign camp) {
		Intent answer = new Intent(context, SFGItem.class);
		answer.putExtra(DSConstants.EXTRAS_KEY.SFGITEM.getValue(), item);
		answer.putExtra(DSConstants.EXTRAS_KEY.CAMPAIGN.getValue(), camp);
		return answer;
	}

}
