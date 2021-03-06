package org.dosomething.android.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.analytics.Analytics;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.dao.DSDao;
import org.dosomething.android.domain.UserCampaign;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.WebForm;

import java.util.HashMap;

public class ReportBack extends AbstractWebForm {

    // Key for the campaign data passed to the fragment through arguments
    private static final String CAMPAIGN = DSConstants.EXTRAS_KEY.CAMPAIGN.getValue();

    // Key for the image path to attach to the report back
    private static final String REPORT_BACK_IMG = DSConstants.EXTRAS_KEY.REPORT_BACK_IMG.getValue();

    public static final int STEP_NUMBER = 4;

    private WebForm webForm;

    @Override
    public String getPageName() {
        return "report-back";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Campaign campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);
        webForm = campaign.getReportBack();

        String imgPath = getIntent().getExtras().getString(REPORT_BACK_IMG);
        if (imgPath != null && imgPath.length() > 0) {
            mPreselectedImage = imgPath;
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSubmitSuccess() {
        Campaign campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);

        DSDao dao = new DSDao(this);

        String uid = new UserContext(this).getUserUid();
        UserCampaign uc = new UserCampaign.UserCampaignCVBuilder()
                .campaignId(campaign.getId())
                .uid(uid)
                .build();
        // For scenarios where a campaign might not have a sign up action (like pre-2014 campaigns),
        // ensure the campaign is marked as signed up.
        dao.setSignedUp(uc);

        // Then also mark the Report Back step as completed.
        dao.setCampaignStepCompleted(uid, campaign.getId(), STEP_NUMBER);

        // Track submission in analytics - Flurry Analytics
        HashMap<String, String> param = new HashMap<String, String>();
        param.put("campaign", campaign.getName());
        Analytics.logEvent("report-back-submit", param);

        // and Google Analytics
        Analytics.logEvent("report-back", "normal-submit", campaign.getName());

        startActivity(CampaignShare.getIntentForReportedBack(this, campaign));
        finish();
    }

    @Override
    protected WebForm getWebForm() {
        return webForm;
    }

    protected int getContentViewResourceId() {
        return R.layout.report_back;
    }

    public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign) {
        Intent answer = new Intent(context, ReportBack.class);
        answer.putExtra(CAMPAIGN, campaign);
        return answer;
    }

}
