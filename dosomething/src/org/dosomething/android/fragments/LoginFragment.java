package org.dosomething.android.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.dosomething.android.R;
import org.dosomething.android.activities.Login;
import org.dosomething.android.tasks.DSFacebookLogin;
import org.dosomething.android.widget.DSFacebookLoginButton;

import java.util.Arrays;

import roboguice.fragment.RoboFragment;

/**
 * Fragment for the Login screen.
 */
public class LoginFragment extends RoboFragment implements View.OnClickListener {

    @Inject private @Named("ProximaNova-Bold")Typeface typefaceBold;

    private Button mBtnLogin;
    private DSFacebookLoginButton mBtnFacebookLogin;
	
	private UiLifecycleHelper uiHelper;
	
	private Login loginActivity;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    uiHelper = new UiLifecycleHelper(getActivity(), callback);
	    uiHelper.onCreate(savedInstanceState);
		
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
	    View view = inflater.inflate(R.layout.login, container, false);

        // Set custom typeface for the buttons
        mBtnLogin = (Button)view.findViewById(R.id.button_login);
        mBtnLogin.setTypeface(typefaceBold, Typeface.BOLD);

        // Set click listener on buttons
        mBtnLogin.setOnClickListener(this);
	    
	    // Cache reference to owning Activity
	    loginActivity = (Login) getActivity();
	    
	    // Set Fragment to handle login button action and set FB permissions we want
	    mBtnFacebookLogin = (DSFacebookLoginButton) view.findViewById(R.id.button_facebook_login);
	    mBtnFacebookLogin.getButton().setFragment(this);
	    mBtnFacebookLogin.getButton().setReadPermissions(Arrays.asList("email", "user_birthday"));

        // Set typeface for OR text
        TextView orText = (TextView)view.findViewById(R.id.or_text);
        orText.setTypeface(typefaceBold, Typeface.BOLD);

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

	    uiHelper.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_login:
                loginActivity.logIn();
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
}
