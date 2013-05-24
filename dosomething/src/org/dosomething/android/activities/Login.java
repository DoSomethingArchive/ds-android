package org.dosomething.android.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.analytics.Analytics;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.json.JSONObject;

import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.google.inject.Inject;
import com.markupartist.android.widget.ActionBar.Action;

public class Login extends AbstractActivity {
	
	private static final String TAG = "Login";
	private static final int REQ_FACEBOOK_LOGIN = 111;
	private static final int REQ_SIGN_UP = 112;
	
    
	private Facebook facebook = new Facebook("525191857506466");
	
	@InjectView(R.id.username) private EditText username;
	@InjectView(R.id.password) private EditText password;
	
	@Inject private UserContext userContext;
	
	private Context context;
	
	@Override
	protected String getPageName() {
		return "login";
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        context = this;
    }
    
    public void logIn(View v){
    	String email = this.username.getText().toString();
    	String password = this.password.getText().toString();
    	
    	new MyLoginTask(email, password).execute();
    }
    
    /**
     * After a login succeeds, takes json object returned and updates the user
     * context with the contained info.
     * 
     * @param obj JSONObject of user and profile data
     */
	private void updateUserContext(JSONObject obj) throws Exception {
    	JSONObject user = obj.getJSONObject("user");
		
    	if (user != null && obj != null) {
			userContext.setLoggedIn(
				user.getString("name"),
				user.getString("mail"),
				user.getString("uid"),
				obj.getString("sessid"),
				obj.getString("session_name"),
				obj.getLong("session_cache_expire"));
			
			userContext.setCreatedTime(user.getString("created"));
    	}
		
		JSONObject profile = obj.optJSONObject("profile");
		if (profile != null) {
			
			String firstName;
			if (profile.optJSONObject("field_user_first_name") != null
					&& profile.optJSONObject("field_user_first_name").optJSONArray("und") != null
					&& profile.optJSONObject("field_user_first_name").optJSONArray("und").optJSONObject(0) != null
					&& (firstName = profile.optJSONObject("field_user_first_name").optJSONArray("und").getJSONObject(0).optString("value", null)) != null)
			{
				userContext.setFirstName(firstName);
			}
			
			String lastName;
			if (profile.optJSONObject("field_user_last_name") != null
					&& profile.optJSONObject("field_user_last_name").optJSONArray("und") != null
					&& profile.optJSONObject("field_user_last_name").optJSONArray("und").optJSONObject(0) != null
					&& (lastName = profile.optJSONObject("field_user_last_name").optJSONArray("und").optJSONObject(0).optString("value", null)) != null)
			{
				userContext.setLastName(lastName);
			}
			

			JSONObject address;
			if (profile.optJSONObject("field_user_address") != null
					&& profile.optJSONObject("field_user_address").optJSONArray("und") != null
					&& (address = profile.optJSONObject("field_user_address").optJSONArray("und").optJSONObject(0)) != null)
			{
				String addr1 = address.optString("thoroughfare");
				String addr2 = address.optString("premise");
				String city = address.optString("locality");
				String state = address.optString("administrative_area");
				String zip = address.optString("postal_code");
				
				userContext.setAddr1(addr1);
				userContext.setAddr2(addr2);
				userContext.setAddrCity(city);
				userContext.setAddrState(state);
				userContext.setAddrZip(zip);
			}
		}
		
    }
    
