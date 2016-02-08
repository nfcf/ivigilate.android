package com.ivigilate.android.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ivigilate.android.AppContext;
import com.ivigilate.android.utils.Logger;

public class StartStopServiceListener extends BroadcastReceiver {
    public static final String INTENT_EXTRA_TOGGLE = "toggle";
    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d("Started...");

        AppContext appContext = (AppContext) context.getApplicationContext();
        boolean shouldEnableService = appContext.settings.getServiceEnabled() ^ intent.getBooleanExtra(INTENT_EXTRA_TOGGLE, false);
        if (shouldEnableService) {
            appContext.startService();
        } else {
            appContext.stopService();
        }

        Logger.i("Finished...");
    }
}
