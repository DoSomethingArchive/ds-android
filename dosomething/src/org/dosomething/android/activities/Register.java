package org.dosomething.android.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.dosomething.android.R;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.json.JSONObject;

import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class Register extends AbstractActivity {
	
	private static final String DATE_FORMAT = "MM/dd/yyyy";
	
	@Inject private UserContext userContext;
	@Inject @Named("DINComp-CondBold")Typeface headerTypeface;
	
	@InjectView(R.id.actionbar) private CustomActionBar actionBar;
	@InjectView(R.id.username_name) private EditText username;
	@InjectView(R.id.mobile) private EditText mobile;
	@InjectView(R.id.first_name) private EditText firstName;
	@InjectView(R.id.last_name) private EditText lastName;
	@InjectView(R.id.email) private EditText email;
	@InjectView(R.id.password) private EditText password;
	@InjectView(R.id.confirm_password) private EditText confirmPassword;
	@InjectView(R.id.birthday) private EditText birthday;
	@InjectView(R.id.cancel) private Button cancel;
	@InjectView(R.id.submit) private Button submit;
	
	private Date savedBirthday;
	
	private Context context;
	
	@Override
	protected String getPageName() {
		return "register";
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        
        context = this;
        
        actionBar.setHomeAction(Campaigns.getHomeAction(context));
        
        birthday.setOnFocusChangeListener(birthdayFocusListener);
        birthday.setOnClickListener(birthdayClickListener);
        
        cancel.setTypeface(headerTypeface, Typeface.BOLD);
        submit.setTypeface(headerTypeface, Typeface.BOLD);
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
    	String confirmPassword = this.confirmPassword.getText().toString();
    	String birthday = this.birthday.getText().toString();
    	
    	if(!password.equals(confirmPassword)) {
    		new AlertDialog.Builder(Register.this)
				.setMessage(getString(R.string.confirm_password_failed))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.ok_upper), null)
				.create()
				.show();
    	} else {
    		new MyTask(username, mobile, first, last, email, password, birthday).execute();
    	}
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
		
		private boolean registerSuccess;
		private String validationMessage;
		
		private ProgressDialog pd;
		
		public MyTask(String username, String mobile, String first, String last, String email, String password, String birthday) {
			super(userContext);
			this.username = username;
			this.first = first;
			this.last = last;
			this.email = email;
			this.password = password;
			this.birthday = birthday;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = ProgressDialog.show(context, null, getString(R.string.registering));
		}

		@Override
		protected void onSuccess() {
			
			if(registerSuccess) {
				setResult(RESULT_OK);
		    	finish();
			} else {
				new AlertDialog.Builder(Register.this)
					.setMessage(validationMessage)
					.setCancelable(false)
					.setPositiveButton(getString(R.string.ok_upper), null)
					.create()
					.show();
			}
			
		}
		
		@Override
		protected void onFinish() {
			pd.dismiss();
		}

		@Override
		protected void onError() {
			Toast.makeText(Register.this, getString(R.string.register_failed), Toast.LENGTH_LONG).show();
		}

		@Override
		protected void doWebOperation() throws Exception {
			String url = "http://www.dosomething.org/?q=rest/user/register.json";
			
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("name", username));
			params.add(new BasicNameValuePair("pass", password));
			params.add(new BasicNameValuePair("mail", email));
			params.add(new BasicNameValuePair("profile_main[field_user_birthday][und][0][value][date]", birthday));
			params.add(new BasicNameValuePair("profile_main[field_user_official_rules][und]", "1")); // must be 1
			params.add(new BasicNameValuePair("profile_main[field_user_anniversary][und][0][value][date]", dateFormat.format(new Date())));
			params.add(new BasicNameValuePair("profile_main[field_user_first_name][und][0][value]", first));
			params.add(new BasicNameValuePair("profile_main[field_user_last_name][und][0][value]", last));
			params.add(new BasicNameValuePair("profile_main[field_user_mobile][und][0][value]", mobile));
			
			WebserviceResponse response = doPost(url, params);
			
			if(response.getStatusCode()>=400 && response.getStatusCode()<500) {
				
				validationMessage = response.extractFormErrorsAsMessage();
				if(validationMessage==null) {
					getString(R.string.auth_failed);
				}
				registerSuccess = false;
			} else {
				JSONObject obj = response.getBodyAsJSONObject();
				JSONObject user = obj.getJSONObject("user");
				
				userContext.setLoggedIn(username, user.getString("mail"), user.getString("uid"), obj.getString("sessid"), obj.getString("session_name"), obj.getLong("session_cache_expire"));
				
				registerSuccess = true;
			}
		}
		
	}
}
