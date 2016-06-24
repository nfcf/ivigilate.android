package com.ivigilate.android.library;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.ivigilate.android.library.classes.ApiResponse;
import com.ivigilate.android.library.classes.Device;
import com.ivigilate.android.library.classes.DeviceProvisioning;
import com.ivigilate.android.library.classes.GPSLocation;
import com.ivigilate.android.library.classes.Rest;
import com.ivigilate.android.library.classes.Sighting;
import com.ivigilate.android.library.classes.User;
import com.ivigilate.android.library.interfaces.ILocationListener;
import com.ivigilate.android.library.interfaces.ISightingListener;
import com.ivigilate.android.library.interfaces.IVigilateApi;
import com.ivigilate.android.library.interfaces.IVigilateApiCallback;
import com.ivigilate.android.library.interfaces.IDeviceSighting;
import com.ivigilate.android.library.utils.Logger;
import com.ivigilate.android.library.utils.PhoneUtils;

import org.altbeacon.beacon.powersave.BackgroundPowerSaver;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class IVigilateManager {
    private static final long INTERVAL_CHECK_SERVICE_ALIVE = 30 * 1000; // unit: ms

    public static final int LOCATION_REQUEST_PRIORITY_HIGH_ACCURACY = 100;
    public static final int LOCATION_REQUEST_PRIORITY_BALANCED_POWER_ACCURACY = 102;
    public static final int LOCATION_REQUEST_PRIORITY_LOW_POWER = 104;
    public static final int LOCATION_REQUEST_PRIORITY_NO_POWER = 105;

    private Context mContext;
    private Settings mSettings;

    private AlarmManager mAlarmManager;
    private PendingIntent mPendingIntentService;
    private IVigilateApi mApi;

    private ISightingListener mSightingListener;
    private ILocationListener mLocationListener;

    private static PowerManager.WakeLock mWakeLock;
    private static WifiManager.WifiLock mWifiLock;

    private static IVigilateManager mInstance;
    private static BackgroundPowerSaver mBackgroundPowerSaver;

    protected IVigilateService iVigilateService;
    protected ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            iVigilateService =  ((IVigilateService.Binder) binder).getService();
            //Log.d(LOG_TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //Log.d(LOG_TAG, "onServiceDisconnected");
            iVigilateService = null;
        }
    };

    private IVigilateManager(Context context) {
        mContext = context;
        mSettings = new Settings(context);
        mApi = Rest.createService(IVigilateApi.class, mContext, mSettings.getServerAddress(), mSettings.getUser() != null ? mSettings.getUser().token : "");
    }

    public static IVigilateManager getInstance(Context context) {
        if (mInstance == null) {
            Logger.d("IVigilateManager instance creation.");
            mInstance = new IVigilateManager(context);

            // Simply constructing this class and holding a reference to it in your custom Application class
            // enables auto battery saving "of about 60%"
            mBackgroundPowerSaver = new BackgroundPowerSaver(context);
        }
        return mInstance;
    }

    public String getServerAddress() {
        return mSettings.getServerAddress();
    }

    public void setServerAddress(String address) {
        mSettings.setServerAddress(address);

        mApi = Rest.createService(IVigilateApi.class, mContext, mSettings.getServerAddress(), mSettings.getUser() != null ? mSettings.getUser().token : "");
    }

    protected long getServerTimeOffset() {
        return mSettings.getServerTimeOffset();
    }

    protected void setServerTimeOffset(long offset) {
        mSettings.setServerTimeOffset(offset);
    }

    protected boolean getServiceEnabled() {
        return mSettings.getServiceEnabled();
    }

    public void clearServiceCache() {
        mSettings.setServiceInvalidBeacons(null);
        mSettings.setServiceActiveSightings(null);
    }

    protected HashMap<String, Long> getServiceInvalidBeacons() {
        return mSettings.getServiceInvalidBeacons();
    }

    protected void setServiceInvalidBeacons(HashMap<String, Long> invalidBeacons) {
        mSettings.setServiceInvalidBeacons(invalidBeacons);
    }

    protected HashMap<String, Sighting> getServiceActiveSightings() {
        return mSettings.getServiceActiveSightings();
    }

    protected void setServiceActiveSightings(HashMap<String, Sighting> activeSightings) {
        mSettings.setServiceActiveSightings(activeSightings);
    }

    protected long getServiceSendInterval() {
        return mSettings.getServiceSendInterval();
    }

    public void setServiceSendInterval(int interval) {
        mSettings.setServiceSendInterval(interval);
    }

    protected int getServiceSightingStateChangeInterval() {
        return mSettings.getServiceSightingStateChangeInterval();
    }

    /**
     * Sets the interval after which a sighting is closed when a beacon is no longer seen
     * This sets the sighting as either 'AutoClosing' or 'ManualClosing'.
     * AutoClosing - All beacon sightings are sent to the server. The server runs a cron job to
     * close sightings not seen for more than X seconds.
     * ManualClosing - Only an open sighting and close sighting events are sent to the server
     * respecting the interval value set.
     * @param intervalInMilliSeconds the interval in ms for 'ManualClosing' or 0 (zero) for 'AutoClosing'
     */
    public void setServiceSightingStateChangeInterval(int intervalInMilliSeconds) {
        mSettings.setServiceSightingStateChangeInterval(intervalInMilliSeconds);
    }

    public JsonObject getServiceSightingMetadata() {
        return mSettings.getServiceSightingMetadata();
    }

    public void setServiceSightingMetadata(JsonObject value) {
        mSettings.setServiceSightingMetadata(value);
    }

    protected int getLocationRequestInterval() {
        return mSettings.getLocationRequestInterval();
    }

    public void setLocationRequestInterval(int intervalInMilliSeconds) {
        mSettings.setLocationRequestInterval(intervalInMilliSeconds);
    }

    protected long getLocationRequestFastestInterval() {
        return mSettings.getLocationRequestFastestInterval();
    }

    public void setLocationRequestFastestInterval(int intervalInMilliSeconds) {
        mSettings.setLocationRequestFastestInterval(intervalInMilliSeconds);
    }

    protected long getLocationRequestSmallestDisplacement() {
        return mSettings.getLocationRequestSmallestDisplacement();
    }

    public void setLocationRequestSmallestDisplacement(int distanceInMeters) {
        mSettings.setLocationRequestSmallestDisplacement(distanceInMeters);
    }

    public void setLocationRequestPriority(int locationRequestPriority) {
        mSettings.setLocationRequestPriority(locationRequestPriority);
    }

    protected int getLocationRequestPriority() {
        return mSettings.getLocationRequestPriority();
    }

    public User getUser() {
        return mSettings.getUser();
    }

    protected void setUser(User user) {
        mSettings.setUser(user);
    }

    public void setSightingListener(ISightingListener sightingListener) {
        mSightingListener = sightingListener;
    }

    public void setLocationListener(ILocationListener locationListener) {
        mLocationListener = locationListener;
    }

    public void startService() {
        mSettings.setServiceEnabled(true);

        // Start the service immediately if required
        IVigilateServiceController.startService(mContext);

        // Start AlarmManager to keep service alive every X seconds
        setKeepServiceAliveAlarm();
    }

    public void stopService() {
        mSettings.setServiceEnabled(false);

        cancelKeepServiceAliveAlarm();
    }

    public void acquireLocks() {
        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "iVigilateWakelock");
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }

        WifiManager wm = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "iVigilateWifiLock");
        if (!mWifiLock.isHeld()) {
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
                mSettings.setServerTimeOffset(result.timestamp - System.currentTimeMillis());
                mSettings.setUser(result.data);

                if (mSettings.getUser() != null) {
                    mApi = Rest.createService(IVigilateApi.class, mContext, mSettings.getServerAddress(), mSettings.getUser().token);

                    JsonObject metadata = new JsonObject();
                    JsonObject device = new JsonObject();
                    device.addProperty("model", PhoneUtils.getDeviceName());
                    metadata.add("device", device);
                    DeviceProvisioning deviceProvisioning = new DeviceProvisioning(DeviceProvisioning.DeviceType.DetectorUser,
                            PhoneUtils.getDeviceUniqueId(mContext),
                            mSettings.getUser().email,
                            true,
                            metadata);

                    provisionDevice(deviceProvisioning, null);

                    if (callback != null) callback.success(result.data);
                } else {
                    if (callback != null) callback.failure("Failed to get User info!");
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                String error = retrofitError.getLocalizedMessage();
                try {
                    Gson gson = new Gson();
                    Type type = new TypeToken<ApiResponse<String>>() {
                    }.getType();
                    ApiResponse<String> errorObj = gson.fromJson(error, type);
                    mSettings.setServerTimeOffset(errorObj.timestamp - System.currentTimeMillis());

                    error = errorObj.data;
                } catch (Exception ex) {
                    // Do nothing...
                }

                Logger.e("Device provisioning failed with error: " + error);
                if (callback != null) callback.failure(error);
            }
        });
    }

    public void logout(final IVigilateApiCallback<User> callback) {

        mSettings.setUser(null);
        mApi.logout(new Callback<ApiResponse<User>>() {
            @Override
            public void success(ApiResponse<User> result, Response response) {
                Logger.d("Logout successful!");
                mSettings.setServerTimeOffset(result.timestamp - System.currentTimeMillis());

                if (callback != null) callback.success(result.data);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                String error = retrofitError.getLocalizedMessage();
                try {
                    Gson gson = new Gson();
                    Type type = new TypeToken<ApiResponse<String>>() {
                    }.getType();
                    ApiResponse<String> errorObj = gson.fromJson(error, type);
                    mSettings.setServerTimeOffset(errorObj.timestamp - System.currentTimeMillis());

                    error = errorObj.data;
                } catch (Exception ex) {
                    // Do nothing...
                }

                Logger.e("Logout failed with error: " + error);
                if (callback != null) callback.failure(error);
            }
        });
    }

    public void provisionDevice(final DeviceProvisioning deviceProvisioning, final IVigilateApiCallback<String> callback) {
        mApi.provisionDevice(deviceProvisioning, new Callback<ApiResponse<String>>() {
            @Override
            public void success(ApiResponse<String> result, Response response) {
                Logger.i("Device '" + deviceProvisioning.uid + "' of type '" + deviceProvisioning.type.toString() + "' provisioned successfully.");
                mSettings.setServerTimeOffset(result.timestamp - System.currentTimeMillis());

                if (callback != null) callback.success(result.data);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                String error = retrofitError.getLocalizedMessage();
                try {
                    Gson gson = new Gson();
                    Type type = new TypeToken<ApiResponse<String>>() {
                    }.getType();
                    ApiResponse<String> errorObj = gson.fromJson(error, type);
                    mSettings.setServerTimeOffset(errorObj.timestamp - System.currentTimeMillis());

                    error = errorObj.data;
                } catch (Exception ex) {
                    // Do nothing...
                }

                Logger.e("Device provisioning failed with error: " + error);
                if (callback != null) callback.failure(error);
            }
        });
    }

    public void getBeacons(final IVigilateApiCallback<List<Device>> callback) {
        mApi.getBeacons(new Callback<List<Device>>() {
            @Override
            public void success(List<Device> result, Response response) {
                if (callback != null) callback.success(result);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                String error = retrofitError.getLocalizedMessage();
                try {
                    Gson gson = new Gson();
                    Type type = new TypeToken<ApiResponse<String>>() {
                    }.getType();
                    ApiResponse<String> errorObj = gson.fromJson(error, type);
                    mSettings.setServerTimeOffset(errorObj.timestamp - System.currentTimeMillis());

                    error = errorObj.data;
                } catch (Exception ex) {
                    // Do nothing...
                }

                Logger.e("Device provisioning failed with error: " + error);
                if (callback != null) callback.failure(error);
            }
        });
    }

    public void getDetectors(final IVigilateApiCallback<List<Device>> callback) {
        mApi.getDetectors(new Callback<List<Device>>() {
            @Override
            public void success(List<Device> result, Response response) {
                if (callback != null) callback.success(result);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                String error = retrofitError.getLocalizedMessage();
                try {
                    Gson gson = new Gson();
                    Type type = new TypeToken<ApiResponse<String>>() {
                    }.getType();
                    ApiResponse<String> errorObj = gson.fromJson(error, type);
                    mSettings.setServerTimeOffset(errorObj.timestamp - System.currentTimeMillis());

                    error = errorObj.data;
                } catch (Exception ex) {
                    // Do nothing...
                }

                Logger.e("Device provisioning failed with error: " + error);
                if (callback != null) callback.failure(error);
            }
        });
    }


    protected void onDeviceSighting(IDeviceSighting deviceSighting) {
        if (mSightingListener != null) {
            mSightingListener.onDeviceSighting(deviceSighting);
        }
    }

    protected void onLocationChanged(GPSLocation location) {
        if (mLocationListener != null) {
            mLocationListener.onLocationChanged(location);
        }
    }

    protected void setKeepServiceAliveAlarm() {
        Intent i = new Intent(mContext, IVigilateServiceController.class);
        if (PendingIntent.getBroadcast(mContext, 0, i, PendingIntent.FLAG_NO_CREATE) == null) {

            mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            //Start service and alarmManager to make sure service is always running
            Intent intentService = new Intent(mContext, IVigilateServiceController.class);
            mPendingIntentService = PendingIntent.getBroadcast(mContext, 0, intentService, PendingIntent.FLAG_UPDATE_CURRENT);

            mAlarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP, 0, INTERVAL_CHECK_SERVICE_ALIVE,
                    mPendingIntentService);
        }
    }

    protected void cancelKeepServiceAliveAlarm() {
        if (mPendingIntentService != null) {
            if (mAlarmManager != null) mAlarmManager.cancel(mPendingIntentService);
            mPendingIntentService.cancel();
        }
        if (IVigilateServiceController.getServiceIntent() != null &&
                IVigilateServiceController.isServiceRunning(mContext, IVigilateService.class)) {
            mContext.stopService(IVigilateServiceController.getServiceIntent());
            mContext.unbindService(mServiceConn);
        }
    }


}
