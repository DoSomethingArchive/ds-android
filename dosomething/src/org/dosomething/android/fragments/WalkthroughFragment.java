package org.dosomething.android.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.dosomething.android.R;

import roboguice.fragment.RoboFragment;

/**
 * Fragment for walkthrough slides.
 */
public class WalkthroughFragment extends RoboFragment {

    @Inject private @Named("DINComp-CondBold")Typeface dinTypeface;

    private int mPageNumber;

    public WalkthroughFragment(int pageNumber) {
        mPageNumber = pageNumber;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_walkthrough, container, false);
        ImageView imageView = (ImageView)rootView.findViewById(R.id.image);
        TextView textView = (TextView)rootView.findViewById(R.id.text);
        textView.setTypeface(dinTypeface);

        // TODO: change out dummy images and text for finalized walkthrough content
        switch(mPageNumber) {
            case 0:
                imageView.setImageResource(R.drawable.cause_animals);
                textView.setText("Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.");
                break;
            case 1:
                imageView.setImageResource(R.drawable.cause_bullying);
                textView.setText("Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat.");
                break;
            case 2:
                imageView.setImageResource(R.drawable.cause_disasters);
                textView.setText("Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat");
                break;
            case 3:
                imageView.setImageResource(R.drawable.cause_discrimination);
                textView.setText("Vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi.");
                break;
            case 4:
                imageView.setImageResource(R.drawable.cause_health);
                textView.setText("Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum.");
                break;
        }

        return rootView;
    }
}
