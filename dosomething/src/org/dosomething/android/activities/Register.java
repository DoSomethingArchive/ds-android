package org.dosomething.android.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.analytics.Analytics;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.fragments.RegisterFragment;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Register extends AbstractActionBarActivity {
	
	@Inject private UserContext userContext;
	@Inject @Named("DINComp-CondBold")Typeface headerTypeface;
	
	public AbstractWebserviceTask registerTask;
    private DSFacebookLoginTask fbLoginTask;
	
	@Override
	protected String getPageName() {
		return "register";
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        RegisterFragment registerFragment;
        if (savedInstanceState == null) {
            // Add the fragment on initial activity setup
            registerFragment = new RegisterFragment();
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, registerFragment).commit();
        }
        else {
            // Or set the fragment from restored state info
            registerFragment = (RegisterFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If home button is selected on ActionBar, then end the activity
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Execute task to register user with the information provided.
     */
    public void register() {
    	String mobile = ((EditText)findViewById(R.id.mobile)).getText().toString();
    	String first = ((EditText)findViewById(R.id.first_name)).getText().toString();
    	String email = ((EditText)findViewById(R.id.email)).getText().toString();
    	String password = ((EditText)findViewById(R.id.password)).getText().toString();
    	String confirmPassword = ((EditText)findViewById(R.id.confirm_password)).getText().toString();
    	String birthday = ((EditText)findViewById(R.id.birthday)).getText().toString();
    	
    	if(!password.equals(confirmPassword)) {
    		new AlertDialog.Builder(Register.this)
				.setMessage(getString(R.string.confirm_password_failed))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.ok_upper), null)
				.create()
				.show();
    	} else {
    		registerTask = new RegisterTask(mobile, first, email, password, birthday);
    		registerTask.execute();
    	}
    }
    
    public void cancel(View v){
    	setResult(RESULT_CANCELED);
    	finish();
    }
	
	private class RegisterTask extends AbstractWebserviceTask {
		private String mobile;
		private String first;
		private String email;
		private String password;
		private String birthday;
		
		private boolean registerSuccess;
		private String validationMessage;
		
		private ProgressDialog pd;
		
		public RegisterTask(String mobile, String first, String email, String password, String birthday) {
			super(userContext);
            this.mobile = mobile;
			this.first = first;
			this.email = email;
			this.password = password;
			this.birthday = birthday;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = ProgressDialog.show(Register.this, null, getString(R.string.registering));
		}

		@Override
		protected void onSuccess() {
			
			if(registerSuccess) {
				// Track register in analytics - Flurry Analytics event tracking
				HashMap<String, String> param = new HashMap<String, String>();
				param.put("success", "true");
				Analytics.logEvent(getPageName(), param);
				
				// and Google Analytics
				Analytics.logEvent("login", "register", "success");
				
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
		protected void onError(Exception e) {
			Toast.makeText(Register.this, getString(R.string.register_failed), Toast.LENGTH_LONG).show();
		}

		@Override
		protected void doWebOperation() throws Exception {
			SimpleDateFormat dateFormat = new SimpleDateFormat(DSConstants.DATE_FORMAT, Locale.US);
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("pass", password));
			params.add(new BasicNameValuePair("mail", email));
			params.add(new BasicNameValuePair("profile_main[field_user_birthday][und][0][value][date]", birthday));
			params.add(new BasicNameValuePair("profile_main[field_user_official_rules][und]", "1")); // must be 1
			params.add(new BasicNameValuePair("profile_main[field_user_anniversary][und][0][value][date]", dateFormat.format(new Date())));
			params.add(new BasicNameValuePair("profile_main[field_user_first_name][und][0][value]", first));
			params.add(new BasicNameValuePair("profile_main[field_user_mobile][und][0][value]", mobile));
			
			WebserviceResponse response = doPost(DSConstants.API_URL_USER_REGISTER, params);
			
			if(response.getStatusCode()>=400 && response.getStatusCode()<500) {
				
				validationMessage = response.extractFormErrorsAsMessage();
				if(validationMessage==null) {
					getString(R.string.auth_failed);
				}
				registerSuccess = false;
			} else {
				JSONObject obj = response.getBodyAsJSONObject();
				JSONObject user = obj.getJSONObject("user");
				
				userContext.setLoggedIn("", user.getString("mail"), user.getString("uid"), obj.getString("sessid"), obj.getString("session_name"), obj.getLong("session_cache_expire"));
				
				registerSuccess = true;
			}
		}
	}

    /**
     * After logging in with Facebook, execute login to DoSomething backend with
     * the given Facebook access token.
     *
     * @param accessToken Access token provided by Facebook Graph API
     */
    public void dsFacebookLogin(String accessToken) {
        if (fbLoginTask == null) {
            fbLoginTask = new DSFacebookLoginTask();
            fbLoginTask.executeWithToken(accessToken);
        }
        // If a FB login task was already create, don't execute another one unless
        // it's already finished executing its previous task.
        else if (fbLoginTask.getStatus() == AsyncTask.Status.FINISHED) {
            fbLoginTask.executeWithToken(accessToken);
        }
    }

    private class DSFacebookLoginTask extends AbstractWebserviceTask {

        private String accessToken;

        private boolean loginSuccess;

        private ProgressDialog pd;

        public DSFacebookLoginTask() {
            super(userContext);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = ProgressDialog.show(Register.this, null, getString(R.string.logging_in));
        }

        public void executeWithToken(String accessToken) {
            this.accessToken = accessToken;
            this.execute();
        }

        @Override
        protected void onSuccess() {
            if (loginSuccess) {
                // Track login with analytics - Flurry Analytics
                HashMap<String, String> param = new HashMap<String, String>();
                param.put("facebook", "login-success");
                Analytics.logEvent(getPageName(), param);

                // and Google Analytics
                Analytics.logEvent("login", "facebook-login", "success");

                goToProfile();
            }
            else {
                Toast.makeText(Register.this, getString(R.string.log_in_auth_failed), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onFinish() {
            if (pd != null && pd.isShowing()) {
                try {
                    pd.dismiss();
                    pd = null;
                }
                catch (IllegalArgumentException e) {
                    // Catching error if progress dialog is dismissed after activity ends
                    //Log.w(getPageName(), "Dismissing progress dialog after Login activity ended");
                }
            }
        }

        @Override
        protected void onError(Exception e) {
            Toast.makeText(Register.this, getString(R.string.log_in_failed), Toast.LENGTH_LONG).show();
        }

        @Override
        protected void doWebOperation() throws Exception {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("access_token", accessToken));

            WebserviceResponse response = doPost(DSConstants.API_URL_FBLOGIN, params);

            if (response.getStatusCode() >= 400 && response.getStatusCode() < 500) {
                loginSuccess = false;
            }
            else {
                updateUserContext(response.getBodyAsJSONObject());
                loginSuccess = true;
            }
        }
    }

    /**
     * After a login succeeds, takes json object returned and updates the user
     * context with the contained info.
     *
     * @param obj JSONObject of user and profile data
     */
    private void updateUserContext(JSONObject obj) throws Exception {
        JSONObject user = obj.getJSONObject("user");

        if (user != null && obj != null) {
            userContext.setLoggedIn(
                    user.optString("name", ""),
                    user.optString("mail", ""),
                    user.getString("uid"),
                    obj.getString("sessid"),
                    obj.getString("session_name"),
                    obj.getLong("session_cache_expire"));

            userContext.setCreatedTime(user.getString("created"));
        }

        JSONObject profile = obj.optJSONObject("profile");
        if (profile != null) {

            String firstName;
            if (profile.optJSONObject("field_user_first_name") != null
                    && profile.optJSONObject("field_user_first_name").optJSONArray("und") != null
                    && profile.optJSONObject("field_user_first_name").optJSONArray("und").optJSONObject(0) != null
                    && (firstName = profile.optJSONObject("field_user_first_name").optJSONArray("und").getJSONObject(0).optString("value", null)) != null)
            {
                userContext.setFirstName(firstName);
            }

            String lastName;
            if (profile.optJSONObject("field_user_last_name") != null
                    && profile.optJSONObject("field_user_last_name").optJSONArray("und") != null
                    && profile.optJSONObject("field_user_last_name").optJSONArray("und").optJSONObject(0) != null
                    && (lastName = profile.optJSONObject("field_user_last_name").optJSONArray("und").optJSONObject(0).optString("value", null)) != null)
            {
                userContext.setLastName(lastName);
            }


            JSONObject address;
            if (profile.optJSONObject("field_user_address") != null
                    && profile.optJSONObject("field_user_address").optJSONArray("und") != null
                    && (address = profile.optJSONObject("field_user_address").optJSONArray("und").optJSONObject(0)) != null)
            {
                String addr1 = address.optString("thoroughfare");
                String addr2 = address.optString("premise");
                String city = address.optString("locality");
                String state = address.optString("administrative_area");
                String zip = address.optString("postal_code");

                userContext.setAddr1(addr1);
                userContext.setAddr2(addr2);
                userContext.setAddrCity(city);
                userContext.setAddrState(state);
                userContext.setAddrZip(zip);
            }
        }
    }

    private void goToProfile(){
        startActivity(new Intent(this, Profile.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }
}
