package org.dosomething.android.activities;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.dosomething.android.R;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.dosomething.android.transfer.Campaign;
import org.json.JSONException;
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
	
	@InjectView(R.id.email) private EditText email;
	@InjectView(R.id.password) private EditText password;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
    }
    
    public void logIn(View v){
    	String email = this.email.getText().toString();
    	String password = this.password.getText().toString();
    	
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
		
		public MyTask(String username, String password) {
			this.username = username;
			this.password = password;
		}

		@Override
		protected void onSuccess() {
			
			
			
			goToProfile();
		}
		
		@Override
		protected void onFinish() {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void onError() {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void doWebOperation() throws Exception {
			String url = "http://www.dosomething.org/?q=rest/user/login.json";
			
			Map<String,String> params = new HashMap<String, String>();
			params.put("username", username);
			params.put("password", password);
			
			JSONObject user = doPost(url, params).getBodyAsJSONObject().getJSONObject("user");
			
			new UserContext(getApplicationContext()).setLoggedIn(user.getLong("login"));
			
		}
		
		private void toastError(final String message) {
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
				}
			});
		}

		private Campaign convert(JSONObject object) throws JSONException, ParseException {
			
			return new Campaign(object);
		}
		
	}
    
}