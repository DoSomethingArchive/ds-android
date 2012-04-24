package org.dosomething.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class StartUp extends Activity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        boolean loggedIn = false;
        
        if(loggedIn){
        	startActivity(new Intent(this, Profile.class));
        }else{
        	startActivity(new Intent(this, Login.class));
        }
        
    }
	
}
