package com.ivigilate.android.library;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ivigilate.android.library.utils.Logger;

public class IVigilateServiceController extends BroadcastReceiver {

    private static Intent sIVigilateServiceIntent = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        startService(context);
    }

    public static void startService(Context context) {
        IVigilateManager iVigilateManager = IVigilateManager.getInstance(context);

        if (iVigilateManager.getServiceEnabled() &&
                !isServiceRunning(context, IVigilateService.class)) {
            Logger.d("Starting IVigilateService...");

            iVigilateManager.setKeepServiceAliveAlarm();  // this restarts the alarmManager if required...

            sIVigilateServiceIntent = new Intent(context, IVigilateService.class);
            context.startService(sIVigilateServiceIntent);
        }
    }

    protected static boolean isServiceRunning(Context context, Class<?> serviceClass) {
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

