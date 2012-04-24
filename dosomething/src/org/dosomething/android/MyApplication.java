package org.dosomething.android;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey="dEhCTHFETWdJcUYxX0s0TUV0RzV0Ync6MQ")
public class MyApplication extends Application {

	@Override
	public void onCreate() {
		ACRA.init(this);
		super.onCreate();
	}

}
