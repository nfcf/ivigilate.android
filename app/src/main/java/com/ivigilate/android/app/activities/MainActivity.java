package com.ivigilate.android.app.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.ivigilate.android.app.AppContext;
import com.ivigilate.android.app.R;
import com.ivigilate.android.app.classes.SightingAdapter;
import com.ivigilate.android.app.utils.Logger;
import com.ivigilate.android.library.IVigilateManager;
import com.ivigilate.android.library.classes.Beacon;
import com.ivigilate.android.library.classes.Detector;
import com.ivigilate.android.library.classes.DeviceProvisioning;
import com.ivigilate.android.library.classes.DeviceSighting;
import com.ivigilate.android.library.interfaces.ISightingListener;
import com.ivigilate.android.library.interfaces.IVigilateApiCallback;

import java.util.LinkedHashMap;
import java.util.List;

public class MainActivity extends BaseActivity {
    // UI references.
    private RelativeLayout mRlSightings;
    private ImageView mIvLogout;
    private TextView mTvEmptySightings;

    private ListView mLvSightings;

    private Button mBtnStartStop;

    private SightingAdapter mSightingAdapter;
    private LinkedHashMap<String, DeviceSighting> mSightings;

    private boolean isScanning;

    private DeviceProvisioning.Type mSelectedType;
    private DeviceProvisioning.UUID mSelectedUUID;
    private DeviceSighting mCurrDeviceSighting;
    private EditText mEtDeviceName;

    private int typeIconId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.d("Started...");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        getIVigilateManager().startService();

        mSightings = new LinkedHashMap<String, DeviceSighting>();

        downloadBeacons();

        downloadDetectors();

        bindControls();

        showHideViews();

        checkRequiredPermissions();

        Logger.d("Finished.");
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkRequiredEnabledFeatures();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private IVigilateManager getIVigilateManager() {
        return ((AppContext) getApplicationContext()).getIVigilateManager();
    }

    private void downloadBeacons(){
        getIVigilateManager().getBeacons(new IVigilateApiCallback<List<Beacon>>() {
            @Override
            public void success(List<Beacon> beacons) {
                runToastOnUIThread("Success getting Beacons", true);
                //for(Beacon beacon : beacons){}
            }

            @Override
            public void failure(String errorMsg) {
                runToastOnUIThread("Failure getting Beacons", true);
            }
        });
    }

    private void downloadDetectors(){
        getIVigilateManager().getDetectors(new IVigilateApiCallback<List<Detector>>() {
            @Override
            public void success(List<Detector> detectors) {
                runToastOnUIThread("Success getting Detectors", true);
            }

            @Override
            public void failure(String errorMsg) {
                runToastOnUIThread("Failure getting Detectors", true);
            }
        });
    }

    private void bindControls() {

        mRlSightings = (RelativeLayout) findViewById(R.id.layout);
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

                                    showHideViews();
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
                mCurrDeviceSighting = (DeviceSighting) parent.getItemAtPosition(position);
                showProvisionDialog();

            }
        });
    }

    private void showHideViews() {
        mIvLogout.setVisibility(View.VISIBLE);
        mBtnStartStop.setVisibility(View.VISIBLE);
        mLvSightings.setVisibility(View.VISIBLE);
        mTvEmptySightings.setVisibility(mSightings.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showProvisionDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.provision_dialog, null);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setTitle("Provision Device");
        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                provisionDevice();
                dialog.dismiss();
            }
        });

        dialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        populateSpinners(dialogView);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void populateSpinners(View dialogView) {
        Spinner spType = (Spinner) dialogView.findViewById(R.id.spinnerType);

        ArrayAdapter<DeviceProvisioning.Type> typeArrayAdapter =
                new ArrayAdapter<DeviceProvisioning.Type>(dialogView.getContext()
                        , android.R.layout.simple_spinner_item
                        , DeviceProvisioning.Type.values());

        typeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeArrayAdapter);
        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedType = (DeviceProvisioning.Type) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner spUUID = (Spinner) dialogView.findViewById(R.id.spinnerUUID);

        ArrayAdapter<DeviceProvisioning.UUID> uuidArrayAdapter =
                new ArrayAdapter<DeviceProvisioning.UUID>(dialogView.getContext()
                        , android.R.layout.simple_spinner_dropdown_item
                        , DeviceProvisioning.UUID.values());

        typeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUUID.setAdapter(uuidArrayAdapter);
        spUUID.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedUUID = (DeviceProvisioning.UUID) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mEtDeviceName = (EditText) dialogView.findViewById(R.id.etDialogName);
    }

    private void provisionDevice() {

        runToastOnUIThread("Provisioning device...", false);

        JsonObject metadata = new JsonObject();
        JsonObject device = new JsonObject();
        device.addProperty("manufacturer", mCurrDeviceSighting.getManufacturer());
        metadata.add("device", device);

        String currentUUID = "";
        switch (mSelectedUUID) {
            case UUID:
                currentUUID = mCurrDeviceSighting.getMac();
                break;
            case MAC:
            default:
                currentUUID = mCurrDeviceSighting.getUUID();
                break;
        }

        switch(mSelectedType){
            case BeaconMovable:
                typeIconId = R.drawable.bm_icon;
                break;
            case DetectorFixed:
                typeIconId = R.drawable.df_icon;
                break;
            case DetectorMovable:
                typeIconId = R.drawable.dm_icon;
                break;
            case DetectorUser:
                typeIconId = R.drawable.du_icon;
                break;
            case BeaconFixed:
            default:
                typeIconId = R.drawable.bf_icon;
                break;
        }

        DeviceProvisioning d = new DeviceProvisioning(mSelectedType
                , currentUUID
                , mEtDeviceName.getText().toString()
                , metadata);

        getIVigilateManager().provisionDevice(d, new IVigilateApiCallback<Void>() {
            @Override
            public void success(Void data) {
                runToastOnUIThread("Success!", true);
                mRlSightings.setBackgroundColor(Color.parseColor("#D3FFCE"));
                ImageView ivTypeIcon = (ImageView) findViewById(R.id.ivTypeIcon);
                ivTypeIcon.setImageResource(typeIconId);
            }

            @Override
            public void failure(String errorMsg) {
                // GA 2016-05-09 - I think this error message should be reviewed. It's too long...
                runToastOnUIThread(errorMsg, true);
            }
        });
    }

    private void runToastOnUIThread(final String toastText, final boolean isLong) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getApplicationContext(), toastText, isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}

