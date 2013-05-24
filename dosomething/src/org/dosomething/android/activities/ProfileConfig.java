package org.dosomething.android.activities;

import java.util.List;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.cache.DSPreferences;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.dao.MyDAO;
import org.dosomething.android.domain.UserCampaign;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.dosomething.android.widget.CustomActionBar;
import org.json.JSONObject;

import roboguice.inject.InjectView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.inject.Inject;

public class ProfileConfig extends AbstractActivity {
	
	private static final String FROM_PROFILE_CONFIG = "from_profile_config";
	
	@Inject private UserContext userContext;
	@Inject private DSPreferences dsPrefs;
	@Inject private MyDAO dao;
	
	@InjectView(R.id.actionbar) private CustomActionBar actionBar;
	@InjectView(R.id.first_name) private EditText firstNameView;
	@InjectView(R.id.last_name) private EditText lastNameView;
	@InjectView(R.id.email) private EditText emailView;
	@InjectView(R.id.campaigns_joined) private TextView campaignsJoinedView;
	@InjectView(R.id.member_since) private TextView memberSinceView;
	@InjectView(R.id.cause1)private ImageView cause1View;
	@InjectView(R.id.cause2)private ImageView cause2View;
	@InjectView(R.id.cause3)private ImageView cause3View;

	@Override
	protected String getPageName() {
		return "profile-config";
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_config);
		
		// Add logout action
		actionBar.addAction(Login.getLogoutAction(this, userContext));
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Fill out fields with saved data from profile
		firstNameView.setText(userContext.getFirstName());
		lastNameView.setText(userContext.getLastName());
		emailView.setText(userContext.getEmail());
		
		
		// TODO: adjust imageview sizes based on screen size
		if (dsPrefs.getCause1() >= 0) {
			cause1View.setImageResource(dsPrefs.getCauseDrawableByFeedId(dsPrefs.getCause1()));
		}
		else {
			cause1View.setImageResource(R.drawable.cause_blue_bg);
		}
		
		if (dsPrefs.getCause2() >= 0) {
			cause2View.setImageResource(dsPrefs.getCauseDrawableByFeedId(dsPrefs.getCause2()));
		}
		else {
			cause2View.setImageResource(R.drawable.cause_blue_bg);
		}
		
		if (dsPrefs.getCause3() >= 0) {
			cause3View.setImageResource(dsPrefs.getCauseDrawableByFeedId(dsPrefs.getCause3()));
		}
		else {
			cause3View.setImageResource(R.drawable.cause_blue_bg);
		}
		
		String memberSince = userContext.getCreatedTime();
		if (memberSince != null) {
			memberSinceView.setText(getString(R.string.profile_config_member_since, memberSince));
		}
		else {
			String emptyDate = getString(R.string.profile_config_date_empty);
			memberSinceView.setText(getString(R.string.profile_config_member_since, emptyDate));
		}
		
		String uid = userContext.getUserUid();
		List<UserCampaign> userCampaigns = dao.findUserCampaigns(uid);
		int numCampaigns = userCampaigns.size();
		campaignsJoinedView.setText(getString(R.string.profile_config_campaigns_joined, numCampaigns));
		
	}
	
	/**
	 * Launch the CauseSelector activity for user to edit their selected causes
	 */
	public void editCauses(View v) {
		Intent intent = new Intent(this, CauseSelector.class);
		intent.putExtra(FROM_PROFILE_CONFIG, true);
		startActivity(intent);
	}
	
	/**
	 * Called from Save button onClick. Save all fields to appropriate locations
	 * on the local filesystem and to the server if possible.
	 */
	public void save(View v) {
		// Save values to Shared Preferences
		String firstName = firstNameView.getText().toString();
		userContext.setFirstName(firstName);
		
		String lastName = lastNameView.getText().toString();
		userContext.setLastName(lastName);
		
		String email = emailView.getText().toString();
		userContext.setEmail(email);
		
		// Save values to the server profile through the web service
		try {
			JSONObject jsonParams = new JSONObject();
			jsonParams.put("field_user_first_name", firstName);
			jsonParams.put("field_user_last_name", lastName);
			new UpdateProfileTask(jsonParams).execute();
		}
		catch(Exception e) {
			// TODO handle json exception
		}
		
		finish();
	}
	
	/**
	 * Task to update user's profile on the server
	 */
	private class UpdateProfileTask extends AbstractWebserviceTask {
		JSONObject putParams;
		
		public UpdateProfileTask(JSONObject params) {
			super(userContext);
			
			this.putParams = params;
		}

		@Override
		protected void onSuccess() {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void onFinish() {
		}

		@Override
		protected void onError(Exception e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void doWebOperation() throws Exception {
			String uid = userContext.getUserUid();
			String url = DSConstants.API_URL_BASE + "profile/"+uid+".json";
			
			WebserviceResponse response = doPut(url, this.putParams);
			// TODO handle error responses
		}
	}

}
