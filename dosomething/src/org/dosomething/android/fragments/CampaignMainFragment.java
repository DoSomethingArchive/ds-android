package org.dosomething.android.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.activities.CampaignSMSRefer;
import org.dosomething.android.activities.Login;
import org.dosomething.android.activities.SignUp;
import org.dosomething.android.analytics.Analytics;
import org.dosomething.android.cache.Cache;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.dao.DSDao;
import org.dosomething.android.domain.UserCampaign;
import org.dosomething.android.tasks.AbstractWebserviceTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * First sub-page/fragment presented to user after selecting a campaign.
 */
public class CampaignMainFragment extends AbstractCampaignFragment implements View.OnClickListener {

    private static final String CAMPAIGN = DSConstants.EXTRAS_KEY.CAMPAIGN.getValue();
    private static final String CAMPAIGN_ID = "campaign-id";
    private static final String SMS_REFER = "sms-refer";

    private static final int REQ_LOGIN_FOR_SIGN_UP = 111;
    private static final int SMS_REFER_ACTIVITY = 112;

    @Inject private ImageLoader imageLoader;
    @Inject private UserContext userContext;
    @Inject private Cache cache;
    @Inject @Named("DINComp-CondBold")Typeface headerTypeface;

    private ImageView imgLogo;
    private ImageView imgBackground;
    private LinearLayout llImageContainer;
    private TextView txtDates;
    private TextView txtTeaser;
    private Button btnSignUp;
    private FrameLayout frmVideo;
    private ImageView imgVideoThumb;
    private ImageView imgThumb;
    private LinearLayout llSMSReferContainer;
    private TextView txtSMSRefer;
    private Button btnSMSRefer;
    private Button btnUnsign;

    private org.dosomething.android.transfer.Campaign campaign;

    @Override
    public String getFragmentName() {
        if (campaign != null) {
            return campaign.getName();
        }
        else {
            return "Campaign-Main";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.campaign_main, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgLogo = (ImageView)view.findViewById(R.id.image);
        imgBackground = (ImageView)view.findViewById(R.id.background);
        llImageContainer = (LinearLayout)view.findViewById(R.id.image_container);
        txtDates = (TextView)view.findViewById(R.id.dates);
        txtTeaser = (TextView)view.findViewById(R.id.teaser);
        btnSignUp = (Button)view.findViewById(R.id.sign_up);
        frmVideo = (FrameLayout)view.findViewById(R.id.frmVideo);
        imgVideoThumb = (ImageView)view.findViewById(R.id.imgVideoThumb);
        imgThumb = (ImageView)view.findViewById(R.id.imgThumb);
        llSMSReferContainer = (LinearLayout)view.findViewById(R.id.sms_refer_container);
        txtSMSRefer = (TextView)view.findViewById(R.id.sms_refer_text);
        btnSMSRefer = (Button)view.findViewById(R.id.sms_refer);
        btnUnsign = (Button)view.findViewById(R.id.unsign_up);

        Bundle args = getArguments();
        campaign = (org.dosomething.android.transfer.Campaign)args.getSerializable(CAMPAIGN);

        // Setup click listener for buttons
        btnSignUp.setOnClickListener(this);
        btnSMSRefer.setOnClickListener(this);
        btnUnsign.setOnClickListener(this);

        populateFields();
    }

    @Override
    public void onResume() {
        super.onResume();

        updateSignUpButton(getActivity());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_up:
                signUp();
                break;
            case R.id.sms_refer:
                startActivityForResult(CampaignSMSRefer.getIntent(getActivity(), campaign), SMS_REFER_ACTIVITY);
                break;
            case R.id.unsign_up:
                removeSignUp();
                break;
        }
    }

