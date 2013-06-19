package org.dosomething.android.activities;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.SFGGalleryItem;
import org.dosomething.android.widget.CustomActionBar;
import org.dosomething.android.widget.ProgressBarImageLoadingListener;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.FacebookException;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.Builder;
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
	@InjectView(R.id.shareButton) private Button shareButton;
	
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
		
		shareButton.setTypeface(dinTypeface);
		shareButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				publishFeedDialog();
			}
		});
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
	
	/**
	 * Displaying the Facebook feed dialog with info about this submission
	 */
	private void publishFeedDialog() {
	    Bundle params = new Bundle();
	    params.putString("name", getString(R.string.campaign_sfg_p4p_share_title));
	    params.putString("caption", "DoSomething.org - " + campaign.getName());
	    params.putString("description", getString(R.string.campaign_sfg_p4p_share_description, sfgItem.getName()));
	    params.putString("link", campaign.getSFGData().getGalleryUrl() + sfgItem.getId());
	    params.putString("picture", campaign.getSFGData().getGalleryUrl() + sfgItem.getImageURL());

	    Builder feedDialogBuilder = new WebDialog.Builder(SFGItem.this, DSConstants.FACEBOOK_APP_ID, "feed", params);
	    WebDialog feedDialog = feedDialogBuilder.build();
	    feedDialog.setOnCompleteListener(new WebDialog.OnCompleteListener() {
			@Override
			public void onComplete(Bundle values, FacebookException error) {
				// TODO implement
				boolean shareSuccess = values != null && !values.isEmpty();
			}
	    });
	    feedDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO implement
			}
		});
	    feedDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO implement
			}
		});
	    feedDialog.show();
	}
	
	public static Intent getIntent(Context context, SFGGalleryItem item, Campaign camp) {
		Intent answer = new Intent(context, SFGItem.class);
		answer.putExtra(DSConstants.EXTRAS_KEY.SFGITEM.getValue(), item);
		answer.putExtra(DSConstants.EXTRAS_KEY.CAMPAIGN.getValue(), camp);
		return answer;
	}

}
