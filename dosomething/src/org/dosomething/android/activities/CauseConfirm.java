package org.dosomething.android.activities;

import org.dosomething.android.R;
import org.dosomething.android.widget.CustomActionBar;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.markupartist.android.widget.ActionBar.Action;

public class CauseConfirm extends AbstractActivity {
	
	@InjectView(R.id.actionbar) private CustomActionBar actionBar;

	@Override
	protected String getPageName() {
		return "cause-confirm";
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cause_confirm);
		
		actionBar.addAction(profileButtonAction);
	}
	
	private final Action profileButtonAction = new Action(){

		@Override
		public int getDrawable() {
			return R.drawable.action_bar_profile;
		}

		@Override
		public void performAction(View view) {
			Context context = getApplicationContext();
			startActivity(Profile.getIntent(context));
			finish();
		}
	};

	public void continueToProfile(View v) {
		startActivity(new Intent(this, Profile.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		finish();
	}
}
