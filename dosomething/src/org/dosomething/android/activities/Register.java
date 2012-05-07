package org.dosomething.android.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.dosomething.android.R;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.dosomething.android.tasks.ErrorResponseCodeException;
import org.dosomething.android.transfer.Campaign;
import org.json.JSONException;
import org.json.JSONObject;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

public class Register extends RoboActivity {
	
	private static final String DATE_FORMAT = "MM/dd/yyyy";
	
	
	@InjectView(R.id.username_name) private EditText username;
	@InjectView(R.id.mobile) private EditText mobile;
	@InjectView(R.id.first_name) private EditText firstName;
	@InjectView(R.id.last_name) private EditText lastName;
	@InjectView(R.id.email) private EditText email;
	@InjectView(R.id.password) private EditText password;
	@InjectView(R.id.birthday) private EditText birthday;
	
	private Date savedBirthday;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        
        birthday.setOnFocusChangeListener(birthdayFocusListener);
        birthday.setOnClickListener(birthdayClickListener);
    }
    
    private final OnClickListener birthdayClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			showBirthdayPicker();
		}
	};
    
    private final OnFocusChangeListener birthdayFocusListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			showBirthdayPicker();
		}
	};
    
    private void showBirthdayPicker(){
    	new DatePickerDialog(this, dateListener, 1995, 0, 1).show();
    }
    
    public void register(View v){
    	String username = this.username.getText().toString();
    	String mobile = this.mobile.getText().toString();
    	String first = this.firstName.getText().toString();
    	String last = this.lastName.getText().toString();
    	String email = this.email.getText().toString();
    	String password = this.password.getText().toString();
    	String birthday = this.birthday.getText().toString();
  
    	new MyTask(username, mobile, first, last, email, password, birthday).execute();
    	
    }
    
    private final OnDateSetListener dateListener = new OnDateSetListener() {
		
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			savedBirthday = new GregorianCalendar(year, monthOfYear, dayOfMonth).getTime();
			birthday.setText(new SimpleDateFormat(DATE_FORMAT).format(savedBirthday));
		}
	};
    
    public void cancel(View v){
    	setResult(RESULT_CANCELED);
    	finish();
    }
	
	private class MyTask extends AbstractWebserviceTask {
		private String username;
		private String mobile;
		private String first;
		private String last;
		private String email;
		private String password;
		private String birthday;
		
		public MyTask(String username, String mobile, String first, String last, String email, String password, String birthday) {
			this.username = username;
			this.first = first;
			this.last = last;
			this.email = email;
			this.password = password;
			this.birthday = birthday;
		}

		@Override
		protected void onSuccess() {
			
			setResult(RESULT_OK);
	    	finish();
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
			String url = "http://www.dosomething.org/?q=rest/user/register.json";
			
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
			
			Map<String,String> params = new HashMap<String, String>();
			params.put("name", username);
			params.put("pass", password);
			params.put("mail", email);
			params.put("profile_main[field_user_birthday][und][0][value][date]", birthday);
			params.put("profile_main[field_user_official_rules][und]", "1"); // must be 1
			params.put("profile_main[field_user_anniversary][und][0][value][date]", dateFormat.format(new Date()));
			params.put("profile_main[field_user_first_name][und][0][value]", first);
			params.put("profile_main[field_user_last_name][und][0][value]", last);
			params.put("profile_main[field_user_mobile][und][0][value]", mobile);
			
			WebserviceResponse response = doPost(url, params);
			
			if(response.getStatusCode()==406) {
				JSONObject errors = response.getBodyAsJSONObject();
				
				
			}
			
				
			//new UserContext(getApplicationContext()).setLoggedIn(user.getLong("login"));
			
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
