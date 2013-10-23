package org.dosomething.android;

import android.app.Application;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.dosomething.android.receivers.UAPushNotificationReceiver;

@ReportsCrashes(formKey="dEhCTHFETWdJcUYxX0s0TUV0RzV0Ync6MQ")
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        ACRA.init(this);
        super.onCreate();

        AirshipConfigOptions uaOptions = AirshipConfigOptions.loadDefaultOptions(this);
        uaOptions.gcmSender = getString(R.string.ua_gcmSender);
        uaOptions.transport = getString(R.string.ua_transport);
        uaOptions.developmentAppKey = getString(R.string.ua_developmentAppKey);
        uaOptions.developmentAppSecret = getString(R.string.ua_developmentAppSecret);
        uaOptions.productionAppKey = getString(R.string.ua_productionAppKey);
        uaOptions.productionAppSecret = getString(R.string.ua_productionAppSecret);
        uaOptions.inProduction = DSConstants.inProduction;
        uaOptions.analyticsEnabled = DSConstants.inProduction;

        UAirship.takeOff(this, uaOptions);

        // Register receiver to handle UA pushes
        PushManager.shared().setIntentReceiver(UAPushNotificationReceiver.class);

        // TODO: this should be configurable by the user
        PushManager.enablePush();
    }

}
