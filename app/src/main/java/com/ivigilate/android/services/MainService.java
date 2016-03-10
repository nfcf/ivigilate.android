package com.ivigilate.android.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.ivigilate.android.AppContext;
import com.ivigilate.android.BuildConfig;
import com.ivigilate.android.R;
import com.ivigilate.android.activities.MainActivity;
import com.ivigilate.android.classes.GPSLocation;
import com.ivigilate.android.classes.Rest;
import com.ivigilate.android.interfaces.IVigilateApi;
import com.ivigilate.android.classes.Sighting;
import com.ivigilate.android.classes.User;
import com.ivigilate.android.utils.Logger;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
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
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, BeaconConsumer {
    private static String REGION_ID = "com.ivigilate.region";

    private GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected Location mLastKnownLocation;

    private BeaconManager mBeaconManager;

    private User mUser;

    private BlockingDeque<Sighting> mDeque;
    private Thread mApiThread;
    private boolean mAbortApiThread;

    private PowerManager.WakeLock mWakeLock;
    private WifiManager.WifiLock mWifiLock;

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

        acquireLocks();

        Logger.i("Finished...");
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Logger.i("Service is now connected to Google API.");
        if (mLastKnownLocation == null) {
            mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Logger.i("GPSLocation updates have been activated.");
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
                        handleSighting(beacon);
                    }
                }
            }
        });

        try {
            mBeaconManager.startRangingBeaconsInRegion(new Region(REGION_ID, null, null, null));
        } catch (RemoteException e) {
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d("Started...");

        Logger.d("Connecting to Google API...");
        mGoogleApiClient.connect();

        Logger.d("Starting ApiThread...");
        final AppContext appContext = (AppContext) getApplication();
        mUser = appContext.settings.getUser();
        mDeque = new LinkedBlockingDeque<Sighting>();
        mAbortApiThread = false;
        mApiThread = new Thread(new Runnable() {
            public void run() {
                try {
                    Logger.d("ApiThread started...");
                    String serverAddress = BuildConfig.DEBUG ? appContext.settings.getDebugServerAddress() : AppContext.SERVER_BASE_URL;
                    RestAdapter restAdapter = Rest.createAdapter(getApplicationContext(), serverAddress);
                    IVigilateApi api = restAdapter.create(IVigilateApi.class);
                    Logger.i("ApiThread is up and running.");

                    while (!mAbortApiThread) {
                        final List<Sighting> sightings = new ArrayList<Sighting>();
                        for (int i = 0; i < 100; i++) {
                            if (mDeque.isEmpty()) break;
                            else sightings.add(mDeque.takeFirst());
                        }
                        if (sightings.size() > 0) {
                            api.addSightings(sightings, new Callback<String>() {
                                @Override
                                public void success(String result, Response response) {
                                    Logger.i("ApiThread sent " + sightings.size() + " sighting(s) successfully.");
                                }

                                @Override
                                public void failure(RetrofitError retrofitError) {
                                    if (retrofitError.getResponse() != null) {
                                        String errorMsg = new String(((TypedByteArray) retrofitError.getResponse().getBody()).getBytes());
                                        Logger.e("ApiThread failed to send sighting: " + errorMsg);
                                    } else {
                                        Logger.e("ApiThread failed to send sighting: " + retrofitError.getKind().toString() + " - " + retrofitError.getMessage());
                                    }
                                }
                            });
                        }
                        try {
                            Thread.sleep(1000);
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

        runAsForeground();

        Logger.i("Finished. Service is up and running.");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Logger.d("Started.");
        mAbortApiThread = true;

        Logger.d("Release CPU and Wifi locks...");
        releaseLocks();

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

        stopForeground(true);

        super.onDestroy();
        Logger.i("Finished.");
    }

    private void acquireLocks() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelock");
        if(!mWakeLock.isHeld()){
            mWakeLock.acquire();
        }

        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "MyWifiLock");
        if(!mWifiLock.isHeld()){
            mWifiLock.acquire();
        }
    }

    private void releaseLocks() {
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

    protected synchronized void buildGoogleApiAndLocationRequest() {
        Logger.d("Started...");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setSmallestDisplacement(5);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        Logger.i("Finished.");
    }

    private void runAsForeground() {
        Logger.d("Started...");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(getResources().getColor(R.color.material_deep_teal_500))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_text))
                .setContentIntent(pendingIntent).build();

        /*RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification);
        contentView.setImageViewResource(R.id.notification_image, R.drawable.ic_notification);
        contentView.setTextViewText(R.id.notification_title, getString(R.string.app_name));
        contentView.setTextViewText(R.id.notification_text, getString(R.string.notification_service_is_enabled));
        contentView.setImageViewResource(R.id.notification_button,
                settings.getServiceEnabled() ? R.drawable.ic_widget_service_on : R.drawable.ic_widget_service_off);

        Intent startStopServiceIntent = new Intent(this, StartStopServiceListener.class);
        startStopServiceIntent.putExtra(StartStopServiceListener.INTENT_EXTRA_TOGGLE, true);
        PendingIntent pendingStartStopServiceIntent = PendingIntent.getBroadcast(this, 0, startStopServiceIntent, 0);

        contentView.setOnClickPendingIntent(R.id.notification_button, pendingStartStopServiceIntent);

        notification.contentView = contentView;*/
        startForeground(AppContext.NOTIFICATION_ID, notification);
        Logger.i("Finished.");
    }

    private void handleSighting(Beacon beacon) {
        try {
            String beacon_uid = beacon.getId1().toString().replace("-", "");
            int power = beacon.getTxPower();
            int battery = beacon.getDataFields().get(0).intValue();
            int rssi = beacon.getRssi();
            if (mUser != null && mDeque != null) { // This should never be null but just making sure...
                Sighting previous_item = !mDeque.isEmpty() ? mDeque.peekLast() : new Sighting();
                final long now = System.currentTimeMillis();
                if (!beacon_uid.equalsIgnoreCase(previous_item.beacon_uid) ||
                        (beacon_uid.equalsIgnoreCase(previous_item.beacon_uid) &&
                                (now - previous_item.timestamp) >= 1000)) {
                    Logger.i("Sighted beacon: %s,%s,%s,%s", beacon_uid, power, battery, rssi);

                    mDeque.putLast(new Sighting(mUser.company_id, mUser.email, beacon_uid, rssi, battery,
                            mLastKnownLocation != null ? new GPSLocation(mLastKnownLocation.getLongitude(), mLastKnownLocation.getLatitude()) : null));
                } else {
                    Logger.d("Skipping packet as a similar one happened less than 1 second ago.");
                }
            }
        } catch (Exception ex) {
            Logger.e("Failed with exception: %s", ex.getMessage());
        }
    }
}
