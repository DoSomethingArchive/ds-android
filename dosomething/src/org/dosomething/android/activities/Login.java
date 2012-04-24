package org.dosomething.android.activities;

import org.dosomething.android.R;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends RoboActivity {
	
	private static final int REQ_SIGN_UP = 111;
    
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
  
    }
    
    public void signUp(View v){
    	startActivityForResult(new Intent(this, SignUp.class), REQ_SIGN_UP);
    }
    
    public void facebookLogin(View v){
    	Toast.makeText(this, "facebookLogin", Toast.LENGTH_SHORT).show();
    }
    
}