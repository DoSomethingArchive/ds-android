package org.dosomething.android.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.activities.ReportBack;
import org.dosomething.android.transfer.Campaign;

/**
 * Campaign sub-page to start the campaign report back process.
 */
public class CampaignReportBackFragment extends AbstractCampaignFragment implements View.OnClickListener {

    // Key for the campaign data passed to the fragment through arguments
    private static final String CAMPAIGN = DSConstants.EXTRAS_KEY.CAMPAIGN.getValue();

    // Key for the image path to attach to the report back
    private static final String REPORT_BACK_IMG = DSConstants.EXTRAS_KEY.REPORT_BACK_IMG.getValue();

    // Request code for the report back image picking action
    private static final int PICK_IMAGE_REQUEST = 0xFF0;

    // Button to begin the report back process
    private Button mBtnReportBack;

    // Campaign data with the report back fields we need
    private Campaign mCampaign;

    @Override
    public String getFragmentName() {
        return "Report-Back";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_campaign_reportback, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBtnReportBack = (Button)view.findViewById(R.id.reportBack);
        mBtnReportBack.setOnClickListener(this);

        Bundle args = getArguments();
        mCampaign = (Campaign)args.getSerializable(CAMPAIGN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If a picture was successfully selected, start Report Back activity passing the
        // selected image along.
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().managedQuery(uri, projection, null, null, null);
            int columnIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(columnIdx);

            Intent intent = ReportBack.getIntent(getActivity(), mCampaign);
            intent.putExtra(REPORT_BACK_IMG, path);

            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reportBack:
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
                break;
        }
    }
}
