package org.dosomething.android.activities;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.dosomething.android.R;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.json.JSONObject;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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

public class Login extends RoboActivity {
	
	private static final String TAG = "Login";
	private static final int REQ_FACEBOOK_LOGIN = 111;
	private static final int REQ_SIGN_UP = 112;
	
    
	private Facebook facebook = new Facebook("105775762330");
	
	@InjectView(R.id.username) private EditText username;
	@InjectView(R.id.password) private EditText password;
	
	@Inject private UserContext userContext;
	
	private Context context;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        context = this;
    }
    
    public void logIn(View v){
    	String email = this.username.getText().toString();
    	String password = this.password.getText().toString();
    	
    	//goToProfile();
    	
    	new MyLoginTask(email, password).execute();
    }
    
    private void goToProfile(){
    	startActivity(new Intent(this, Profile.class));
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
				Log.d(TAG, "facebook authorize cancelled");
			}
    		
    	});
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQ_FACEBOOK_LOGIN){
        	 facebook.authorizeCallback(requestCode, resultCode, data);
        }else if(requestCode == REQ_SIGN_UP){
        	if(resultCode == RESULT_OK){
        		setResult(RESULT_OK);
        		finish();
        	}
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
			
			if(loginSuccess) {
				goToProfile();
			} else {
				Toast.makeText(Login.this, getString(R.string.log_in_auth_failed), Toast.LENGTH_LONG).show();
			}
		}
		
		@Override
		protected void onFinish() {
			pd.dismiss();
		}

		@Override
		protected void onError() {
			
			new AlertDialog.Builder(Login.this)
				.setMessage(getString(R.string.facebook_auth_failed))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.ok_upper), null)
				.create()
				.show();
		}

		@Override
		protected void doWebOperation() throws Exception {
			String url = "http://www.dosomething.org/?q=rest/user/login.json";
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("username", username));
			params.add(new BasicNameValuePair("password", password));
			
			WebserviceResponse response = doPost(url, params);
			
			if(response.getStatusCode()>=400 && response.getStatusCode()<500) {
				loginSuccess = false;
			} else {
				JSONObject obj = response.getBodyAsJSONObject();
				JSONObject user = obj.getJSONObject("user");
				
				userContext.setLoggedIn(user.getString("name"), user.getString("uid"), obj.getString("sessid"), obj.getString("session_name"), obj.getLong("session_cache_expire"));
				
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
			
			if(loginSuccess) {
				goToProfile();
			} else {
				Toast.makeText(Login.this, getString(R.string.log_in_auth_failed), Toast.LENGTH_LONG).show();
			}
		}
		
		@Override
		protected void onFinish() {
			pd.dismiss();
		}

		@Override
		protected void onError() {
			
			Toast.makeText(Login.this, getString(R.string.log_in_failed), Toast.LENGTH_LONG).show();
		}

		@Override
		protected void doWebOperation() throws Exception {
			String url = "http://www.dosomething.org/?q=rest/user/fblogin.json";
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("access_token", accessToken));
			
			WebserviceResponse response = doPost(url, params);
			
			if(response.getStatusCode()>=400 && response.getStatusCode()<500) {
				loginSuccess = false;
			} else {
				
				JSONObject obj = response.getBodyAsJSONObject();
				JSONObject user = obj.getJSONObject("user");
				
				userContext.setLoggedIn(user.getString("name"), user.getString("uid"), obj.getString("sessid"), obj.getString("session_name"), obj.getLong("session_cache_expire"));
				
				loginSuccess = true;
			}
		}
	}
}