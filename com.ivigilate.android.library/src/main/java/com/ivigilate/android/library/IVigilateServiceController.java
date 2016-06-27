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

            // Commented out as this wasn't working when the proccess got killed by android
            // and so was replaced with a foreground notification...
            //iVigilateManager.setKeepServiceAliveAlarm();  // this restarts the alarmManager if required...

            sIVigilateServiceIntent = new Intent(context, IVigilateService.class);
            context.startService(sIVigilateServiceIntent);

            // http://stackoverflow.com/questions/29114072/broadcastreceiver-components-are-not-allowed-to-bind-to-services-in-android
            context.getApplicationContext().bindService(sIVigilateServiceIntent, iVigilateManager.mServiceConn, Context.BIND_AUTO_CREATE);
        }
    }

    protected static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        String serviceClassName = serviceClass.getName();

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : am.getRunningServices(Integer.MAX_VALUE)) {
            String className = serviceInfo.service.getClassName();
            if (className.equals(serviceClassName)) {
                return true;
            }
        }
        return false;
    }

    public static Intent getServiceIntent() {
        return sIVigilateServiceIntent;
    }
}

