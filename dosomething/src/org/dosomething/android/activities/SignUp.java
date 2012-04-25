package org.dosomething.android.activities;

import java.util.Date;
import java.util.GregorianCalendar;

import org.dosomething.android.R;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;

public class SignUp extends RoboActivity {
	
	@InjectView(R.id.first_name) private EditText firstName;
	@InjectView(R.id.last_name) private EditText lastName;
	@InjectView(R.id.email) private EditText email;
	@InjectView(R.id.password) private EditText password;
	@InjectView(R.id.birthday) private EditText birthday;
	
	private Date savedBirthday;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        
        birthday.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				new DatePickerDialog(getApplicationContext(), dateListener, 2000, 1, 1).show();
			}
		});
    }
    
    public void signUp(View v){
    	String email = this.email.getText().toString();
    	String password = this.password.getText().toString();
  
    	setResult(RESULT_OK);
    	finish();
    }
    
    private final OnDateSetListener dateListener = new OnDateSetListener() {
		
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			savedBirthday = new GregorianCalendar(year, monthOfYear, dayOfMonth).getTime();
		}
	};
    
    public void cancel(View v){
    	setResult(RESULT_CANCELED);
    	finish();
    }
	
}
