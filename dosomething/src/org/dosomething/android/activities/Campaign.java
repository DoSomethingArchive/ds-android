package org.dosomething.android.activities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.dosomething.android.R;
import org.dosomething.android.context.UserContext;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.inject.Inject;
import com.markupartist.android.widget.ActionBar;
import com.nostra13.universalimageloader.core.ImageLoader;

public class Campaign extends RoboActivity {

	private static final String CAMPAIGN = "campaign";

	private static final int REQ_LOGIN_FOR_SIGN_UP = 111;

	@Inject private ImageLoader imageLoader;

	@InjectView(R.id.actionbar) private ActionBar actionBar;
	@InjectView(R.id.image) private ImageView imgLogo;
	@InjectView(R.id.image_container) private LinearLayout llImageContainer;
	@InjectView(R.id.dates) private TextView txtDates;
	@InjectView(R.id.teaser) private TextView txtTeaser;
	@InjectView(R.id.howTo) private Button btnHowTo;
	@InjectView(R.id.gallery) private Button btnGallery;
	@InjectView(R.id.prizes) private Button btnPrizes;
	@InjectView(R.id.resources) private Button btnResources;
	@InjectView(R.id.faq) private Button btnFaq;

	private org.dosomething.android.transfer.Campaign campaign;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.campaign);

		actionBar.setHomeAction(Campaigns.getHomeAction(this));

		campaign = (org.dosomething.android.transfer.Campaign) getIntent().getSerializableExtra(CAMPAIGN);

		actionBar.setTitle(campaign.getName());

		txtDates.setText(formatDateRange(campaign));
		txtTeaser.setText(campaign.getTeaser());

		llImageContainer.setBackgroundColor(Color.parseColor(campaign.getBackgroundColor()));
		imageLoader.displayImage(campaign.getLogoUrl(), imgLogo);

		if(!nullOrEmpty(campaign.getHowTos())){
			btnHowTo.setVisibility(Button.VISIBLE);
		}

		if(campaign.getGallery() != null){
			btnGallery.setVisibility(Button.VISIBLE);
		}

		if(campaign.getPrize() != null){
			btnPrizes.setVisibility(Button.VISIBLE);
		}

		if(!nullOrEmpty(campaign.getResources())){
			btnResources.setVisibility(Button.VISIBLE);
		}

		if(!nullOrEmpty(campaign.getFaqs())){
			btnFaq.setVisibility(Button.VISIBLE);
		}

	}

	private static final boolean nullOrEmpty(List<?> list){
		return list == null || list.isEmpty();
	}

	public void howTo(View v){
		startActivity(CampaignHowTo.getIntent(this, campaign));
	}

	public void gallery(View v){
		startActivity(CampaignGallery.getIntent(this, campaign));
	}

	public void prizes(View v){
		startActivity(CampaignPrizes.getIntent(this, campaign));
	}

	public void locator(View v){
		//Toast.makeText(this, "do locator", Toast.LENGTH_LONG).show();
		startActivity(ReportBack.getIntent(this, campaign));
	}

	public void resources(View v){
		startActivity(CampaignResources.getIntent(this, campaign));
	}

	public void faq(View v){
		startActivity(CampaignFAQ.getIntent(this, campaign));
	}

	private String formatDateRange(org.dosomething.android.transfer.Campaign campaign){
		SimpleDateFormat mf = new SimpleDateFormat("MMMMM");

		Calendar scal = Calendar.getInstance();
		scal.setTime(campaign.getStartDate());

		Calendar ecal = Calendar.getInstance();
		ecal.setTime(campaign.getEndDate());

		int sday = scal.get(Calendar.DAY_OF_MONTH);
		int eday = ecal.get(Calendar.DAY_OF_MONTH);

		return mf.format(campaign.getStartDate()) + " " + sday + getOrdinalFor(sday)
				+ "-" + mf.format(campaign.getEndDate()) + " " + eday + getOrdinalFor(eday);
	}

	private static String getOrdinalFor(int value) {
		int mod = value % 10;
		
		switch (mod) {
		case 1:
			return "st";
		case 2:
			return "nd";
		case 3:
			return "rd";
		default:
			return "th";
		}
	}

	public void signUp(View v){
		if(new UserContext(this).isLoggedIn()){
			startActivity(CampaignSignedUp.getIntent(this, campaign));
		}else{
			startActivityForResult(new Intent(this, Login.class), REQ_LOGIN_FOR_SIGN_UP);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == REQ_LOGIN_FOR_SIGN_UP && resultCode == RESULT_OK){
			if(new UserContext(this).isLoggedIn()){
				startActivity(CampaignSignedUp.getIntent(this, campaign));
			}
		}
	}

	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign) {
		Intent answer = new Intent(context, Campaign.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}

}
