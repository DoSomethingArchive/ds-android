package org.dosomething.android.test;

import org.dosomething.android.R;
import org.dosomething.android.activities.Login;
import org.dosomething.android.activities.Profile;
import org.dosomething.android.activities.Register;
import org.dosomething.android.context.UserContext;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivityTest extends ActivityInstrumentationTestCase2<Login> {
	
	private Login mActivity;
	private Button mLoginButton;
	private Button mSignUpButton;
	private EditText mUsername;
	private EditText mPassword;
	private UserContext mUserContext;
	
	private com.facebook.widget.LoginButton mFBLogin;
	
	public LoginActivityTest() {
		super(Login.class);
	}
	
	// Called before every test
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Turn off touch mode in device/emulator
		setActivityInitialTouchMode(false);
		
		Context ctx = getInstrumentation().getTargetContext().getApplicationContext();
		mUserContext = new UserContext(ctx);
		
		// Handle to Login activity
		mActivity = getActivity();
		
		mLoginButton = (Button)mActivity.findViewById(R.id.loginButton);
		mSignUpButton = (Button)mActivity.findViewById(R.id.signUpButton);
		mUsername = (EditText)mActivity.findViewById(R.id.username);
		mPassword = (EditText)mActivity.findViewById(R.id.password);
		mFBLogin = (com.facebook.widget.LoginButton)mActivity.findViewById(R.id.facebookLoginButton);
	}
	
	// Called after the end of every test
	@Override
	protected void tearDown() throws Exception {
		// Clear user context data that might've been set by a login call before handle is torn down
		mUserContext.clear();
				
		super.tearDown();
	}
	
	// Test initial conditions
	public void testPreConditions() {
		assertNotNull(mActivity);
		assertNotNull(mUsername);
		assertNotNull(mPassword);
		assertNotNull(mFBLogin);
	}
	
	// Test that Register activity opens
	public void testRegisterButton() {
		Instrumentation instrumentation = getInstrumentation();
		Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(Register.class.getName(), null, false);
		
		TouchUtils.clickView(this, mSignUpButton);
		Activity regActivity = monitor.waitForActivity();
		assertEquals(regActivity.getClass().getName(), Register.class.getName());
		
		instrumentation.removeMonitor(monitor);
		regActivity.finish();
	}
	
	// Test Login button and service
	public void testLogin() {
		Instrumentation instrumentation = getInstrumentation();
		Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(Profile.class.getName(), null, false);
		
		TouchUtils.clickView(this, mUsername);
		instrumentation.sendStringSync("bohemian_test");
		TouchUtils.clickView(this, mPassword);
		instrumentation.sendStringSync("bohemian_test");
		TouchUtils.clickView(this, mLoginButton);
		
		Activity profileActivity = monitor.waitForActivityWithTimeout(5000);
		assertEquals(profileActivity.getClass().getName(), Profile.class.getName());
		profileActivity.finish();
		
		instrumentation.removeMonitor(monitor);
	}

}
