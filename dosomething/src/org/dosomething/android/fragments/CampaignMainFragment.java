package org.dosomething.android.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
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
import org.dosomething.android.activities.ReportBack;
import org.dosomething.android.activities.SFGGallery;
import org.dosomething.android.activities.SignUp;
import org.dosomething.android.analytics.Analytics;
import org.dosomething.android.cache.Cache;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.dao.DSDao;
import org.dosomething.android.domain.UserCampaign;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.dosomething.android.transfer.Campaign;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import roboguice.fragment.RoboFragment;

/**
 * TODO
 */
public class CampaignMainFragment extends RoboFragment implements View.OnClickListener {

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
    private Button btnReportBack;
    private Button btnSignUp;
    private FrameLayout frmVideo;
    private ImageView imgVideoThumb;
    private ImageView imgThumb;
    private LinearLayout llSMSReferContainer;
    private TextView txtSMSRefer;
    private Button btnSMSRefer;

    private Campaign campaign;

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
        btnReportBack = (Button)view.findViewById(R.id.report_back);
        btnSignUp = (Button)view.findViewById(R.id.sign_up);
        frmVideo = (FrameLayout)view.findViewById(R.id.frmVideo);
        imgVideoThumb = (ImageView)view.findViewById(R.id.imgVideoThumb);
        imgThumb = (ImageView)view.findViewById(R.id.imgThumb);
        llSMSReferContainer = (LinearLayout)view.findViewById(R.id.sms_refer_container);
        txtSMSRefer = (TextView)view.findViewById(R.id.sms_refer_text);
        btnSMSRefer = (Button)view.findViewById(R.id.sms_refer);

        Bundle args = getArguments();
        campaign = (Campaign)args.getSerializable(CAMPAIGN);

        // Setup click listener for buttons
        btnReportBack.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);
        btnSMSRefer.setOnClickListener(this);

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
            case R.id.report_back:
                startActivity(ReportBack.getIntent(getActivity(), campaign));
                break;
            case R.id.sign_up:
                signUp(v);
                break;
            case R.id.sms_refer:
                startActivityForResult(CampaignSMSRefer.getIntent(getActivity(), campaign), SMS_REFER_ACTIVITY);
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

        btnReportBack.setTypeface(headerTypeface, Typeface.BOLD);
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

    public void signUp(View v) {
        Activity assocActivity = getActivity();
        String uid = new UserContext(assocActivity).getUserUid();

        if (campaign.getCampaignType() == DSConstants.CAMPAIGN_TYPE.SHARE_FOR_GOOD) {
            startActivity(SFGGallery.getIntent(assocActivity, campaign));
        }
        else if (useSmsActionSignUp()) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(assocActivity);
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
        else if (useAlternateSignUp()) {
            // Log the alternate sign up events to Flurry Analytics
            HashMap<String, String> param = new HashMap<String, String>();
            param.put(CAMPAIGN, campaign.getName());
            Analytics.logEvent("sign-up-submit", param);

            // and Google Analytics
            Analytics.logEvent("sign-up", "alt-sign-up", campaign.getName());

            // Open up the link in another activity to view
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(campaign.getSignUpAltLink()));
            startActivity(i);
        }
        else if (uid != null) {
            startActivity(SignUp.getIntent(assocActivity, campaign));
        }
        else {
            startActivityForResult(new Intent(assocActivity, Login.class), REQ_LOGIN_FOR_SIGN_UP);
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

    private void updateSignUpButton(Context context) {
        if (useAlternateSignUp() || useSmsActionSignUp()) {
            btnSignUp.setEnabled(true);
            btnSignUp.setText(campaign.getSignUpAltText());
        }
        else if (userContext.isLoggedIn() && campaign != null) {
            if (hasNoSignUp() && campaign.getReportBack() != null) {
                btnReportBack.setVisibility(Button.VISIBLE);
                btnSignUp.setVisibility(Button.GONE);
            }
            else {
                UserCampaign userCampaign = new DSDao(context).findUserCampaign(userContext.getUserUid(), campaign.getId());
                if (userCampaign != null) {
                    // If user's already signed up and there is a report back available, then show report back button
                    if (campaign.getReportBack() != null) {
                        btnReportBack.setVisibility(Button.VISIBLE);
                        btnSignUp.setVisibility(Button.GONE);
                    }
                    else {
                        btnSignUp.setEnabled(false);
                        btnSignUp.setText(R.string.campaign_sign_up_button_already_signed_up);

                        btnReportBack.setVisibility(Button.GONE);
                        btnSignUp.setVisibility(Button.VISIBLE);
                    }
                }
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
