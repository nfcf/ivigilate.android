package com.ivigilate.android.core.services;

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
import com.ivigilate.android.core.IVigilateManager;
import com.ivigilate.android.core.classes.ApiResponse;
import com.ivigilate.android.core.classes.GPSLocation;
import com.ivigilate.android.core.classes.Rest;
import com.ivigilate.android.core.classes.Sighting;
import com.ivigilate.android.core.interfaces.IVigilateApi;
import com.ivigilate.android.core.utils.Logger;
import com.ivigilate.android.core.utils.PhoneUtils;
import com.ivigilate.android.core.utils.StringUtils;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.scanner.NonBeaconLeScanCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, BeaconConsumer {

    private static String REGION_ID = "com.ivigilate.android.core.region";

    private GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected Location mLastKnownLocation;

    private BeaconManager mBeaconManager;
    private IVigilateManager mIVigilateManager;

    private BlockingDeque<Sighting> mDequeSightings;
    private Thread mApiThread;
    private boolean mAbortApiThread;

    private IVigilateApi mApi;

    public MainService() {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        Logger.d("Started...");
        super.onCreate();

        buildGoogleApiAndLocationRequest();

        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25")); //altBeacon
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25")); //kontakt / jaalee
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=6572,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25")); //forever

        mIVigilateManager = IVigilateManager.getInstance(this);
        Logger.i("Finished...");
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
    }

    @Override
    public void onBeaconServiceConnect() {
        mBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    for (Beacon beacon : beacons) {
                        handleBeaconSighting(beacon);
                    }
                }
            }
        });

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
        RestAdapter restAdapter = Rest.createAdapter(context, mIVigilateManager.getSettings().getServerAddress());
        mApi = restAdapter.create(IVigilateApi.class);

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

                        if (sightings.size() > 0) {
                            mApi.addSightings(sightings, new Callback<ApiResponse<Void>>() {
                                @Override
                                public void success(ApiResponse<Void> result, Response response) {
                                    Logger.i("ApiThread sent " + sightings.size() + " sighting(s) successfully.");
                                    mIVigilateManager.getSettings().setServerTimeOffset(new Date(result.timestamp).getTime() - System.currentTimeMillis());
                                }

                                @Override
                                public void failure(RetrofitError retrofitError) {
                                    String errorMsg = "";
                                    if (retrofitError.getResponse() != null) {
                                        errorMsg = new String(((TypedByteArray) retrofitError.getResponse().getBody()).getBytes());
                                    } else {
                                        errorMsg = retrofitError.getKind().toString() + " - " + retrofitError.getMessage();
                                    }

                                    Logger.e("ApiThread failed to send sighting(s): " + errorMsg);
                                }
                            });
                        }
                        try {
                            Thread.sleep(mIVigilateManager.getSettings().getServiceSendInterval());
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

    @Override
    public void onDestroy() {
        Logger.d("Started.");
        mAbortApiThread = true;

        Logger.d("Release CPU and Wifi locks...");
        IVigilateManager.getInstance(getApplicationContext()).releaseLocks();

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
        mLocationRequest.setInterval(30 * 1000);
        mLocationRequest.setFastestInterval(20 * 1000);
        mLocationRequest.setSmallestDisplacement(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        Logger.i("Finished.");
    }

    private void handleBeaconSighting(Beacon beacon) {
        try {
            String mac = beacon.getBluetoothAddress();
            String uid = beacon.getId1().toString().replace("-", "");
            String beacon_uid = mac.replace(":", "") + "|" + uid;
            int power = beacon.getTxPower();
            int battery = beacon.getDataFields().get(0).intValue();
            int rssi = beacon.getRssi();

            mIVigilateManager.onDeviceSighted(mac, uid, rssi); //Event to be catched by the user application

            if (mDequeSightings != null) { // This should never be null but just making sure...

                Sighting previous_item = !mDequeSightings.isEmpty() ? mDequeSightings.peekLast() : new Sighting();
                final long now = System.currentTimeMillis() + mIVigilateManager.getSettings().getServerTimeOffset();

                if (!beacon_uid.equalsIgnoreCase(previous_item.beacon_uid) ||
                        (beacon_uid.equalsIgnoreCase(previous_item.beacon_uid) &&
                                (now - previous_item.timestamp) >= mIVigilateManager.getSettings().getServiceSendInterval())) {

                    Logger.i("Sighted beacon: %s,%s,%s,%s,%s", mac, uid, battery, power, rssi);

                    Context context = getApplicationContext();
                    mDequeSightings.putLast(new Sighting(now, PhoneUtils.getDeviceUniqueId(context), (int)PhoneUtils.getBatteryLevel(context),
                            beacon_uid, battery, rssi,
                            mLastKnownLocation != null ? new GPSLocation(mLastKnownLocation.getLongitude(), mLastKnownLocation.getLatitude(), mLastKnownLocation.getAltitude()) : null));
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
            Logger.e("Failed with exception: %s", ex.getMessage());
        }
    }

    private void handleNonBeaconSighting(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
        String mac = bluetoothDevice.getAddress();
        String payload = StringUtils.bytesToHexString(bytes);
        String manufacturer = payload.substring(14, 4);
        String uid = payload.substring(18, 32);
        Logger.d("bleDevice: " + mac);
        Logger.d("payload: " + payload);
        Logger.d("manufacturer: " + manufacturer);
        Logger.d("uid: " + uid);

        mIVigilateManager.onDeviceSighted(bluetoothDevice.getAddress(), uid, rssi);
    }
}
