package org.dosomething.android.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.activities.AbstractActionBarActivity;
import org.dosomething.android.activities.Profile;
import org.dosomething.android.analytics.Analytics;
import org.dosomething.android.context.UserContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DSFacebookLogin {

    private static DSFacebookLoginTask fbLoginTask;

    /**
     * After logging in with Facebook, execute login to DoSomething backend with
     * the given Facebook access token.
     *
     * @param accessToken Access token provided by Facebook Graph API
     */
    public static void execute(Context context, String accessToken) {
        if (fbLoginTask == null) {
            fbLoginTask = new DSFacebookLoginTask(context);
            fbLoginTask.executeWithToken(accessToken);
        }
        // If a FB login task was already create, don't execute another one unless
        // it's already finished executing its previous task.
        else if (fbLoginTask.getStatus() == AsyncTask.Status.FINISHED) {
            fbLoginTask.executeWithToken(accessToken);
        }
    }

    private static void goToProfile(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity)context;
            activity.startActivity(new Intent(context, Profile.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            activity.finish();
        }
    }

    /**
     * TODO
     */
    private static class DSFacebookLoginTask extends AbstractWebserviceTask {

        private String accessToken;

        private boolean loginSuccess;

        private ProgressDialog pd;

        private Context context;

        public DSFacebookLoginTask(Context ctx) {
            super(new UserContext(ctx));

            context = ctx;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = ProgressDialog.show(context, null, context.getString(R.string.logging_in));
        }

        public void executeWithToken(String accessToken) {
            this.accessToken = accessToken;
            this.execute();
        }

        @Override
        protected void onSuccess() {
            if (loginSuccess) {
                String pageName = "Login/Register";
                if (context instanceof AbstractActionBarActivity) {
                    pageName = ((AbstractActionBarActivity)context).getPageName();
                }

                // Track login with analytics - Flurry Analytics
                HashMap<String, String> param = new HashMap<String, String>();
                param.put("facebook", "login-success");
                Analytics.logEvent(pageName, param);

                // and Google Analytics
                Analytics.logEvent(pageName, "facebook-login", "success");

                goToProfile(context);
            }
            else {
                Toast.makeText(context, context.getString(R.string.log_in_auth_failed), Toast.LENGTH_LONG).show();
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
            Toast.makeText(context, context.getString(R.string.log_in_failed), Toast.LENGTH_LONG).show();
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
                UserContext userContext = new UserContext(context);
                userContext.updateWithUserObject(response.getBodyAsJSONObject());
                loginSuccess = true;
            }
        }
    }
}
