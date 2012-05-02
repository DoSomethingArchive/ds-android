package org.dosomething.android.activities;

import org.dosomething.android.R;
import org.dosomething.android.context.UserContext;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.google.inject.Inject;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class Profile extends RoboActivity {
	
	@Inject private LayoutInflater inflater;
	
	@InjectView(R.id.actionbar) private ActionBar actionBar;
	@InjectView(R.id.content) private LinearLayout content;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        
        actionBar.addAction(logoutAction);
        
        content.addView(inflater.inflate(R.layout.profile_no_campaigns, null));
    }
	
	private final Action logoutAction = new Action(){

		@Override
		public int getDrawable() {
			return R.drawable.action_bar_profile;
		}

		@Override
		public void performAction(View view) {
			new UserContext(Profile.this).clear();
			startActivity(new Intent(getApplicationContext(), Campaigns.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		}
		
	};
	
	public void findCampaigns(View v){
		finish();
	}

	public static Intent getIntent(Context context) {
		return new Intent(context, Profile.class);
	}
	
}
