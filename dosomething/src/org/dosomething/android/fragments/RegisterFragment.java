package org.dosomething.android.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.activities.Register;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.DSFacebookLogin;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import roboguice.fragment.RoboFragment;

/**
 * TODO
 */
public class RegisterFragment extends RoboFragment implements View.OnClickListener {

    @Inject private UserContext userContext;
    @Inject private @Named("DINComp-CondBold")Typeface dinTypeface;

    private LoginButton mBtnFacebookLogin;
    private Button mBtnRegister;
    private EditText mEditBirthday;
    private EditText mEditMobile;
    private Register mRegisterActivity;
    private TextView mTextDisclaimer;
    private UiLifecycleHelper mUiHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUiHelper = new UiLifecycleHelper(getActivity(), callback);
        mUiHelper.onCreate(savedInstanceState);

        // In case any session is lingering with token info, clear it to start fresh
        Session session = Session.getActiveSession();
        if (session != null) {
            session.closeAndClearTokenInformation();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // This fragment is responsible for displaying the Login layout content
        // instead of the owning Activity.
        View view = inflater.inflate(R.layout.register, container, false);

        // Set custom typeface for the buttons
        mBtnRegister = (Button)view.findViewById(R.id.button_register);
        mBtnRegister.setTypeface(dinTypeface, Typeface.BOLD);

        // Set click listener on buttons
        mBtnRegister.setOnClickListener(this);

        // Cache reference to owning Activity
        mRegisterActivity = (Register)getActivity();

        // Set Fragment to handle login button action and set FB permissions we want
        mBtnFacebookLogin = (LoginButton) view.findViewById(R.id.button_facebook_login);
        mBtnFacebookLogin.setFragment(this);
        mBtnFacebookLogin.setReadPermissions(Arrays.asList("email", "user_birthday"));

        // Set typeface for OR text
        TextView orText = (TextView)view.findViewById(R.id.or_text);
        orText.setTypeface(dinTypeface, Typeface.BOLD);

        mEditBirthday = (EditText)view.findViewById(R.id.birthday);
        mEditBirthday.setOnFocusChangeListener(birthdayFocusListener);
        mEditBirthday.setOnClickListener(birthdayClickListener);

        mEditMobile = (EditText)view.findViewById(R.id.mobile);
        // Prepopulate mobile # field with # found on phone
        if (mEditMobile != null && userContext.getPhoneNumber() != null) {
            mEditMobile.setText(userContext.getPhoneNumber());
        }

        mTextDisclaimer = (TextView)view.findViewById(R.id.disclaimer);
        mTextDisclaimer.setMovementMethod(LinkMovementMethod.getInstance());
        mTextDisclaimer.setText(Html.fromHtml(getString(R.string.register_disclaimer)));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // For scenarios where the main activity is launched and user
        // session is not null, the session state change notification
        // may not be triggered. Trigger it if it's open/closed.
        Session session = Session.getActiveSession();
        if (session != null && (session.isOpened() || session.isClosed()) ) {
            onSessionStateChange(session, session.getState(), null);
        }

        mUiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mUiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        mUiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mUiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_register:
                mRegisterActivity.register();
                break;
        }
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        // If session state has changed to a logged in state, execute Facebook
        // login to DoSomething backend.
        if (state.isOpened()) {
            String fbAccessToken = session.getAccessToken();
            if (fbAccessToken != null && fbAccessToken.length() > 0) {
                DSFacebookLogin.execute(getActivity(), fbAccessToken);
            }
        }
    }

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private final DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Date savedBirthday = new GregorianCalendar(year, monthOfYear, dayOfMonth).getTime();
            mEditBirthday.setText(new SimpleDateFormat(DSConstants.DATE_FORMAT, Locale.US).format(savedBirthday));
        }
    };

    private final View.OnClickListener birthdayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showBirthdayPicker();
        }
    };

    private final View.OnFocusChangeListener birthdayFocusListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus) {
                showBirthdayPicker();
            }
        }
    };

    /**
     * Show picker dialog with date set to 13 years prior to current date.
     */
    private void showBirthdayPicker() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -13);
        new DatePickerDialog(getActivity(), dateListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1).show();
    }
}
