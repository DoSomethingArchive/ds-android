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
	
	private String initialFirstName;
	private String initialLastName;

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
		initialFirstName = userContext.getFirstName();
		initialLastName = userContext.getLastName();
		
		firstNameView.setText(initialFirstName);
		lastNameView.setText(initialLastName);
		emailView.setText(userContext.getEmail());

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
		String firstName = firstNameView.getText().toString();
		String lastName = lastNameView.getText().toString();
		
		// Only update values if they've changed
		if (!initialFirstName.equals(firstName) || !initialLastName.equals(lastName)) {
			// Save values to Shared Preferences
			userContext.setFirstName(firstName);
			userContext.setLastName(lastName);
			
			// Save values to the server profile through the web service
			// TODO update email if it's not empty
			try {
				JSONObject jsonParams = new JSONObject();
				jsonParams.put("field_user_first_name", firstName);
				jsonParams.put("field_user_last_name", lastName);
				new UpdateProfileTask(jsonParams).execute();
			}
			catch(Exception e) {
				// Profile not updating is non-fatal. Just continue on to next Activity.
			}
		}
		
		finish();
	}
	
	/**
	 * Task to update user's profile on the server
	 */
	private class UpdateProfileTask extends AbstractWebserviceTask {
		private JSONObject putParams;
		private boolean updateSuccess;
		
		public UpdateProfileTask(JSONObject params) {
			super(userContext);
			
			this.putParams = params;
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
		protected void onPostExecute(Exception exception) {
			super.onPostExecute(exception);
			
			// TODO: may want to find a way to prompt user through an AlertDialog
			// that the profile update failed if updateSuccess == false
			// Can't use ProfileConfig as a context though since the activity
			// will likely already be closed by the time the profile update
			// is complete.
		}

		@Override
		protected void doWebOperation() throws Exception {
			String uid = userContext.getUserUid();
			String url = DSConstants.API_URL_BASE + "profile/"+uid+".json";
			
			WebserviceResponse response = doPut(url, this.putParams);
			if (response.getStatusCode() >= 400 && response.getStatusCode() < 500) {
				updateSuccess = false;
			}
			else {
				updateSuccess = true;
			}
			
		}
	}

}