    private void populateFields() {
        SimpleDateFormat mf = new SimpleDateFormat("MMM d", Locale.US);
        txtDates.setText(getString(R.string.campaign_date_ends, mf.format(campaign.getEndDate())));
        txtDates.setTypeface(headerTypeface);

        txtTeaser.setText(campaign.getTeaser());

        llImageContainer.setBackgroundColor(Color.parseColor(campaign.getBackgroundColor()));
        if(!nullOrEmpty(campaign.getBackgroundUrl())) {
            imageLoader.displayImage(campaign.getBackgroundUrl(), imgBackground);
        }
        imageLoader.displayImage(campaign.getLogoUrl(), imgLogo);

        btnSignUp.setTypeface(headerTypeface, Typeface.BOLD);

        if (!nullOrEmpty(campaign.getVideoThumbnail()) && !nullOrEmpty(campaign.getVideoUrl())) {
            imageLoader.displayImage(campaign.getVideoThumbnail(), imgVideoThumb);
            frmVideo.setVisibility(ImageView.VISIBLE);
        }
        else if (!nullOrEmpty(campaign.getImage())) {
            imageLoader.displayImage(campaign.getImage(), imgThumb);
            imgThumb.setVisibility(ImageView.VISIBLE);
        }

        btnSMSRefer.setTypeface(headerTypeface, Typeface.BOLD);
        if (!nullOrEmpty(campaign.getSMSReferText())) {
            llSMSReferContainer.setVisibility(LinearLayout.VISIBLE);
            txtSMSRefer.setText(campaign.getSMSReferText());
        }
    }

    private boolean useAlternateSignUp() {
        if (campaign != null) {
            boolean hasAltText = campaign.getSignUpAltText() != null && campaign.getSignUpAltText().length() > 0;
            boolean hasAltLink = campaign.getSignUpAltLink() != null && campaign.getSignUpAltLink().length() > 0;

            if (hasAltText && hasAltLink) {
                return true;
            }
        }

        return false;
    }

    private boolean useSmsActionSignUp() {
        if (campaign != null) {
            boolean hasAltText = campaign.getSignUpAltText() != null && campaign.getSignUpAltText().length() > 0;
            boolean hasSmsAction = campaign.getSignUpSmsAction() != null && campaign.getSignUpSmsAction().length() > 0;

            if (hasAltText && hasSmsAction) {
                return true;
            }
        }

        return false;
    }

