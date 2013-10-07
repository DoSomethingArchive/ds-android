package org.dosomething.android.activities;

import android.os.Bundle;

import org.dosomething.android.R;

public class Welcome extends AbstractActivity {

	private boolean skipWelcome = true;
	
	@Override
	protected String getPageName() {
		return "Welcome";
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
	}

}
