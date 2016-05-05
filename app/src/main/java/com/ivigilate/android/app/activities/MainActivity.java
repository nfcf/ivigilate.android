package com.ivigilate.android.app.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.ivigilate.android.app.AppContext;
import com.ivigilate.android.app.R;
import com.ivigilate.android.app.classes.SightingAdapter;
import com.ivigilate.android.app.utils.Logger;
import com.ivigilate.android.library.IVigilateManager;
import com.ivigilate.android.library.classes.DeviceProvisioning;
import com.ivigilate.android.library.classes.DeviceSighting;
import com.ivigilate.android.library.interfaces.ISightingListener;
import com.ivigilate.android.library.interfaces.IVigilateApiCallback;

import java.util.LinkedHashMap;

public class MainActivity extends Activity {
    private static final int PERMISSIONS_REQUEST = 1;

    // UI references.
    private ImageView mIvLogout;
    private TextView mTvEmptySightings;

    private ListView mLvSightings;

    private Button mBtnStartStop;

    private SightingAdapter mSightingAdapter;
    private LinkedHashMap<String, DeviceSighting> mSightings;

    private boolean isScanning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.d("Started...");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        getIVigilateManager().startService();

        mSightings = new LinkedHashMap<String, DeviceSighting>();

        bindControls();

        showHideViews();

        checkRequiredPermissions();

        Logger.d("Finished.");
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private IVigilateManager getIVigilateManager() {
        return ((AppContext)getApplicationContext()).getIVigilateManager();
    }

    private void bindControls() {
        ImageView ivLogo = (ImageView) findViewById(R.id.ivLogo);
        ivLogo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Logger.d("Opening website...");
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(getIVigilateManager().getServerAddress()));
                startActivity(i);
            }
        });

        mIvLogout = (ImageView) findViewById(R.id.ivLogout);
        mIvLogout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Logger.d("Logging out...");
                getIVigilateManager().stopService();
                getIVigilateManager().logout(null);
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

        mBtnStartStop = (Button) findViewById(R.id.btnStartStop);

        mBtnStartStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isScanning) {
                    mBtnStartStop.setText("SCAN");
                    getIVigilateManager().setSightingListener(null);
                    mSightings.clear();
                    mTvEmptySightings.setVisibility(mSightings.isEmpty() ? View.VISIBLE : View.GONE);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSightingAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    mBtnStartStop.setText("STOP");
                    mTvEmptySightings.setVisibility(mSightings.isEmpty() ? View.VISIBLE : View.GONE);
                    getIVigilateManager().setSightingListener(new ISightingListener() {
                        @Override
                        public void onDeviceSighting(final DeviceSighting deviceSighting) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String key = deviceSighting.getMac() + "|" + deviceSighting.getUUID();

                                    mSightings.put(key, deviceSighting);

                                    mSightingAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
                }
                isScanning = !isScanning;
            }
        });

        mTvEmptySightings = (TextView) findViewById(R.id.tvEmptySightings);
        mSightingAdapter = new SightingAdapter(mSightings);
        mLvSightings = (ListView) findViewById(R.id.lvSightings);
        mLvSightings.setAdapter(mSightingAdapter);

        mLvSightings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final DeviceSighting deviceSighting = (DeviceSighting) parent.getItemAtPosition(position);
                //TODO: the toasts need to runOnUiThread...
                Toast.makeText(getApplicationContext(), "Provisioning device...", Toast.LENGTH_SHORT);

                JsonObject metadata = new JsonObject();
                JsonObject device = new JsonObject();
                device.addProperty("manufacturer", deviceSighting.getManufacturer());
                metadata.add("device", device);
                DeviceProvisioning d = new DeviceProvisioning(DeviceProvisioning.Type.BeaconMovable, deviceSighting.getMac(), "Test", metadata);

                getIVigilateManager().provisionDevice(d, new IVigilateApiCallback<Void>() {
                    @Override
                    public void success(Void data) {
                        Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT);
                    }

                    @Override
                    public void failure(String errorMsg) {
                        Toast.makeText(getApplicationContext(), "Failed! Error:" + errorMsg, Toast.LENGTH_SHORT);
                    }
                });
            }
        });
    }

    private void showHideViews() {
        mIvLogout.setVisibility(View.VISIBLE);
        mBtnStartStop.setVisibility(View.VISIBLE);
        mLvSightings.setVisibility(View.VISIBLE);
        mTvEmptySightings.setVisibility(mSightings.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Logger.d("Coarse location permission granted");
                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                } else if (grantResults[1] == PackageManager.PERMISSION_DENIED) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("This app needs to be able to access the device's IMEI to be able to function properly.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }
}

