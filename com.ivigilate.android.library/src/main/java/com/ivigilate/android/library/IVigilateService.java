package com.ivigilate.android.library;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.ivigilate.android.library.classes.ApiResponse;
import com.ivigilate.android.library.classes.DeviceSighting;
import com.ivigilate.android.library.classes.GPSLocation;
import com.ivigilate.android.library.classes.Rest;
import com.ivigilate.android.library.classes.Sighting;
import com.ivigilate.android.library.interfaces.IVigilateApi;
import com.ivigilate.android.library.utils.Logger;
import com.ivigilate.android.library.utils.PhoneUtils;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;

import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.scanner.NonBeaconLeScanCallback;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class IVigilateService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, BeaconConsumer {

    private static String REGION_ID = "com.ivigilate.android.region";

    private GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected Location mLastKnownLocation;

    private BeaconManager mBeaconManager;
    private IVigilateManager mIVigilateManager;

    private BlockingDeque<Sighting> mDequeSightings;
    private Thread mApiThread;
    private boolean mAbortApiThread;

    private IVigilateApi mApi;

    private HashMap<String, Sighting> mActiveSightings;

    public IVigilateService() {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        Logger.d("Started...");
        super.onCreate();

        mIVigilateManager = IVigilateManager.getInstance(this);

        mActiveSightings = mIVigilateManager.getServiceActiveSightings();

        buildGoogleApiAndLocationRequest();

        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        mBeaconManager.getBeaconParsers().clear();
        //mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25")); //altBeacon
        //mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25")); //kontakt / jaalee / estimote
        //mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=6572,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25")); //forever
        //mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=ad7700c6,i:4-19,i:20-21,i:22-23,p:24-24")); //gimbal

        mBeaconManager.setForegroundScanPeriod(1100);  // default 1100
        mBeaconManager.setForegroundBetweenScanPeriod(500);  // default 0
        mBeaconManager.setBackgroundScanPeriod(2100);  //default 10000
        mBeaconManager.setBackgroundBetweenScanPeriod(1000);  // default 5 * 60 * 1000

        Logger.i("Finished...");
    }

    @Override
    public void onDestroy() {
        Logger.d("Started.");
        mAbortApiThread = true;

        Logger.d("Release CPU and Wifi locks...");
        mIVigilateManager.releaseLocks();

        Logger.d("Unbinding bluetooth manager...");
        mBeaconManager.unbind(this);

        Logger.d("Disconnecting from Google API...");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

        Logger.d("Stopping ApiThread...");
        if (mApiThread != null) {
            mApiThread.interrupt();
            try {
                mApiThread.join(15 * 1000);
            } catch (Exception ex) {
            }
        }

        super.onDestroy();
        Logger.i("Finished.");
    }

    protected synchronized void buildGoogleApiAndLocationRequest() {
        Logger.d("Started...");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(mIVigilateManager.getLocationRequestInterval());
        mLocationRequest.setFastestInterval(mIVigilateManager.getLocationRequestFastestInterval());
        mLocationRequest.setSmallestDisplacement(mIVigilateManager.getLocationRequestSmallestDisplacement());
        mLocationRequest.setPriority(mIVigilateManager.getLocationRequestPriority());
        Logger.i("Finished.");
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Logger.i("Service is now connected to Google API.");
        try {
            if (mLastKnownLocation == null) {
                mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            Logger.i("GPSLocation updates have been activated.");
        } catch (SecurityException ex) {
            Logger.e("GPSLocation updates require user permissions to be activated!");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Logger.w("Attempting to reconnect to Google API...");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Logger.e("Failed with error code: " + result.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastKnownLocation = location;

        mIVigilateManager.onLocationChanged(new GPSLocation(
                location.getLongitude(),
                location.getLatitude(),
                location.getAltitude()));
    }

    @Override
    public void onBeaconServiceConnect() {
        mBeaconManager.setNonBeaconLeScanCallback(new NonBeaconLeScanCallback() {
            @Override
            public void onNonBeaconLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
                handleNonBeaconSighting(bluetoothDevice, rssi, bytes);
            }
        });

        try {
            mBeaconManager.startRangingBeaconsInRegion(new Region(REGION_ID, null, null, null));
        } catch (RemoteException e) {
            Logger.e("Error on startRangingBeaconsInRegion(): " + e.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d("Started...");

        Logger.d("Connecting to Google API...");
        mGoogleApiClient.connect();

        Logger.d("Starting ApiThread...");
        Context context = getApplicationContext();
        mApi = Rest.createService(IVigilateApi.class, context, mIVigilateManager.getServerAddress(), mIVigilateManager.getUser() != null ? mIVigilateManager.getUser().token : "");

        mDequeSightings = new LinkedBlockingDeque<Sighting>();
        mAbortApiThread = false;
        mApiThread = new Thread(new Runnable() {
            public void run() {
                try {
                    Logger.i("ApiThread is up and running.");

                    while (!mAbortApiThread) {
                        final List<Sighting> sightings = new ArrayList<Sighting>();
                        synchronized(mDequeSightings) {
                            for (int i = 0; i < 100; i++) {
                                if (mDequeSightings.isEmpty()) break;
                                else sightings.add(mDequeSightings.takeFirst());
                            }
                        }

                        if (mActiveSightings.size() > 0) {
                            final long now = System.currentTimeMillis() + mIVigilateManager.getServerTimeOffset();
                            for (Sighting sighting : new ArrayList<Sighting>(mActiveSightings.values())) {
                                if (now - sighting.timestamp > mIVigilateManager.getServiceStateChangeInterval()) {
                                    sighting.is_active = false;
                                    sightings.add(sighting);

                                    synchronized(mActiveSightings) {
                                        mActiveSightings.remove(sighting.getKey());
                                        mIVigilateManager.setServiceActiveSightings(mActiveSightings);
                                    }
                                }
                            }
                        }

                        if (sightings.size() > 0) {
                            mApi.addSightings(sightings, new Callback<ApiResponse<Void>>() {
                                @Override
                                public void success(ApiResponse<Void> result, Response response) {
                                    Logger.i("ApiThread sent " + sightings.size() + " onDeviceSighted(s) successfully.");
                                    mIVigilateManager.setServerTimeOffset(result.timestamp - System.currentTimeMillis());
                                }

                                @Override
                                public void failure(RetrofitError retrofitError) {
                                    String error = retrofitError.getLocalizedMessage();
                                    try {
                                        Gson gson = new Gson();
                                        Type type = new TypeToken<ApiResponse<String>>() {}.getType();
                                        ApiResponse<String> errorObj = gson.fromJson(error, type);
                                        mIVigilateManager.setServerTimeOffset(errorObj.timestamp - System.currentTimeMillis());

                                        error = errorObj.data;
                                    } catch (Exception ex) {
                                        // Do nothing...
                                    }

                                    Logger.e("ApiThread failed to send Sighting(s): " + error);
                                }
                            });
                        }
                        try {
                            Thread.sleep(mIVigilateManager.getServiceSendInterval());
                        } catch (Exception ex) {
                        }
                    }
                } catch (Exception ex) {
                    Logger.e("ApiThread failed with exception: " + ex.getMessage());
                }
            }
        });
        mApiThread.start();

        Logger.d("Binding bluetooth manager...");
        mBeaconManager.bind(this);


        Logger.i("Finished. Service is up and running.");
        return START_NOT_STICKY;
    }

    private void handleNonBeaconSighting(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
        try {
            DeviceSighting deviceSighting = new DeviceSighting(bluetoothDevice, rssi, bytes);

            mIVigilateManager.onDeviceSighting(deviceSighting);

            if (mDequeSightings != null) { // This should never be null but just making sure...

                Sighting previous_item = !mDequeSightings.isEmpty() ? mDequeSightings.peekLast() : new Sighting();
                final long now = System.currentTimeMillis() + mIVigilateManager.getServerTimeOffset();

                if (!deviceSighting.getUUID().equalsIgnoreCase(previous_item.beacon_uid) ||
                        (deviceSighting.getUUID().equalsIgnoreCase(previous_item.beacon_uid) &&
                                (now - previous_item.timestamp) >= mIVigilateManager.getServiceSendInterval())) {

                    Logger.i("Sighted beacon: %s,%s,%s,%s",
                            deviceSighting.getMac(), deviceSighting.getUUID(), deviceSighting.getBattery(), rssi);

                    Context context = getApplicationContext();
                    Sighting.Type type = mIVigilateManager.getServiceStateChangeInterval() > 0 ? Sighting.Type.ManualClosing : Sighting.Type.AutoClosing;

                    Sighting sighting = new Sighting(now, type,
                            PhoneUtils.getDeviceUniqueId(context), (int)PhoneUtils.getBatteryLevel(context),
                            deviceSighting.getMac(), deviceSighting.getUUID(), deviceSighting.getBattery(), rssi,
                            mLastKnownLocation != null ? new GPSLocation(mLastKnownLocation.getLongitude(), mLastKnownLocation.getLatitude(), mLastKnownLocation.getAltitude()) : null,
                            mIVigilateManager.getServiceSightingMetadata());

                    if (type == Sighting.Type.AutoClosing || !mActiveSightings.containsKey(sighting.getKey())) {
                        synchronized(mDequeSightings) {
                            mDequeSightings.putLast(sighting); // Queue to be sent to server
                        }
                    }

                    synchronized(mActiveSightings) {
                        mActiveSightings.put(sighting.getKey(), sighting);
                        mIVigilateManager.setServiceActiveSightings(mActiveSightings);
                    }
                } else {
                    Logger.d("Averaging packet with previous similar one as it happened less than X second(s) ago.");

                    previous_item.rssi = (previous_item.rssi + rssi) / 2;
                    synchronized(mDequeSightings) {
                        mDequeSightings.takeLast();
                        mDequeSightings.putLast(previous_item);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.d("Failed to handleNonBeaconSighting with exception: " + ex.getMessage());
        }
    }

}
