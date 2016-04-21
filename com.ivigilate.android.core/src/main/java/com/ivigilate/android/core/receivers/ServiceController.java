package com.ivigilate.android.core.receivers;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ivigilate.android.core.services.MainService;
import com.ivigilate.android.core.utils.Logger;

public class ServiceController extends BroadcastReceiver {

    private static Intent sMainServiceIntent = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!isServiceRunning(context, MainService.class)) {
            Logger.d("Starting MainService by AlarmManager...");
            sMainServiceIntent = new Intent(context, MainService.class);
            context.startService(sMainServiceIntent);
        }
    }

    private boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : am.getRunningServices(Integer.MAX_VALUE)) {
            String className1 = serviceInfo.service.getClassName();
            String className2 = serviceClass.getName();
            if (className1.equals(className2)) {
                return true;
            }
        }
        return false;
    }

    public static Intent getServiceIntent() {
        return sMainServiceIntent;
    }
}

