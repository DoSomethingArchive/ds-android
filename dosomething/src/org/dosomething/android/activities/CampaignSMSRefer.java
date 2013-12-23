package org.dosomething.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.analytics.Analytics;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractWebserviceTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import roboguice.inject.InjectView;

public class CampaignSMSRefer extends AbstractActionBarActivity {
	
	private static final String CAMPAIGN = "campaign";
	private static final String SMS_REFER = "sms-refer";
	private static final int GET_CONTACT_ACTIVITY = 1;

	@Inject private LayoutInflater inflater;
	@Inject private UserContext userContext;
    @Inject private @Named("ProximaNova-Bold")Typeface typefaceBold;
	@InjectView(R.id.sms_cell_input) private EditText etCellInput;
	@InjectView(R.id.sms_friends_container) private LinearLayout llFriendsContainer;
	@InjectView(R.id.sms_friends_label) private TextView tvFriendsLabel;
	@InjectView(R.id.sms_name_input) private EditText etNameInput;
	@InjectView(R.id.sms_refer_text) private TextView txtSMSRefer;
    @InjectView(R.id.sms_add_numbers) private Button btnAddNumbers;
	@InjectView(R.id.submit) private Button btnSubmit;
	
	private org.dosomething.android.transfer.Campaign campaign;
	private List<String> friendNumbers;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.sms_refer);

        // Enable ActionBar home button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
		
		campaign = (org.dosomething.android.transfer.Campaign) getIntent().getExtras().get(CAMPAIGN);
		String smsReferText = campaign.getSMSReferText();
		if (smsReferText != null) {
			txtSMSRefer.setText(smsReferText);
		}
		
		friendNumbers = new ArrayList<String>();
		
		btnSubmit.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		onSubmitClick();
        	}
        });

        // Set custom typeface for buttons
        btnAddNumbers.setTypeface(typefaceBold,Typeface.BOLD);
        btnSubmit.setTypeface(typefaceBold, Typeface.BOLD);

		// Auto-fill any fields we already have information for
		prePopulate();
	}

	@Override
	public String getPageName() {
		return "sms-refer";
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If home button is selected on ActionBar, then end the activity
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
	
	// Open user's contacts list and allow them to select numbers from it
	public void addNumbers(View v) {
		Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(intent, GET_CONTACT_ACTIVITY);
	}

	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		// Handle data returned from the Contact picker activity
		if (reqCode == GET_CONTACT_ACTIVITY) {
			if (resultCode == Activity.RESULT_OK) {
				// Getting a contact's number is a 2-step process. First need to
				// get the contact's internal ID. Then need to query a different
				// URI to get the phone number based off the ID.
				Uri contactData = data.getData();
				ContentResolver cr = getContentResolver();
				Cursor c = cr.query(contactData, null, null, null, null);
				if (c.moveToFirst()) {
					String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
					
					// Determine if contact has phone number, and if so, query for it
					int has_phone = Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
					if (has_phone > 0) {
						String contact_id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
						Cursor pc = cr.query(
								ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
								null, 
								ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", 
								new String[] {contact_id},
								null);
						
						if (pc.moveToFirst()) {
							String phone = pc.getString(pc.getColumnIndex(Phone.NUMBER));
							if (phone != null) {
								// Phone successfully retrieved
								addFriendToContact(name, phone);
							}
						}
					}
					// If no number is found, notify user that contact has no number
					else {
						Toast toast = Toast.makeText(this, R.string.sms_refer_contact_has_no_number, Toast.LENGTH_LONG);
						toast.show();
					}
				}
			}
		}
	}
	
	private void addFriendToContact(String name, String phone) {
		tvFriendsLabel.setVisibility(TextView.VISIBLE);
		
		View v = inflater.inflate(R.layout.sms_friend_contact, null);
		TextView tvContact = (TextView)v.findViewById(R.id.contact);
		if (tvContact != null) {
			String strDisplay = name + " - " + phone;
			tvContact.setText(strDisplay);
			
			friendNumbers.add(strDisplay);
			
			llFriendsContainer.addView(v);
		}
	}
	
	private void onSubmitClick() {
		// validate required fields
		if (validatedFields()) {
			// submit form
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("person[first_name]", etNameInput.getText().toString()));
			params.add(new BasicNameValuePair("person[phone]", etCellInput.getText().toString()));
			params.add(new BasicNameValuePair("opt_in_path", Integer.toString(campaign.getMCommonsAlphaOptIn())));
			params.add(new BasicNameValuePair("friends_opt_in_path", Integer.toString(campaign.getMCommonsBetaOptIn())));

			for (int i=0; i<friendNumbers.size(); i++) {
				String friendNumber = friendNumbers.get(i);
				params.add(new BasicNameValuePair("friends[]", friendNumber));
			}
			
			new SMSReferralTask(this, params).execute();
		}
	}
	
	private boolean validatedFields() {
		String errorMsg = "";
		if (etNameInput.getText().toString() == null || etNameInput.getText().length() == 0) {
			String reqField = getString(R.string.sms_name_field);
			errorMsg = getString(R.string.required_field, reqField);
		}
		else if (etCellInput.getText().toString() == null || etCellInput.getText().length() == 0) {
			String reqField = getString(R.string.sms_cell_field);
			errorMsg = getString(R.string.required_field, reqField);
		}
		else if (friendNumbers.size() == 0) {
			errorMsg = getString(R.string.sms_no_friends_selected);
		}
		
		if (errorMsg != "") {
			new AlertDialog.Builder(CampaignSMSRefer.this)
				.setMessage(errorMsg)
				.setCancelable(false)
				.setPositiveButton(getString(R.string.ok_upper), null)
				.create()
				.show();
			return false;
		}
		else {
			return true;
		}
	}
	
	/**
	 * Use values from the UserContext to populate input fields we've already got info for
	 */
	private void prePopulate() {
		etNameInput.setText(userContext.getFirstName());
		etCellInput.setText(userContext.getPhoneNumber());
	}

	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign) {
		Intent answer = new Intent(context, CampaignSMSRefer.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}
	
	private class SMSReferralTask extends AbstractWebserviceTask {
		
		private Context context;
		private List<NameValuePair> params;
		private boolean webOpSuccess;
		
		public SMSReferralTask(Context context, List<NameValuePair> params) {
			super(userContext);
			this.context = context;
			this.params = params;
			this.webOpSuccess = false;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
            setProgressBarIndeterminateVisibility(Boolean.TRUE);
		}

		@Override
		protected void onSuccess() {
			if (webOpSuccess) {
				// Log successful SMS submission
				HashMap<String, String> param = new HashMap<String, String>();
				param.put("submit", "success");
				param.put(CAMPAIGN, campaign.getName());
				param.put("num-shared", Integer.toString(friendNumbers.size()));
				Analytics.logEvent(getPageName(), param);
				Analytics.logEvent("sms", "refer", campaign.getName(), Long.valueOf(friendNumbers.size()));
				
				// Update the ftafs_sent count
				userContext.addFtafsSent(friendNumbers.size());
				
				// Finish this activity, and notify previous activity that sms referral succeeded
				Intent i = new Intent(context, org.dosomething.android.activities.Campaign.class);
				i.putExtra(SMS_REFER, true);
				setResult(Activity.RESULT_OK, i);
				finish();
			}
			else {
				// Log failed SMS submission
				HashMap<String, String> param = new HashMap<String, String>();
				param.put("submit", "failed");
				Analytics.logEvent(getPageName(), param);
				
				new AlertDialog.Builder(CampaignSMSRefer.this)
					.setMessage(getString(R.string.form_submit_failed))
					.setCancelable(false)
					.setPositiveButton(getString(R.string.ok_upper), null)
					.create()
					.show();
			}
		}

		@Override
		protected void onFinish() {
            setProgressBarIndeterminateVisibility(Boolean.FALSE);
		}

		@Override
		protected void onError(Exception e) {
			new AlertDialog.Builder(CampaignSMSRefer.this)
				.setMessage(getString(R.string.form_submit_failed))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.ok_upper), null)
				.create()
				.show();
		}

		@Override
		protected void doWebOperation() throws Exception {
			WebserviceResponse response = doPost(DSConstants.MCOMMONS_API_JOIN_URL, params);
			
			if (response.getStatusCode() >= 400 && response.getStatusCode() < 500) {
				// submission failed
				webOpSuccess = false;
			}
			else {
				// submission succeeded
				webOpSuccess = true;
			}
		}
	}
}
