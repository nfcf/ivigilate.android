package com.ivigilate.android;

import android.app.Application;
import android.content.Intent;
import com.ivigilate.android.classes.Settings;
import com.ivigilate.android.listeners.WidgetProvider;
import com.ivigilate.android.services.MainService;
import com.ivigilate.android.utils.Logger;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

//TODO: Going to need to update the ACRA to use a different backend (instead of google forms) as it is no longer supported...
@ReportsCrashes(formKey = "", mailTo = "nunofcf@gmail.com", mode = ReportingInteractionMode.TOAST, forceCloseDialogAfterToast = false, resToastText = R.string.crash_text)
public class AppContext extends Application {
    public static final String SERVER_BASE_URL = "http://192.168.1.14:8000";

    public static final int NOTIFICATION_ID = 1;

    public Settings settings = null;

    @Override
    public void onCreate() {
        Logger.d("Started...");
        super.onCreate();

        ACRA.init(this);

        if (settings == null) {
            settings = new Settings(this);
        }
        Logger.i("Finished...");
    }

    public void startService() {
        Logger.d("Starting service...");
        settings.setServiceEnabled(true);
        Intent i = new Intent(this, MainService.class);
        this.startService(i);

        Logger.d("Updating widgets...");
        WidgetProvider.updateWidget(this);
    }

    public void stopService() {
        Logger.d("Stopping service...");
        settings.setServiceEnabled(false);
        Intent i = new Intent(this, MainService.class);
        this.stopService(i);

        Logger.d("Updating widgets...");
        WidgetProvider.updateWidget(this);
    }

}
