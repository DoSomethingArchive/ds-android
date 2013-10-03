package org.dosomething.android.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.Builder;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.dosomething.android.DSConstants;
import org.dosomething.android.FadeInResizeBitmapDisplayer;
import org.dosomething.android.R;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.SFGGalleryItem;
import org.dosomething.android.widget.ProgressBarImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

public class SFGItem extends AbstractActivity {
	
	@Inject private ImageLoader imageLoader;
	@Inject @Named("DINComp-CondBold")Typeface dinTypeface;
	@Inject private UserContext userContext;

	@InjectView(R.id.image) private ImageView imageView;
	@InjectView(R.id.share_count) private TextView shareCountView;
	@InjectView(R.id.shelter) private TextView shelterView;
	@InjectView(R.id.location) private TextView locationView;
	@InjectView(R.id.story) private TextView storyView; 
	@InjectView(R.id.progressBar) private ProgressBar progressBar;
	@InjectView(R.id.findButton) private Button findButton;
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
		
		campaign = (Campaign)getIntent().getExtras().get(DSConstants.EXTRAS_KEY.CAMPAIGN.getValue());
		
		shareButton.setTypeface(dinTypeface);
		shareButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				publishFeedDialog();
			}
		});
		
		if (campaign.getSFGData().getLocatorType() != null) {
			findButton.setVisibility(Button.VISIBLE);
			findButton.setTypeface(dinTypeface);
			findButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (campaign.getSFGData().getLocatorType().equals("adoptapet")) {
						// TODO: this url is specific to Pics for Pets. Will need to be changed for other campaigns.
						String url = "http://www.adoptapet.com/animal-shelter-search?city_or_zip=" + sfgItem.getCity()
									+ ",%20" + sfgItem.getState()
									+ "&shelter_name=" + sfgItem.getShelter()
									+ "&distance=50&adopts_out=all";
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse(url));
						startActivity(i);
					}
				}
			});
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (sfgItem != null) {
			if (campaign != null && imageView != null && progressBar != null) {
				String imageUrl = campaign.getSFGData().getGalleryUrl() + sfgItem.getImageURL();
				DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
					.displayer(new FadeInResizeBitmapDisplayer(DSConstants.IMAGE_LOADER_FADE_IN_TIME))
					.build();
				imageLoader.displayImage(imageUrl, imageView, imageOptions, new ProgressBarImageLoadingListener(progressBar));
				imageView.setAdjustViewBounds(true);
			}
			
			if (shareCountView != null) {
				shareCountView.setTypeface(dinTypeface, Typeface.BOLD);
				shareCountView.setText("Share Count: "+sfgItem.getShareCount());
			}
			
			if (shelterView != null) {
				shelterView.setTypeface(dinTypeface, Typeface.BOLD);
				shelterView.setText(sfgItem.getShelter());
			}
			
			if (locationView != null) {
				locationView.setTypeface(dinTypeface, Typeface.BOLD);
				locationView.setText(sfgItem.getCity() + ", " + sfgItem.getState());
			}
			
			if (storyView != null && sfgItem.getStory() != null
					&& !sfgItem.getStory().equalsIgnoreCase("null")) {
				storyView.setText(sfgItem.getStory());
			}
			else {
				storyView.setVisibility(TextView.GONE);
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
				boolean shareSuccess = values != null && !values.isEmpty();
				if (shareSuccess) {
					// Send POST to update the share count
					int postId = sfgItem.getId();
					String uid = userContext.getUserUid();
					new PostShareTask(postId, uid).execute();
					
					Toast.makeText(SFGItem.this, campaign.getSFGData().getShareSuccessMsg(), Toast.LENGTH_LONG).show();
				}
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
	
	/**
	 * Notify database about a post being shared.
	 */
	private class PostShareTask extends AbstractWebserviceTask {
		private int postId;
		private String uid;
		
		public PostShareTask(int _postId, String _uid) {
			super(userContext);
			
			postId = _postId;
			uid = _uid;
		}

		@Override
		protected void onSuccess() {
		}

		@Override
		protected void onFinish() {
		}

		@Override
		protected void onError(Exception e) {
		}

		@Override
		protected void doWebOperation() throws Exception {
			String url = campaign.getSFGData().getGalleryUrl() + "shares.json?key=" + DSConstants.PICS_API_KEY;
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("share[uid]", uid));
			params.add(new BasicNameValuePair("share[post_id]", String.valueOf(postId)));
			
			doPost(url, params);
		}
	}
}
