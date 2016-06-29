package com.ivigilate.android.app;

import android.app.Application;

import com.ivigilate.android.app.utils.Logger;
import com.ivigilate.android.library.IVigilateManager;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

//TODO: Going to need to update the ACRA to use a different backend (instead of google forms) as it is no longer supported...
@ReportsCrashes(formKey = "", mailTo = "support@ivigilate.com", mode = ReportingInteractionMode.TOAST, forceCloseDialogAfterToast = false, resToastText = R.string.crash_text)
public class AppContext extends Application {
    private IVigilateManager mIVigilateManager;

    @Override
    public void onCreate() {
        Logger.d("Started...");
        super.onCreate();

        ACRA.init(this);

        mIVigilateManager = IVigilateManager.getInstance(this);
        mIVigilateManager.setServiceSendInterval(2 * 1000);
        
        Logger.i("Finished...");
    }

    public IVigilateManager getIVigilateManager() {
        return mIVigilateManager;
    }

}
