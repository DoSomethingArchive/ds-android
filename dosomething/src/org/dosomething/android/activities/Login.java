package org.dosomething.android.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.Session;
import com.google.inject.Inject;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.analytics.Analytics;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


//public class Login extends AbstractFragmentActivity {
public class Login extends AbstractActionBarActivity {

    private static final int REQ_SIGN_UP = 112;

    private LoginFragment loginFragment;

    @Inject private UserContext userContext;

    private DSFacebookLoginTask fbLoginTask;
    private DSLoginTask dsLoginTask;

    @Override
    protected String getPageName() {
        return "Login";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (savedInstanceState == null) {
            // Add the fragment on initial activity setup
            loginFragment = new LoginFragment();
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, loginFragment).commit();
        }
        else {
            // Or set the fragment from restored state info
            loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
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
     * Execute task for normal DoSomething login with username/email and password
     */
    public void logIn(View v){
        EditText username = (EditText)findViewById(R.id.username);
        EditText password = (EditText)findViewById(R.id.password);

        String user = username.getText().toString();
        String pw = password.getText().toString();

        dsLoginTask = new DSLoginTask(user, pw);
        dsLoginTask.execute();
    }

    /**
     * Get handle to DSLoginTask. Useful for unit tests.
     */
    public AbstractWebserviceTask getDSLoginTask() {
        return dsLoginTask;
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

    public void signUp(View v){
        startActivityForResult(new Intent(this, Register.class), REQ_SIGN_UP);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_SIGN_UP) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    /**
     * Provide Intent for other activities to open this activity
     */
    public static Intent getIntent(Context context) {
        return new Intent(context, Login.class);
    }

    public static void logout(Context context) {
        new AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.logout_confirm))
                .setPositiveButton(context.getString(R.string.yes_upper), new OnLogoutClickListener(context))
                .setNegativeButton(context.getString(R.string.no_upper), null)
                .create()
                .show();
    }

    private static class OnLogoutClickListener implements OnClickListener {
        private Context dialogContext;

        public OnLogoutClickListener(Context ctx) {
            dialogContext = ctx;
        }
        public void onClick(DialogInterface arg0, int arg1) {
            // Close Facebook session and clear token info if any
            Session session = Session.getActiveSession();
            if (session != null) {
                session.closeAndClearTokenInformation();
            }

            new UserContext(dialogContext).clear();
            dialogContext.startActivity(new Intent(dialogContext, Welcome.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
    }

    private class DSLoginTask extends AbstractWebserviceTask {

        private String username;
        private String password;

        private boolean loginSuccess;

        private ProgressDialog pd;

        public DSLoginTask(String username, String password) {
            super(userContext);
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = ProgressDialog.show(Login.this, null, getString(R.string.logging_in));

            // Clear the UserContext of any data that could be lingering (ex: session cookie info)
            userContext.clear();
        }

        @Override
        protected void onSuccess() {

            if (loginSuccess) {
                // Track login with analytics - Flurry Analytics
                HashMap<String, String> param = new HashMap<String, String>();
                param.put("ds-login", "login-success");
                Analytics.logEvent(getPageName(), param);

                // and Google Analytics
                Analytics.logEvent("login", "ds-login", "success");

                goToProfile();
            }
            else {
                Toast.makeText(Login.this, getString(R.string.log_in_auth_failed), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onFinish() {
            try {
                pd.dismiss();
            }
            catch (IllegalArgumentException e) {
                // Catching error if progress dialog is dismissed after activity ends
            }
        }

        @Override
        protected void onError(Exception e) {

            new AlertDialog.Builder(Login.this)
                    .setMessage(getString(R.string.log_in_failed))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok_upper), null)
                    .create()
                    .show();
        }

        @Override
        protected void doWebOperation() throws Exception {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));

            WebserviceResponse response = doPost(DSConstants.API_URL_LOGIN, params);

            if (response.getStatusCode() >= 400 && response.getStatusCode() < 500) {
                loginSuccess = false;
            }
            else {
                updateUserContext(response.getBodyAsJSONObject());

                loginSuccess = true;
            }
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
            pd = ProgressDialog.show(Login.this, null, getString(R.string.logging_in));
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
                Toast.makeText(Login.this, getString(R.string.log_in_auth_failed), Toast.LENGTH_LONG).show();
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
            Toast.makeText(Login.this, getString(R.string.log_in_failed), Toast.LENGTH_LONG).show();
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
}