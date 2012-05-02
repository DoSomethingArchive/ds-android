package org.dosomething.android.activities;

import org.dosomething.android.R;
import org.dosomething.android.context.UserContext;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.inject.Inject;
import com.markupartist.android.widget.ActionBar;

public class Profile extends RoboActivity {
	
	private static final int REQ_CAMPAIGNS = 100;

	@Inject private LayoutInflater inflater;
	
	@InjectView(R.id.actionbar) private ActionBar actionbar;
	@InjectView(R.id.content) private LinearLayout content;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        
        TextView logout = new TextView(this);
        logout.setText("Log out");
        logout.setClickable(true);
        logout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new UserContext(Profile.this).clear();
				startActivity(new Intent(getApplicationContext(), Campaigns.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			}
		});
        
        //actionbar.addButton(logout);
        
        content.addView(inflater.inflate(R.layout.profile_no_campaigns, null));
    }
	
	public void findCampaigns(View v){
		startActivityForResult(new Intent(this, Campaigns.class), REQ_CAMPAIGNS);
	}
	
}
