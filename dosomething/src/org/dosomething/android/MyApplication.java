package org.dosomething.android;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;

@ReportsCrashes(formKey="dEhCTHFETWdJcUYxX0s0TUV0RzV0Ync6MQ")
public class MyApplication extends Application {

	@Override
	public void onCreate() {
		ACRA.init(this);
		super.onCreate();
		
		AirshipConfigOptions uaOptions = AirshipConfigOptions.loadDefaultOptions(this);
		uaOptions.inProduction = DSConstants.inProduction;
		
		UAirship.takeOff(this, uaOptions);
		
		// TODO: this should be configurable by the user
		PushManager.enablePush();
	}

}
