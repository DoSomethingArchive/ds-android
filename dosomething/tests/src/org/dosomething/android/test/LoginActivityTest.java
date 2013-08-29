package org.dosomething.android.test;

import java.util.concurrent.CountDownLatch;

import org.dosomething.android.R;
import org.dosomething.android.activities.Login;
import org.dosomething.android.activities.Profile;
import org.dosomething.android.activities.Register;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractWebserviceTask;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
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
	
	/**
	 * setUp() called before every test
	 */
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
	
	/**
	 * tearDown() called after the end of every test
	 */
	@Override
	protected void tearDown() throws Exception {
		// Clear user context data that might've been set by a login call before handle is torn down
		mUserContext.clear();
				
		super.tearDown();
	}
	
	/**
	 * Test initial conditions
	 */
	public void testPreConditions() {
		assertNotNull(mActivity);
		assertNotNull(mUsername);
		assertNotNull(mPassword);
		assertNotNull(mLoginButton);
		assertNotNull(mSignUpButton);
		assertNotNull(mFBLogin);
	}
	
	/**
	 * Test that Register activity opens
	 */
	public void testRegisterButton() {
		Instrumentation instrumentation = getInstrumentation();
		Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(Register.class.getName(), null, false);
		
		mActivity.runOnUiThread(
			new Runnable() {
				public void run() {
					mSignUpButton.requestFocus();
					mSignUpButton.setSelected(true);
				}
			}
		);
		
		this.sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
		Activity regActivity = monitor.waitForActivityWithTimeout(10000);
		assertEquals(regActivity.getClass().getName(), Register.class.getName());
		
		instrumentation.removeMonitor(monitor);
		regActivity.finish();
	}
	
	/**
	 * Tests the Login task
	 * @throws InterruptedException
	 */
	public void testLogin() throws InterruptedException {
		Instrumentation instrumentation = getInstrumentation();
		Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(Profile.class.getName(), null, false);
		
		CountDownLatch countDownLatch = new CountDownLatch(1);
		
		// Setup dummy date on separate UI thread
		mActivity.runOnUiThread(new LoginUiPrep(countDownLatch));
		
		// Wait for UI prep to complete before continuing
		countDownLatch.await();

		// Execute the Login task
		mActivity.logIn(null);
		
		AbstractWebserviceTask loginTask = mActivity.getDSLoginTask();
		
		Activity profileActivity = monitor.waitForActivityWithTimeout(10000);
		assertEquals(profileActivity.getClass().getName(), Profile.class.getName());
		
		loginTask.cancel(true);
		profileActivity.finish();
		
		instrumentation.removeMonitor(monitor);
	}
	
	/**
	 * Preps UI elements for Login task 
	 */
	private class LoginUiPrep implements Runnable {
		private CountDownLatch mCountDownLatch;
		
		public LoginUiPrep(CountDownLatch countDownLatch) {
			mCountDownLatch = countDownLatch;
		}
		
		public void run() {
			mUsername.setText("bohemian_test");
			mPassword.setText("bohemian_test");
			
			mCountDownLatch.countDown();
		}
	}

}