    private void goToProfile(){
    	startActivity(new Intent(this, Profile.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    	finish();
    }
    
    public void signUp(View v){
    	startActivityForResult(new Intent(this, Register.class), REQ_SIGN_UP);
    }
    
    public void facebookLogin(View v){

    	facebook.authorize(this, new String[]{"email","user_birthday"}, REQ_FACEBOOK_LOGIN, new DialogListener(){

			@Override
			public void onComplete(Bundle values) {
				
				new MyFacebookLoginTask(facebook.getAccessToken()).execute();
			}

			@Override
			public void onFacebookError(FacebookError e) {
				new AlertDialog.Builder(Login.this)
					.setMessage(getString(R.string.facebook_auth_failed))
					.setCancelable(false)
					.setPositiveButton(getString(R.string.ok_upper), null)
					.create()
					.show();
			}

			@Override
			public void onError(DialogError e) {
				new AlertDialog.Builder(Login.this)
					.setMessage(getString(R.string.facebook_auth_failed))
					.setCancelable(false)
					.setPositiveButton(getString(R.string.ok_upper), null)
					.create()
					.show();
			}

			@Override
			public void onCancel() {
				HashMap<String, String> param = new HashMap<String, String>();
				param.put("facebook", "authorize-cancelled");
				Analytics.logEvent(getPageName(), param);
				
				Log.d(TAG, "facebook authorize cancelled");
			}
    		
    	});
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_FACEBOOK_LOGIN) {
        	 facebook.authorizeCallback(requestCode, resultCode, data);
        }
        else if (requestCode == REQ_SIGN_UP) {
        	if (resultCode == RESULT_OK) {
        		setResult(RESULT_OK);
        		finish();
        	}
        }
    }
    
    public static Action getLogoutAction(Context context, UserContext userContext) {
    	return new MyLogoutAction(context, userContext);
    }
    
    private static class MyLogoutAction implements Action {
    	private Context context;
    	private UserContext userContext;
    	
    	public MyLogoutAction(Context context, UserContext userContext) {
			this.context = context;
			this.userContext = userContext;
		}
    	
    	@Override
		public int getDrawable() {
			return R.drawable.action_bar_logout;
		}

		@Override
		public void performAction(View view) {
			new AlertDialog.Builder(context)
				.setMessage(context.getString(R.string.logout_confirm))
				.setPositiveButton(context.getString(R.string.yes_upper), new OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						userContext.clear();
						context.startActivity(new Intent(context, Profile.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
					}
				})
				.setNegativeButton(context.getString(R.string.no_upper), null)
				.create()
				.show();
		}
    }
    
    
	private class MyLoginTask extends AbstractWebserviceTask {

		private String username;
		private String password;
		
		private boolean loginSuccess;
		
		private ProgressDialog pd;
		
		public MyLoginTask(String username, String password) {
			super(userContext);
			this.username = username;
			this.password = password;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = ProgressDialog.show(context, null, getString(R.string.logging_in));
		}

		@Override
		protected void onSuccess() {
			
			if (loginSuccess) {
				goToProfile();
			}
			else {
				Toast.makeText(Login.this, getString(R.string.log_in_auth_failed), Toast.LENGTH_LONG).show();
			}
		}
		
		@Override
		protected void onFinish() {
			pd.dismiss();
		}

		@Override
		protected void onError(Exception e) {
			
			new AlertDialog.Builder(Login.this)
				.setMessage(getString(R.string.log_in_failed))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.ok_upper), null)
				.create()
				.show();
		}

		@Override
		protected void doWebOperation() throws Exception {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("username", username));
			params.add(new BasicNameValuePair("password", password));
			
			WebserviceResponse response = doPost(DSConstants.API_URL_LOGIN, params);
			
			if (response.getStatusCode() >= 400 && response.getStatusCode() < 500) {
				loginSuccess = false;
			}
			else {
				updateUserContext(response.getBodyAsJSONObject());
				
				loginSuccess = true;
			}
		}
	}
	
	private class MyFacebookLoginTask extends AbstractWebserviceTask {

		private String accessToken;
		
		private boolean loginSuccess;
		
		private ProgressDialog pd;
		
		public MyFacebookLoginTask(String accessToken) {
			super(userContext);
			this.accessToken = accessToken;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = ProgressDialog.show(context, null, getString(R.string.logging_in));
		}

		@Override
		protected void onSuccess() {
			
			if (loginSuccess) {
				HashMap<String, String> param = new HashMap<String, String>();
				param.put("facebook", "login-success");
				Analytics.logEvent(getPageName(), param);
				
				goToProfile();
			}
			else {
				Toast.makeText(Login.this, getString(R.string.log_in_auth_failed), Toast.LENGTH_LONG).show();
			}
		}
		
		@Override
		protected void onFinish() {
			pd.dismiss();
		}

		@Override
		protected void onError(Exception e) {
			
			Toast.makeText(Login.this, getString(R.string.log_in_failed), Toast.LENGTH_LONG).show();
		}

		@Override
		protected void doWebOperation() throws Exception {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("access_token", accessToken));
			
			WebserviceResponse response = doPost(DSConstants.API_URL_FBLOGIN, params);
			
			if (response.getStatusCode() >= 400 && response.getStatusCode() < 500) {
				loginSuccess = false;
			}
			else {
				updateUserContext(response.getBodyAsJSONObject());
				loginSuccess = true;
			}
		}
	}
}