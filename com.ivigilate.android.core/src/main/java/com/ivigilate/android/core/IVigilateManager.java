package com.ivigilate.android.core;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.PowerManager;

import com.ivigilate.android.core.classes.ApiResponse;
import com.ivigilate.android.core.classes.Device;
import com.ivigilate.android.core.classes.Rest;
import com.ivigilate.android.core.classes.Settings;
import com.ivigilate.android.core.classes.User;
import com.ivigilate.android.core.interfaces.IDeviceSighted;
import com.ivigilate.android.core.interfaces.IVigilateApi;
import com.ivigilate.android.core.interfaces.IVigilateApiCallback;
import com.ivigilate.android.core.receivers.ServiceController;
import com.ivigilate.android.core.utils.Logger;
import com.ivigilate.android.core.utils.PhoneUtils;

import java.util.Date;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class IVigilateManager {
    private static final long INTERVAL_CHECK_SERVICE_ALIVE = 30 * 1000; // unit: ms

    private Context mContext;
    private Settings mSettings;

    private AlarmManager mAlarmManager;
    private PendingIntent mPendingIntentService;
    private IVigilateApi mApi;
    private IDeviceSighted mDeviceSighted;

    private static PowerManager.WakeLock mWakeLock;
    private static WifiManager.WifiLock mWifiLock;

    private IVigilateManager(Context context) {
        mContext = context;
        mSettings = new Settings(context);

        RestAdapter restAdapter = Rest.createAdapter(mContext, mSettings.getServerAddress());
        mApi = restAdapter.create(IVigilateApi.class);
    }

    public Settings getSettings() {
        return mSettings;
    }
    public void setServerAddress(String address) {
        mSettings.setServerAddress(address);

        RestAdapter restAdapter = Rest.createAdapter(mContext, mSettings.getServerAddress());
        mApi = restAdapter.create(IVigilateApi.class);
    }

    public void setOnDeviceSighted(IDeviceSighted deviceSighted) {
        mDeviceSighted = deviceSighted;
    }

    public void onDeviceSighted(String mac, String uid, int rssi) {
        if (mDeviceSighted != null) {
            mDeviceSighted.sighting(mac, uid, rssi);
        }
    }

    private static IVigilateManager mInstance;
    public static IVigilateManager getInstance(Context context) {
        if (mInstance == null) {
            Logger.d("IVigilateManager instance creation.");
            mInstance = new IVigilateManager(context);
        }
        return mInstance;
    }

    public void startService() {
        setKeepServiceAliveAlarm();
    }

    public void stopService() {
        cancelKeepServiceAliveAlarm();
    }

    public void acquireLocks(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "iVigilateWakelock");
        if(!mWakeLock.isHeld()){
            mWakeLock.acquire();
        }

        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "iVigilateWifiLock");
        if(!mWifiLock.isHeld()){
            mWifiLock.acquire();
        }
    }

    public void releaseLocks() {
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }

        if (mWifiLock != null) {
            if (mWifiLock.isHeld()) {
                mWifiLock.release();
            }
        }
    }

    public void login(User loginUser, final IVigilateApiCallback<User> callback) {

        loginUser.metadata = String.format("{\"device\": {\"uid\": \"%s\"}}", PhoneUtils.getDeviceUniqueId(mContext));
        mApi.login(loginUser, new Callback<ApiResponse<User>>() {
            @Override
            public void success(ApiResponse<User> result, Response response) {
                Logger.d("Login successful!");
                mSettings.setServerTimeOffset(new Date(result.timestamp).getTime() - System.currentTimeMillis());
                mSettings.setUser(result.data);

                provisionDevice(new Device(Device.Type.DetectorUser, PhoneUtils.getDeviceUniqueId(mContext), ""), null);

                if (callback != null) callback.success(result.data);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                String errorMsg = "";
                if (retrofitError.getResponse() != null) {
                    errorMsg = new String(((TypedByteArray) retrofitError.getResponse().getBody()).getBytes());
                } else {
                    errorMsg = retrofitError.getKind().toString() + " - " + retrofitError.getMessage();
                }

                Logger.e("Device provisioning failed with error: " + errorMsg);
                if (callback != null) callback.failure(errorMsg);
            }
        });
    }

    public void provisionDevice(final Device device, final IVigilateApiCallback<Void> callback) {
        mApi.provisionDevice(device, new Callback<ApiResponse<Void>>() {
            @Override
            public void success(ApiResponse<Void> result, Response response) {
                Logger.i("Device '" + device.uid + "' of type '" + device.type.toString() + "' provisioned successfully.");
                mSettings.setServerTimeOffset(new Date(result.timestamp).getTime() - System.currentTimeMillis());

                if (callback != null) callback.success(null);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                String errorMsg = "";
                if (retrofitError.getResponse() != null) {
                    errorMsg = new String(((TypedByteArray) retrofitError.getResponse().getBody()).getBytes());
                } else {
                    errorMsg = retrofitError.getKind().toString() + " - " + retrofitError.getMessage();
                }

                Logger.e("Device provisioning failed with error: " + errorMsg);
                if (callback != null) callback.failure(errorMsg);
            }
        });
    }


    private void setKeepServiceAliveAlarm() {
        Intent i = new Intent(mContext, ServiceController.class);
        if (PendingIntent.getBroadcast(mContext, 0, i, PendingIntent.FLAG_NO_CREATE) == null) {

            mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            //Start service and alarmManager to make sure service is always running
            Intent intentService = new Intent(mContext, ServiceController.class);
            mPendingIntentService = PendingIntent.getBroadcast(mContext, 0, intentService, PendingIntent.FLAG_UPDATE_CURRENT);

            mAlarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP, 0, INTERVAL_CHECK_SERVICE_ALIVE,
                    mPendingIntentService);
        }
    }

    private void cancelKeepServiceAliveAlarm() {
        if (mPendingIntentService != null) {
            if (mAlarmManager != null) mAlarmManager.cancel(mPendingIntentService);
            mPendingIntentService.cancel();
        }
    }

}
