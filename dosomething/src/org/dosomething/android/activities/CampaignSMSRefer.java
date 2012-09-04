package org.dosomething.android.activities;

import org.dosomething.android.R;
import org.dosomething.android.widget.CustomActionBar;

import roboguice.inject.InjectView;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class CampaignSMSRefer extends AbstractActivity {
	
	private static final String CAMPAIGN = "campaign";
	private static final int GET_CONTACT_ACTIVITY = 1;
	
	@InjectView(R.id.actionbar) private CustomActionBar actionBar;
	@InjectView(R.id.sms_refer_text) private TextView txtSMSRefer;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sms_refer);
		
		actionBar.addAction(Campaigns.getHomeAction(this));
		
		org.dosomething.android.transfer.Campaign campaign = (org.dosomething.android.transfer.Campaign) getIntent().getExtras().get(CAMPAIGN);
		String smsReferText = campaign.getSMSReferText();
		if (smsReferText != null) {
			txtSMSRefer.setText(smsReferText);
		}
	}

	@Override
	protected String getPageName() {
		return "sms-refer";
	}
	
	// Open user's contacts list and allow them to select numbers from it
	public void addNumbers(View v) {
		Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(intent, GET_CONTACT_ACTIVITY);
	}

	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		Log.v("SMS_TEST", "received result from activity");
		Log.v("SMS_TEST", "reqCode="+reqCode+" / resultCode="+resultCode);
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
								// TODO: Phone successfully retrieved, display to screen
							}
						}
					}
				}
			}
		}
	}

	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign) {
		Intent answer = new Intent(context, CampaignSMSRefer.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}
}