    /**
     * Execute the sign up action for this campaign.
     */
    public void signUp() {
        Activity activity = getActivity();
        String uid = new UserContext(activity).getUserUid();

        // For SMS campaigns, opt the user in through the Mobile Commons opt-in path
        if (useSmsActionSignUp()) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
            alertBuilder.setTitle(R.string.campaign_sign_up_sms_action_alert_title)
                    .setMessage(getString(R.string.campaign_sign_up_sms_action_alert_body, campaign.getName()))
                    .setNegativeButton(R.string.cancel_upper, null)
                    .setPositiveButton(R.string.ok_upper, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Join the user into the Mobile Commons opt-in path
                            new SMSMobileCommonsOptInTask(getActivity(), campaign.getSignUpSmsOptIn()).execute();
                        }
                    })
                    .create()
                    .show();
        }
        // Non-null UID indicates the user is signed in
        else if (uid != null) {
            // If we need additional sign up information for this campaign, launch the Sign Up activity
            if (!hasNoSignUp()) {
                startActivity(SignUp.getIntent(activity, campaign));
            }
            // Otherwise, notify app and server that user signed up for this campaign
            else {
                // Save on the app-side that the user signed up for this campaign
                UserCampaign uc = new UserCampaign.UserCampaignCVBuilder()
                        .campaignId(campaign.getId())
                        .uid(uid)
                        .campaignName(campaign.getName())
                        .dateEnds(campaign.getEndDate().getTime() / 1000) // times needs to be in seconds
                        .dateSignedUp(Calendar.getInstance().getTimeInMillis() / 1000)
                        .dateStarts(campaign.getStartDate().getTime() / 1000)
                        .build();
                new DSDao(getActivity()).setSignedUp(uc);

                // Update the sign up button's look
                updateSignUpButton(getActivity());

                // Display a Toast message for success
                Toast.makeText(getActivity(), getString(R.string.campaign_sign_up_success), Toast.LENGTH_LONG).show();

                // Update the ActionBar tabs on the Activity
                if (activity instanceof org.dosomething.android.activities.Campaign) {
                    org.dosomething.android.activities.Campaign campActivity = (org.dosomething.android.activities.Campaign)activity;

                    // Enable the rest of the tabs
                    campActivity.refreshActionBarTabs();

                    // Change to the next tab
                    campActivity.setCurrentTab(1);
                }

                // TODO: notify server that user signed up
            }
        }
        // If the user is not logged in somehow, send to the Login activity
        else {
            startActivityForResult(new Intent(activity, Login.class), REQ_LOGIN_FOR_SIGN_UP);
        }
    }

    private boolean hasNoSignUp() {
        if (campaign != null
                && campaign.getSignUp() == null
                && (campaign.getSignUpAltLink() == null || campaign.getSignUpAltLink().length() == 0)
                && (campaign.getSignUpAltText() == null || campaign.getSignUpAltText().length() == 0)
                && (campaign.getSignUpSmsAction() == null || campaign.getSignUpSmsAction().length() == 0)
                && campaign.getSignUpSmsOptIn() == 0)
        {
            return true;
        }
        else
            return false;
    }

    /**
     * Removes the user's sign up for this campaign. Mainly inteded for development use.
     */
    private void removeSignUp() {
        Activity activity = getActivity();
        int removedRows = new DSDao(activity).removeSignUp(
                new UserContext(activity).getUserUid(), campaign.getId());

        if (removedRows > 0) {
            Toast.makeText(activity, "Removed sign up for the campaign", Toast.LENGTH_LONG).show();
            updateSignUpButton(activity);

            if (activity instanceof org.dosomething.android.activities.Campaign) {
                ((org.dosomething.android.activities.Campaign)activity).refreshActionBarTabs();
            }
        }
        else {
            Toast.makeText(activity, "Unable to remove sign up for this campaign", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Update look and behavior of the sign up button based on the type of campaign and the
     * user's campaign status.
     *
     * @param context Activity context
     */
    private void updateSignUpButton(Context context) {
        btnUnsign.setVisibility(View.GONE);

        if (useAlternateSignUp() || useSmsActionSignUp()) {
            btnSignUp.setEnabled(true);
            btnSignUp.setText(campaign.getSignUpAltText());
        }
        else {
            boolean isSignedUp = new DSDao(context).isSignedUpForCampaign(userContext.getUserUid(), campaign.getId());
            if (isSignedUp) {
                btnSignUp.setEnabled(false);
                btnSignUp.setText(R.string.campaign_sign_up_button_already_signed_up);

                // Show a remove sign up button for dev builds
                if (!DSConstants.inProduction) {
                    btnUnsign.setVisibility(View.VISIBLE);
                }
            }
            else {
                btnSignUp.setEnabled(true);
                btnSignUp.setText(R.string.campaign_sign_up_button);
            }
        }
    }

    /**
     * Webservice task to join the user into the specified Mobile Commons opt-in path
     */
    private class SMSMobileCommonsOptInTask extends AbstractWebserviceTask {

        private Context context;
        private int optInPath;
        private boolean webOpSuccess;

        public SMSMobileCommonsOptInTask(Context context, int optInPath) {
            super(userContext);
            this.context = context;
            this.optInPath = optInPath;
        }

        @Override
        protected void onSuccess() {
            if (webOpSuccess) {
                // Log the SMS sign up event to Flurry Analytics
                HashMap<String, String> param = new HashMap<String, String>();
                param.put(CAMPAIGN, campaign.getName());
                Analytics.logEvent("sign-up-submit", param);

                // and Google Analytics
                Analytics.logEvent("sign-up", "sms-sign-up", campaign.getName());

                // Display a Toast message
                Toast.makeText(context, getString(R.string.campaign_sign_up_sms_success), Toast.LENGTH_LONG).show();

                // Increment the counter of times an SMS experience was started
                userContext.addSmsCampaignsStarted();
            }
            else {
                onError(null);
            }
        }

        @Override
        protected void onFinish() {
        }

        @Override
        protected void onError(Exception e) {
            String errorMessage = getString(R.string.campaign_sign_up_sms_error);
            // If a keyword is available, offer that option in the error message
            if (!nullOrEmpty(campaign.getSignUpSmsAction())) {
                errorMessage = getString(R.string.campaign_sign_up_sms_error_with_keyword, campaign.getSignUpSmsAction());
            }

            new AlertDialog.Builder(getActivity())
                    .setMessage(errorMessage)
                    .setPositiveButton(getString(R.string.ok_upper), null)
                    .create()
                    .show();
        }

        @Override
        protected void doWebOperation() throws Exception {
            webOpSuccess = false;
            String phoneNumber = userContext.getPhoneNumber();
            if (phoneNumber != null && optInPath > 0) {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("person[phone]", phoneNumber));
                params.add(new BasicNameValuePair("opt_in_path", Integer.toString(optInPath)));

                WebserviceResponse response = doPost(DSConstants.MCOMMONS_API_JOIN_URL, params);
                if (response.getStatusCode() < 400 || response.getStatusCode() > 500) {
                    webOpSuccess = true;
                }
            }
        }

    }

    private static final boolean nullOrEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    private static boolean nullOrEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }
}
