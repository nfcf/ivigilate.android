package com.ivigilate.android.app;

import android.app.Application;

import com.ivigilate.android.app.utils.Logger;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

//TODO: Going to need to update the ACRA to use a different backend (instead of google forms) as it is no longer supported...
@ReportsCrashes(formKey = "", mailTo = "nunofcf@gmail.com", mode = ReportingInteractionMode.TOAST, forceCloseDialogAfterToast = false, resToastText = R.string.crash_text)
public class AppContext extends Application {
    @Override
    public void onCreate() {
        Logger.d("Started...");
        super.onCreate();

        ACRA.init(this);
        
        Logger.i("Finished...");
    }

}
