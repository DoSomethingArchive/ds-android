package org.dosomething.android.activities;

import org.dosomething.android.R;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SignUp extends RoboActivity {
	
	@InjectView(R.id.first_name) private EditText firstName;
	@InjectView(R.id.last_name) private EditText lastName;
	@InjectView(R.id.email) private EditText email;
	@InjectView(R.id.password) private EditText password;
	@InjectView(R.id.birthday) private EditText birthday;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
    }
    
    public void signUp(View v){
    	String email = this.email.getText().toString();
    	String password = this.password.getText().toString();
  
    	Toast.makeText(this, email, Toast.LENGTH_SHORT).show();
    }
    
    public void cancel(View v){
    	setResult(RESULT_CANCELED);
    	finish();
    }
	
}
