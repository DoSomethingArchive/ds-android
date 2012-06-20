package org.dosomething.android.activities;

import org.dosomething.android.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Welcome extends AbstractActivity {

	@Override
	protected String getPageName() {
		return "welcome";
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
	}
	
	public void continueToCauses(View v) {
		startActivity(new Intent(this, CauseSelector.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		finish();
	}

}
