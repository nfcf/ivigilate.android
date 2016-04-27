package com.ivigilate.android.core;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ivigilate.android.core.utils.Logger;

public class IVigilateServiceController extends BroadcastReceiver {

    private static Intent sIVigilateServiceIntent = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!isServiceRunning(context, IVigilateService.class)) {
            Logger.d("Starting IVigilateService by AlarmManager...");
            sIVigilateServiceIntent = new Intent(context, IVigilateService.class);
            context.startService(sIVigilateServiceIntent);
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
        return sIVigilateServiceIntent;
    }
}

