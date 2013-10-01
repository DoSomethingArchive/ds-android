package org.dosomething.android.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.Prize;
import org.dosomething.android.transfer.PrizeItem;

import roboguice.fragment.RoboFragment;

/**
 * TODO
 */
public class CampaignPrizesFragment extends RoboFragment {

    private static final String CAMPAIGN = DSConstants.EXTRAS_KEY.CAMPAIGN.getValue();

    @Inject
    private LayoutInflater inflater;
    @Inject private ImageLoader imageLoader;
    @Inject @Named("DINComp-CondBold")Typeface headerTypeface;

    private LinearLayout content;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.campaign_prizes, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        content = (LinearLayout)view.findViewById(R.id.content);

        Bundle args = getArguments();
        Campaign campaign = (Campaign)args.getSerializable(CAMPAIGN);

        Prize prize = campaign.getPrize();

        if(prize.getMainText()!=null && prize.getMainText().length() > 0) {
            content.addView(createMainTextView(prize.getMainText()));
        }

        if(prize.getScholarship()!=null) {
            content.addView(createScholarshipView(prize.getScholarship()));
        }

        if(prize.getOthers()!=null) {
            for(PrizeItem prizeItem : prize.getOthers()) {
                content.addView(createPrizeView(prizeItem));
            }
        }

        if(prize.getRulesUrl()!=null && prize.getRulesUrl().length() > 0) {
            content.addView(createRulesView(prize.getRulesUrl()));
        }
    }

    private View createScholarshipView(PrizeItem scholarship) {
        View v = inflater.inflate(R.layout.prize_row, null);

        TextView header = (TextView)v.findViewById(R.id.header);
        header.setText(scholarship.getHeader());

        ImageView image = (ImageView)v.findViewById(R.id.image);
        header.setTypeface(headerTypeface, Typeface.BOLD);
        imageLoader.displayImage(scholarship.getImageUrl(), image);

        TextView body = (TextView)v.findViewById(R.id.body);
        body.setText(scholarship.getBody());

        return v;
    }

    private View createPrizeView(PrizeItem prizeItem) {
        View v = inflater.inflate(R.layout.prize_row, null);

        TextView header = (TextView)v.findViewById(R.id.header);
        header.setTypeface(headerTypeface, Typeface.BOLD);
        header.setText(prizeItem.getHeader());

        ImageView image = (ImageView)v.findViewById(R.id.image);
        imageLoader.displayImage(prizeItem.getImageUrl(), image);

        TextView body = (TextView)v.findViewById(R.id.body);
        body.setText(prizeItem.getBody());

        return v;
    }

    private View createRulesView(final String rules) {
        View v = inflater.inflate(R.layout.prize_rules, null);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(rules)));
            }
        });

        return v;
    }

    private View createMainTextView(final String mainText) {
        View v = inflater.inflate(R.layout.prize_main_text, null);

        TextView body = (TextView)v.findViewById(R.id.body);
        body.setText(mainText);

        return v;
    }
}
