package com.ivigilate.android.app.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class MainActivity extends BaseActivity {
    // UI references.
    private ImageView mIvLogout;
    private TextView mTvEmptySightings;

    private ListView mLvSightings;

    private Button mBtnStartStop;

    private SightingAdapter mSightingAdapter;
    private LinkedHashMap<String, DeviceSighting> mSightings;
    private HashMap<String, Beacon> mDownloadedBeacons;
    private HashMap<String, Detector> mDownloadedDetectors;

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
                mDownloadedBeacons = new HashMap<String, Beacon>(beacons.size());
                for(Beacon beacon : beacons){
                    mDownloadedBeacons.put(beacon.getUid().toUpperCase(), beacon);
                }

            }

            @Override
            public void failure(String errorMsg) {
                runToastOnUIThread("Failure getting Beacons " + errorMsg, true);
            }
        });
    }

    private void downloadDetectors(){
        getIVigilateManager().getDetectors(new IVigilateApiCallback<List<Detector>>() {
            @Override
            public void success(List<Detector> detectors) {
                mDownloadedDetectors = new HashMap<String, Detector>(detectors.size());
                for(Detector detector : detectors) {
                    mDownloadedDetectors.put(detector.getUid().toUpperCase(), detector);
                }
            }

            @Override
            public void failure(String errorMsg) {
                runToastOnUIThread("Failure getting Detectors " + errorMsg, true);
            }
        });
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

                                    checkSighting(deviceSighting);
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

    private void checkSighting(DeviceSighting deviceSighting) {
        boolean found = false;

        if(mDownloadedBeacons.containsKey(deviceSighting.getUUID())){
            deviceSighting.setDeviceName(mDownloadedBeacons.get(deviceSighting.getUUID()).getName());
            deviceSighting.setProvisioned(true);
            getIconByType(mDownloadedBeacons.get(deviceSighting.getUUID()).getDpType());
            deviceSighting.setTypeIconId(typeIconId);
            deviceSighting.setDeviceType(mDownloadedBeacons.get(deviceSighting.getUUID()).getDpType().name());
            deviceSighting.setDeviceUUIDType(DeviceProvisioning.UUID.UUID.name());
            found = true;
        }else if(mDownloadedBeacons.containsKey(deviceSighting.getMac())){
            deviceSighting.setDeviceName(mDownloadedBeacons.get(deviceSighting.getMac()).getName());
            deviceSighting.setProvisioned(true);
            getIconByType(mDownloadedBeacons.get(deviceSighting.getMac()).getDpType());
            deviceSighting.setTypeIconId(typeIconId);
            deviceSighting.setDeviceType(mDownloadedBeacons.get(deviceSighting.getMac()).getDpType().name());
            deviceSighting.setDeviceUUIDType(DeviceProvisioning.UUID.MAC.name());
            found = true;
        }

        if(!found) {
            if(mDownloadedDetectors.containsKey(deviceSighting.getUUID())){
                deviceSighting.setDeviceName(mDownloadedDetectors.get(deviceSighting.getUUID()).getName());
                deviceSighting.setProvisioned(true);
                getIconByType(mDownloadedDetectors.get(deviceSighting.getUUID()).getDpType());
                deviceSighting.setTypeIconId(typeIconId);
                deviceSighting.setDeviceType(mDownloadedDetectors.get(deviceSighting.getUUID()).getDpType().name());
                deviceSighting.setDeviceUUIDType(DeviceProvisioning.UUID.UUID.name());
            }else if(mDownloadedDetectors.containsKey(deviceSighting.getMac())){
                deviceSighting.setDeviceName(mDownloadedDetectors.get(deviceSighting.getMac()).getName());
                deviceSighting.setProvisioned(true);
                getIconByType(mDownloadedDetectors.get(deviceSighting.getMac()).getDpType());
                deviceSighting.setTypeIconId(typeIconId);
                deviceSighting.setDeviceType(mDownloadedDetectors.get(deviceSighting.getMac()).getDpType().name());
                deviceSighting.setDeviceUUIDType(DeviceProvisioning.UUID.MAC.name());
            }
        }
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

        populateDialogView(dialogView);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void populateDialogView(View dialogView) {
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
        if(mCurrDeviceSighting.isProvisioned()) {
            mEtDeviceName.setText(mCurrDeviceSighting.getName());
            spType.setSelection(DeviceProvisioning.Type.valueOf(mCurrDeviceSighting.getDeviceType()).ordinal());
            spUUID.setSelection(DeviceProvisioning.UUID.valueOf(mCurrDeviceSighting.getDeviceUUIDType()).ordinal());
        }
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

        getIconByType(mSelectedType);

        DeviceProvisioning d = new DeviceProvisioning(mSelectedType
                , currentUUID
                , mEtDeviceName.getText().toString()
                , metadata);

        getIVigilateManager().provisionDevice(d, new IVigilateApiCallback<String>() {
            @Override
            public void success(String resultMessage) {
                mCurrDeviceSighting.setDeviceType(mSelectedType.name());
                mCurrDeviceSighting.setDeviceUUIDType(mSelectedUUID.name());
                mCurrDeviceSighting.setDeviceName(mEtDeviceName.getText().toString());
                mCurrDeviceSighting.setProvisioned(true);
                mCurrDeviceSighting.setTypeIconId(typeIconId);

                String key = mCurrDeviceSighting.getMac() + "|" + mCurrDeviceSighting.getUUID();
                //Overwrite the previous entry on the sightings map
                mSightings.put(key, mCurrDeviceSighting);

                //notify adapter to refresh itself
                mSightingAdapter.notifyDataSetChanged();


                runToastOnUIThread(resultMessage, true);
            }

            @Override
            public void failure(String errorMsg) {
                runToastOnUIThread(errorMsg, true);
            }
        });
    }

    private void getIconByType(DeviceProvisioning.Type type) {
        switch(type){
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

