package org.dosomething.android.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.activities.Register;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractWebserviceTask;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivityTest extends ActivityInstrumentationTestCase2<Register> {

	private Register mActivity;
	private EditText mUsername;
	private EditText mMobile;
	private EditText mFirstName;
	private EditText mLastName;
	private EditText mEmail;
	private EditText mPassword;
	private EditText mPasswordConfirm;
	private EditText mBirthday;
	private Button mRegisterButton;
	
	private UserContext mUserContext;
	
	public RegisterActivityTest() {
		super(Register.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		setActivityInitialTouchMode(false);
		
		Context ctx = getInstrumentation().getTargetContext().getApplicationContext();
		mUserContext = new UserContext(ctx);
		mUserContext.clear();
		
		mActivity = getActivity();
		
		mUsername = (EditText)mActivity.findViewById(R.id.username_name);
		mMobile = (EditText)mActivity.findViewById(R.id.mobile);
		mFirstName = (EditText)mActivity.findViewById(R.id.first_name);
		mLastName = (EditText)mActivity.findViewById(R.id.last_name);
		mEmail = (EditText)mActivity.findViewById(R.id.email);
		mPassword = (EditText)mActivity.findViewById(R.id.password);
		mPasswordConfirm = (EditText)mActivity.findViewById(R.id.confirm_password);
		mBirthday = (EditText)mActivity.findViewById(R.id.birthday);
		mRegisterButton = (Button)mActivity.findViewById(R.id.submit);
		
		// Use QA server for API calls
		DSConstants.API_URL_USER_DELETE = "http://qa.dosomething.org/rest/user/%d.json";
		DSConstants.API_URL_USER_REGISTER = "http://qa.dosomething.org/rest/user/register.json";
	}
	
	@Override
	protected void tearDown() throws Exception {
		CountDownLatch countDownLatch = new CountDownLatch(1);
		
		// Delete dummy user account from the server
		String sUid = mUserContext.getUserUid();
		if (sUid != null && sUid.length() > 0) {
			int uid = Integer.valueOf(sUid).intValue();
			String url = String.format(DSConstants.API_URL_USER_DELETE, uid);
			new DeleteUserTask(url, countDownLatch).execute();
		}
		
		// Wait for DeleteUserTask to finish before finishing tearDown()
		countDownLatch.await(15, TimeUnit.SECONDS);
		mUserContext.clear();
		
		super.tearDown();
	}
	
	/**
	 * Preps UI elements for the Register task
	 */
	private class RegisterUiPrep implements Runnable {
		private CountDownLatch cdLatch;
		
		public RegisterUiPrep(CountDownLatch countDownLatch) {
			cdLatch = countDownLatch;
		}
		
		public void run() {
			mUsername.setText("ds-android-test");
			mMobile.setText("5555555555");
			mFirstName.setText("ds-android-test-first-name");
			mLastName.setText("ds-android-test-last-name");
			mEmail.setText("ds-android-test@dosomething.org");
			mPassword.setText("ds-android-test");
			mPasswordConfirm.setText("ds-android-test");
			mBirthday.setText("01-01-2000");
			
			mRegisterButton.requestFocus();
			mRegisterButton.setSelected(true);
			
			cdLatch.countDown();
		}
	}
	
	/**
	 * Tests the user registration service
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void testRegistration() throws InterruptedException, ExecutionException {
		assertNotNull(mUsername);
		assertNotNull(mMobile);
		assertNotNull(mFirstName);
		assertNotNull(mLastName);
		assertNotNull(mEmail);
		assertNotNull(mPassword);
		assertNotNull(mPasswordConfirm);
		assertNotNull(mBirthday);
		assertNotNull(mRegisterButton);
		
		CountDownLatch countDownLatch = new CountDownLatch(1);
		
		mActivity.runOnUiThread(new RegisterUiPrep(countDownLatch));
		countDownLatch.await(15, TimeUnit.SECONDS);
		
		mActivity.register(null);
		// This will block until task is complete
		mActivity.registerTask.get();
		
		// A UID > 0 indicates a successful registration
		assertTrue(Integer.valueOf(mUserContext.getUserUid()).intValue() > 0);
	}
	
	/**
	 * Meant to be executed during the tearDown(). This should delete the
	 * dummy user account that was just created on the target server.
	 */
	private class DeleteUserTask extends AbstractWebserviceTask {
		private CountDownLatch mCountDownLatch;
		private String mUrl;

		public DeleteUserTask(String url, CountDownLatch countDownLatch) {
			super(mUserContext);
			
			mUrl = url;
			mCountDownLatch = countDownLatch;
		}
		
		@Override
		protected void doWebOperation() throws Exception {
			doDelete(mUrl);
		}

		@Override
		protected void onSuccess() {}

		@Override
		protected void onFinish() {
			// Signal to waiting threads that task is complete
			mCountDownLatch.countDown();
		}

		@Override
		protected void onError(Exception e) {}
	}
}
