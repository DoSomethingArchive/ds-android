package org.dosomething.android.activities;

import org.dosomething.android.R;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

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

    	goToProfile();
    }
    
    private void goToProfile(){
    	startActivity(new Intent(this, Profile.class));
    	finish();
    }
    
    public void signUp(View v){
    	startActivityForResult(new Intent(this, SignUp.class), REQ_SIGN_UP);
    }
    
    public void facebookLogin(View v){

    	facebook.authorize(this, new String[]{"birthday"}, REQ_FACEBOOK_LOGIN, new DialogListener(){

			@Override
			public void onComplete(Bundle values) {
				Toast.makeText(getApplicationContext(), "complete", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFacebookError(FacebookError e) {
				Toast.makeText(getApplicationContext(), "facebook error", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onError(DialogError e) {
				Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onCancel() {
				Toast.makeText(getApplicationContext(), "cancel", Toast.LENGTH_SHORT).show();
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
        		goToProfile();
        	}
        }
        
       
    }
    
}