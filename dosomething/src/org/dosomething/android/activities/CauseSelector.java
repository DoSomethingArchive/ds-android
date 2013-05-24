package org.dosomething.android.activities;

import java.util.HashMap;

import org.dosomething.android.R;
import org.dosomething.android.analytics.Analytics;
import org.dosomething.android.cache.DSPreferences;

import roboguice.inject.InjectView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

public class CauseSelector extends AbstractActivity {

	private final static int[] CAUSE_BUTTON_IDS = {
		R.id.animals_btn,
		R.id.bullying_violence_btn,
		R.id.disasters_btn,
		R.id.discrimination_btn,
		R.id.education_btn,
		R.id.environment_btn,
		R.id.homelessness_poverty_btn,
		R.id.human_rights_btn,
		R.id.our_troops_btn,
		R.id.health_btn,
		R.id.sex_relationships_btn
	};
	
	private static final String FROM_CAUSE_SEL = "from_cause_sel";
	private static final String FROM_PROFILE_CONFIG = "from_profile_config";

	@InjectView(R.id.cause1) private TextView textCause1;
	@InjectView(R.id.cause2) private TextView textCause2;
	@InjectView(R.id.cause3) private TextView textCause3;
	
	private boolean launchProfileOnFinish = true;
	
	@Override
	protected String getPageName() {
		return "cause-selector";
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cause_selector);
		
		if (getIntent() != null && getIntent().getExtras() != null) {
			boolean bFromProfileConfig = getIntent().getExtras().getBoolean(FROM_PROFILE_CONFIG, false);
			launchProfileOnFinish = !bFromProfileConfig;
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// Update text and toggle buttons if previously selected
		DSPreferences prefs = new DSPreferences(this);
		updateCauseDisplayForId(prefs.getCause1());
		updateCauseDisplayForId(prefs.getCause2());
		updateCauseDisplayForId(prefs.getCause3());
	}
	
	private void updateCauseDisplayForId(int cause_id) {
		DSPreferences prefs = new DSPreferences(this);
		int res_id = prefs.getCauseResId(cause_id);
		ToggleButton tb = (ToggleButton) findViewById(res_id);
		if (tb != null) {
			tb.setChecked(true);
			updateCauseList(res_id, true);
		}
	}
	
	public void continueToProfile(View v) {
		HashMap<String, String> param = new HashMap<String, String>();
		if (textCause1.getText().length() > 0) {
			param.put("cause-1", textCause1.getText().toString());
		}
		else if (textCause2.getText().length() > 0) {
			param.put("cause-2", textCause2.getText().toString());
		}
		else if (textCause3.getText().length() > 0) {
			param.put("cause-3", textCause3.getText().toString());
		}
		Analytics.logEvent("causes-selected", param);
		
		if (launchProfileOnFinish) {
			Intent intent = new Intent(this, Profile.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(FROM_CAUSE_SEL, true);
			startActivity(intent);
		}
		
		finish();
	}

	public void onCauseSelected(View v) {
		ToggleButton tb = (ToggleButton) v;
		if (tb == null)
			return;
		
		int id = tb.getId();
		boolean isChecked = tb.isChecked();
		
		updateCauseList(id, isChecked);
	}
	
	private void updateCauseList(int id, boolean isChecked) {
		String strCause = "";
		switch (id) {
		case R.id.animals_btn:
			strCause = getString(R.string.cause_animals);
			break;
		case R.id.bullying_violence_btn:
			strCause = getString(R.string.cause_bullying_violence);
			break;
		case R.id.disasters_btn:
			strCause = getString(R.string.cause_disasters);
			break;
		case R.id.discrimination_btn:
			strCause = getString(R.string.cause_discrimination);
			break;
		case R.id.education_btn:
			strCause = getString(R.string.cause_education);
			break;
		case R.id.environment_btn:
			strCause = getString(R.string.cause_environment);
			break;
		case R.id.homelessness_poverty_btn:
			strCause = getString(R.string.cause_homelessness_poverty);
			break;
		case R.id.human_rights_btn:
			strCause = getString(R.string.cause_human_rights);
			break;
		case R.id.our_troops_btn:
			strCause = getString(R.string.cause_our_troops);
			break;
		case R.id.health_btn:
			strCause = getString(R.string.cause_health);
			break;
		case R.id.sex_relationships_btn:
			strCause = getString(R.string.cause_sex_relationships);
			break;
		}
		
		DSPreferences prefs = new DSPreferences(this);
		
		// Add cause to the list
		if (isChecked) {
			if (textCause1.getText().length() == 0) {
				textCause1.setText(strCause);
			}
			else if (textCause2.getText().length() == 0) {
				textCause2.setText(strCause);
			}
			else if (textCause3.getText().length() == 0) {
				textCause3.setText(strCause);
				disableButtons();
			}
			
			// Save to SharedPreferences
			prefs.setCause(id);
		}
		// Remove cause from the list
		else {
			if (textCause1.getText().toString().compareTo(strCause) == 0) {
				// Remove 1st case
				textCause1.setText("");
				
				// Move 2nd cause up to 1st
				if (textCause2.getText().length() > 0) {
					textCause1.setText( textCause2.getText() );
					textCause2.setText("");
				}
				
				// Move 3rd cause up to 2nd
				if (textCause3.getText().length() > 0) {
					textCause2.setText( textCause3.getText() );
					textCause3.setText("");
				}
			}
			else if (textCause2.getText().toString().compareTo(strCause) == 0) {
				// Remove 2nd cause
				textCause2.setText("");
				
				// Move 3rd cause up to 2nd
				if (textCause3.getText().length() > 0) {
					textCause2.setText( textCause3.getText() );
					textCause3.setText("");
				}
			}
			else if (textCause3.getText().toString().compareTo(strCause) == 0) {
				// Remove 3rd cause
				textCause3.setText("");
			}
			
			enableButtons();
			
			// Remove from SharedPreferences
			prefs.unsetCause(id);
		}
	}
	
	private void disableButtons() {
		for (int i = 0; i < CAUSE_BUTTON_IDS.length; i++) {
			ToggleButton tb = (ToggleButton)findViewById(CAUSE_BUTTON_IDS[i]);
			if (!tb.isChecked()) {
				tb.setEnabled(false);
			}
		}
	}
	
	private void enableButtons() {
		for (int i = 0; i < CAUSE_BUTTON_IDS.length; i++) {
			ToggleButton tb = (ToggleButton)findViewById(CAUSE_BUTTON_IDS[i]);
			tb.setEnabled(true);
		}
	}
}
