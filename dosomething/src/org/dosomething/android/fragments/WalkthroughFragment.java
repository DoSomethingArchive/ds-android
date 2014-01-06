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

    @Inject private @Named("ProximaNova-Reg")Typeface typefaceReg;

    private ImageView mImageView;
    private TextView mTextView;

    private int mPageNumber;

    public WalkthroughFragment() {
        mPageNumber = 0;
    }

    public WalkthroughFragment(int pageNumber) {
        mPageNumber = pageNumber;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_walkthrough, container, false);
        mImageView = (ImageView)rootView.findViewById(R.id.image);
        mTextView = (TextView)rootView.findViewById(R.id.text);
        mTextView.setTypeface(typefaceReg);

        switch(mPageNumber) {
            case 0:
                mTextView.setText(R.string.walkthrough_screen1);
                break;
            case 1:
                mTextView.setText(R.string.walkthrough_screen2);
                break;
            case 2:
                mTextView.setText(R.string.walkthrough_screen3);
                break;
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        switch(mPageNumber) {
            case 0:
                mImageView.setImageResource(R.drawable.walkthrough_bg1);
                break;
            case 1:
                mImageView.setImageResource(R.drawable.walkthrough_bg2);
                break;
            case 2:
                mImageView.setImageResource(R.drawable.walkthrough_bg3);
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        mImageView.setImageDrawable(null);
    }
}
