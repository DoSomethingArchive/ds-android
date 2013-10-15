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
                imageView.setImageResource(R.drawable.walkthrough_bg1);
                textView.setText(R.string.walkthrough_screen1);
                break;
            case 1:
                imageView.setImageResource(R.drawable.walkthrough_bg2);
                textView.setText(R.string.walkthrough_screen2);
                break;
            case 2:
                imageView.setImageResource(R.drawable.walkthrough_bg3);
                textView.setText(R.string.walkthrough_screen3);
                break;
        }

        return rootView;
    }
}
