package org.dosomething.android.activities;

import java.util.HashMap;
import java.util.Map;

import org.dosomething.android.R;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.json.JSONObject;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.android.Facebook;

public class Login extends RoboActivity {
	
	private static final int REQ_SIGN_UP = 111;
	private static final int REQ_FACEBOOK_LOGIN = 111;
    
	private Facebook facebook = new Facebook("105775762330");
	
	@InjectView(R.id.username) private EditText username;
	@InjectView(R.id.password) private EditText password;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
    }
    
    public void logIn(View v){
    	String email = this.username.getText().toString();
    	String password = this.password.getText().toString();
    	
    	//goToProfile();
    	
    	new MyTask(email, password).execute();
    }
    
    private void goToProfile(){
    	startActivity(new Intent(this, Profile.class));
    	finish();
    }
    
    public void signUp(View v){
    	startActivityForResult(new Intent(this, Register.class), REQ_SIGN_UP);
    }
    
    public void facebookLogin(View v){

//    	facebook.authorize(this, new String[]{"birthday"}, REQ_FACEBOOK_LOGIN, new DialogListener(){
//
//			@Override
//			public void onComplete(Bundle values) {
//				Toast.makeText(getApplicationContext(), "complete", Toast.LENGTH_SHORT).show();
//			}
//
//			@Override
//			public void onFacebookError(FacebookError e) {
//				Toast.makeText(getApplicationContext(), "facebook error", Toast.LENGTH_SHORT).show();
//			}
//
//			@Override
//			public void onError(DialogError e) {
//				Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
//			}
//
//			@Override
//			public void onCancel() {
//				Toast.makeText(getApplicationContext(), "cancel", Toast.LENGTH_SHORT).show();
//			}
//    		
//    	});
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
    
	private class MyTask extends AbstractWebserviceTask {

		private String username;
		private String password;
		
		private boolean loginSuccess;
		
		public MyTask(String username, String password) {
			this.username = username;
			this.password = password;
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
		protected void onFinish() { /*ignore */	}

		@Override
		protected void onError() {
			
			Toast.makeText(Login.this, getString(R.string.log_in_failed), Toast.LENGTH_LONG).show();
		}

		@Override
		protected void doWebOperation() throws Exception {
			String url = "http://www.dosomething.org/?q=rest/user/login.json";
			
			Map<String,String> params = new HashMap<String, String>();
			params.put("username", username);
			params.put("password", password);
			
			WebserviceResponse response = doPost(url, params);
			
			if(response.getStatusCode()>=400 && response.getStatusCode()<500) {
				loginSuccess = false;
			} else {
				JSONObject obj = response.getBodyAsJSONObject();
				JSONObject user = obj.getJSONObject("user");
				String uid = user.getString("uid");
				
				new UserContext(getApplicationContext()).setLoggedIn(uid);
				loginSuccess = true;
			}
		}
	}
}