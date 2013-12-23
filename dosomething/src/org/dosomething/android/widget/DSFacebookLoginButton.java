package org.dosomething.android.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.facebook.widget.LoginButton;

import org.dosomething.android.R;

/**
 * Custom Facebook Login button with Do Something style.
 */
public class DSFacebookLoginButton extends LinearLayout {

    private Typeface typefaceDin;

    private LoginButton mLoginButton;

    public DSFacebookLoginButton(Context context) {
        super(context);

        View v = View.inflate(context, R.layout.button_dsfacebooklogin, this);

        // Clear out Facebook background to then be able to provide our own
        mLoginButton = (LoginButton)v.findViewById(R.id.loginButton);
        mLoginButton.setBackgroundDrawable(null);

        // Set custom font
        if (!isInEditMode()) {
            typefaceDin = Typeface.createFromAsset(context.getAssets(), "ProximaNova-Bold.ttf");
            mLoginButton.setTypeface(typefaceDin, Typeface.BOLD);
        }
    }

    public DSFacebookLoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        View v = View.inflate(context, R.layout.button_dsfacebooklogin, this);

        // Clear out Facebook background to then be able to provide our own
        mLoginButton = (LoginButton)v.findViewById(R.id.loginButton);
        mLoginButton.setBackgroundDrawable(null);

        // Set custom font
        if (!isInEditMode()) {
            typefaceDin = Typeface.createFromAsset(context.getAssets(), "ProximaNova-Bold.ttf");
            mLoginButton.setTypeface(typefaceDin, Typeface.BOLD);
        }
    }

    /**
     * Facebook LoginButton accessor.
     *
     * @return Handle to the Facebook LoginButton object.
     */
    public LoginButton getButton() {
        return mLoginButton;
    }
}
