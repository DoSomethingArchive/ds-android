package org.dosomething.android.activities;

import java.util.Date;
import java.util.GregorianCalendar;

import org.dosomething.android.R;
import org.dosomething.android.widgets.ActionBar;

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
        
        birthday.setOnFocusChangeListener(birthdayFocusListener);
    }
    
    private final OnFocusChangeListener birthdayFocusListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			showBirthdayPicker();
		}
	};
    
    private void showBirthdayPicker(){
    	new DatePickerDialog(this, dateListener, 1995, 0, 1).show();
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
