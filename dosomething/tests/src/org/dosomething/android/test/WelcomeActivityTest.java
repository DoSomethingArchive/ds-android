package org.dosomething.android.test;

import org.dosomething.android.activities.Welcome;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

public class WelcomeActivityTest extends ActivityInstrumentationTestCase2<Welcome> {
	
	private Welcome mActivity;

	public WelcomeActivityTest() {
		super(Welcome.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Turns off touch mode in device/emulator so test methods can send key events instead
		setActivityInitialTouchMode(false);
		
		// Create Intent mimicking initial launch of the application
		Intent intent = new Intent();
		intent.addCategory("android.intent.category.LAUNCHER");
		setActivityIntent(intent);
		
		mActivity = getActivity();
	}
	
	public void testPreConditions() {
		// Verify splash screen is created
		assertTrue(mActivity.getSplashScreen() != null);
	}
	
}
