package org.dosomething.android.tasks;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.dosomething.android.DSConstants;
import org.dosomething.android.activities.DSWebViewActivity;
import org.dosomething.android.cache.DSPreferences;
import org.dosomething.android.context.UserContext;
import org.json.JSONObject;

/**
 * SurveyCheckTask is a webservice task that retrieves survey info from a given location. If valid
 * survey info is found, it'll make the survey View prompt visible and set up the listener to open
 * up a DSWebViewActivity to display it.
 */
public class SurveyCheckTask extends AbstractWebserviceTask {

    // View with the survey text and button
    private View mView;

    // View to display the survey text
    private TextView mTextView;

    // Text prompt displayed in the survey View
    private String mSurveyText;

    // Handle to the Button to open up the webview for the survey
    private Button mOpenSurvey;

    // URL for the survey as provided by the web service response
    private String mSurveyUrl;

    // ID for the survey as provided by the web service response
    private int mSurveyId;

    // Whether or not to display the View
    private boolean mShowView;

    // Handle to SharedPreferences data
    private DSPreferences mPrefs;

    public SurveyCheckTask(UserContext userContext, View view, TextView textView, Button openSurvey) {
        super(userContext);

        mView = view;
        mTextView = textView;
        mOpenSurvey = openSurvey;
        mShowView = false;

        if (mView != null) {
            mPrefs = new DSPreferences(mView.getContext());
        }
    }

    @Override
    protected void doWebOperation() throws Exception {
        WebserviceResponse response = doGet(DSConstants.SURVEY_CHECK_URL);
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 400) {
            JSONObject jsonResponse = response.getBodyAsJSONObject();
            mSurveyId = jsonResponse.optInt("id", 0);

            int lastSurveyId = mPrefs.getLastSurveyId();

            if (mSurveyId > 0 && mSurveyId > lastSurveyId) {
                mSurveyText = jsonResponse.optString("text");
                mSurveyUrl = jsonResponse.getString("url");
                mShowView = true;
            }
        }
    }

    @Override
    protected void onSuccess() {
        // Display the survey popup in the View
        if (mShowView && mView != null && mTextView != null && mOpenSurvey != null) {
            mView.setVisibility(View.VISIBLE);

            // Text prompt to take the survey
            if (mSurveyText.length() > 0) {
                mTextView.setText(mSurveyText);
            }

            // Click listener for the "open survey" button
            mOpenSurvey.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DSPreferences prefs = new DSPreferences(mView.getContext());
                    prefs.setLastSurveyId(mSurveyId);

                    Intent i = new Intent(view.getContext(), DSWebViewActivity.class);
                    i.setData(Uri.parse(mSurveyUrl));
                    view.getContext().startActivity(i);
                }
            });
        }
        else {
            mView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onFinish() {
    }

    @Override
    protected void onError(Exception e) {
    }
}
