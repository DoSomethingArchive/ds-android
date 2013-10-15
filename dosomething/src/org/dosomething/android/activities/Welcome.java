package org.dosomething.android.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.dosomething.android.R;
import org.dosomething.android.fragments.WalkthroughFragment;

import roboguice.inject.InjectView;

public class Welcome extends AbstractFragmentActivity implements View.OnClickListener {

    private final int NUM_WALKTHROUGH_PAGES = 3;

    @Inject private @Named("DINComp-CondBold")Typeface dinTypeface;

    @InjectView(R.id.button_login) private Button mBtnLogin;
    @InjectView(R.id.button_register) private Button mBtnRegister;
    @InjectView(R.id.pager) private ViewPager mPager;
    @InjectView(R.id.pager_dots) private LinearLayout mDotsLayout;

    private PagerAdapter mPagerAdapter;

    @Override
    protected String getPageName() {
        return "Welcome";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);

        mPagerAdapter = new WalkthroughPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setDotPosition(position);
            }
        });

        // Draw dots for pager position at position 0
        setDotPosition(0);

        // Set typeface for register and login buttons
        mBtnLogin.setTypeface(dinTypeface);
        mBtnRegister.setTypeface(dinTypeface);

        // Set button click listeners to launch next appropriate activity
        mBtnLogin.setOnClickListener(this);
        mBtnRegister.setOnClickListener(this);
    }

    /**
     * Implements View.OnClickListener. Listen for button click events to launch and handle them.
     *
     * @param v View that register the click event
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_login:
                startActivity(new Intent(Welcome.this, Login.class));
                break;
            case R.id.button_register:
                startActivity(new Intent(Welcome.this, Register.class));
                break;
        }
    }

    /**
     * Draw dots to indicate current position in the pager view.
     *
     * @param position Index position of the dot to fill in
     */
    private void setDotPosition(int position) {
        mDotsLayout.removeAllViews();

        LayoutInflater inflater = getLayoutInflater();
        for (int i = 0; i < NUM_WALKTHROUGH_PAGES; i++) {
            int dotResource = R.layout.shape_pager_dot_open;
            if (i == position) {
                dotResource = R.layout.shape_pager_dot_filled;
            }

            View v = inflater.inflate(dotResource, mDotsLayout, false);
            mDotsLayout.addView(v);
        }
    }

    /**
     * Pager Adapter to place custom WalkthroughFragments into the Pager.
     */
    private class WalkthroughPagerAdapter extends FragmentStatePagerAdapter {
        public WalkthroughPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new WalkthroughFragment(position);
        }

        @Override
        public int getCount() {
            return NUM_WALKTHROUGH_PAGES;
        }
    }
}
