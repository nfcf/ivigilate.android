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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.ivigilate.android.app.AppContext;
import com.ivigilate.android.app.R;
import com.ivigilate.android.app.classes.DeviceSightingEx;
import com.ivigilate.android.app.classes.SightingAdapter;
import com.ivigilate.android.app.utils.Logger;
import com.ivigilate.android.library.IVigilateManager;
import com.ivigilate.android.library.classes.Device;
import com.ivigilate.android.library.classes.DeviceProvisioning;
import com.ivigilate.android.library.classes.DeviceSighting;
import com.ivigilate.android.library.interfaces.IDeviceSighting;
import com.ivigilate.android.library.interfaces.ISightingListener;
import com.ivigilate.android.library.interfaces.IVigilateApiCallback;
import com.ivigilate.android.library.utils.StringUtils;

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
    private LinkedHashMap<String, DeviceSightingEx> mSightings;
    private HashMap<String, Device> mProvisionedDevices;

    private boolean isScanning;

    private DeviceProvisioning.DeviceType mSelectedDeviceType;
    private DeviceProvisioning.IdentifierType mSelectedIdentifierType;
    private DeviceSightingEx mCurrentDeviceSighting;
    private EditText mEtDeviceName;
    private Switch mSwitchIsActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.d("Started...");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        getIVigilateManager().startService();

        mSightings = new LinkedHashMap<String, DeviceSightingEx>();

        mProvisionedDevices = new HashMap<String, Device>();

        if (getIVigilateManager().getUser() != null) {
            downloadBeacons();
            downloadDetectors();
        }

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
        getIVigilateManager().getBeacons(new IVigilateApiCallback<List<Device>>() {
            @Override
            public void success(List<Device> devices) {
                for(Device device : devices){
                    switch(device.getType()) {
                        case "M":
                            device.setDeviceType(DeviceProvisioning.DeviceType.BeaconMovable);
                            break;
                        case "F":
                        default:
                            device.setDeviceType(DeviceProvisioning.DeviceType.BeaconFixed);
                            break;
                    }
                    mProvisionedDevices.put(device.getUid().toUpperCase(), device);
                }

            }

            @Override
            public void failure(String errorMsg) {
                runToastOnUIThread("Failure getting Beacons " + errorMsg, true);
            }
        });
    }

    private void downloadDetectors(){
        getIVigilateManager().getDetectors(new IVigilateApiCallback<List<Device>>() {
            @Override
            public void success(List<Device> devices) {
                for(Device device : devices) {
                    switch(device.getType()) {
                        case "U":
                            device.setDeviceType(DeviceProvisioning.DeviceType.DetectorUser);
                            break;
                        case "M":
                            device.setDeviceType(DeviceProvisioning.DeviceType.DetectorMovable);
                            break;
                        case "F":
                        default:
                            device.setDeviceType(DeviceProvisioning.DeviceType.DetectorFixed);
                            break;
                    }
                    mProvisionedDevices.put(device.getUid().toUpperCase(), device);
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
                        public void onDeviceSighting(final IDeviceSighting deviceSighting) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DeviceSightingEx sighting = new DeviceSightingEx(deviceSighting);
                                    String key = sighting.getMac() + "|" + sighting.getUUID();

                                    checkSighting(sighting);
                                    mSightings.put(key, sighting);

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
                mCurrentDeviceSighting = (DeviceSightingEx) parent.getItemAtPosition(position);

                if (getIVigilateManager().getUser() != null) {
                    showProvisionDialog();
                } else {
                    runToastOnUIThread("Need to be logged in to be able to provision devices.", true);
                }
            }
        });
    }

    private void checkSighting(DeviceSightingEx deviceSighting) {
        String uuid = deviceSighting.getUUID();
        String mac = deviceSighting.getMac();

        if(mProvisionedDevices.containsKey(mac)){  // MAC takes precedence
            Device provisionedDevice = mProvisionedDevices.get(mac);

            deviceSighting.setDeviceProvisioned(true);
            deviceSighting.setDeviceActive(provisionedDevice.isActive());
            deviceSighting.setDeviceName(provisionedDevice.getName());
            deviceSighting.setDeviceType(provisionedDevice.getDeviceType());
            deviceSighting.setTypeIconId(getIconByType(provisionedDevice.getDeviceType()));
            deviceSighting.setIdentifierType(DeviceProvisioning.IdentifierType.MAC);
        } else if(mProvisionedDevices.containsKey(uuid)){
            Device provisionedDevice = mProvisionedDevices.get(uuid);

            deviceSighting.setDeviceProvisioned(true);
            deviceSighting.setDeviceActive(provisionedDevice.isActive());
            deviceSighting.setDeviceName(provisionedDevice.getName());
            deviceSighting.setDeviceType(provisionedDevice.getDeviceType());
            deviceSighting.setTypeIconId(getIconByType(provisionedDevice.getDeviceType()));
            deviceSighting.setIdentifierType(DeviceProvisioning.IdentifierType.UUID);
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

        ArrayAdapter<DeviceProvisioning.DeviceType> typeArrayAdapter =
                new ArrayAdapter<DeviceProvisioning.DeviceType>(dialogView.getContext()
                        , android.R.layout.simple_spinner_item
                        , DeviceProvisioning.DeviceType.values());

        typeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeArrayAdapter);
        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedDeviceType = (DeviceProvisioning.DeviceType) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner spIdentifierType = (Spinner) dialogView.findViewById(R.id.spinnerIdentifierType);

        ArrayAdapter<DeviceProvisioning.IdentifierType> uuidArrayAdapter =
                new ArrayAdapter<DeviceProvisioning.IdentifierType>(dialogView.getContext()
                        , android.R.layout.simple_spinner_dropdown_item
                        , DeviceProvisioning.IdentifierType.values());

        typeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spIdentifierType.setAdapter(uuidArrayAdapter);
        spIdentifierType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedIdentifierType = (DeviceProvisioning.IdentifierType) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mEtDeviceName = (EditText) dialogView.findViewById(R.id.etDialogName);
        mEtDeviceName.setText(mCurrentDeviceSighting.getName());

        mSwitchIsActive = (Switch) dialogView.findViewById(R.id.switchIsActive);
        mSwitchIsActive.setChecked(mCurrentDeviceSighting.isDeviceActive());

        if(mCurrentDeviceSighting.isDeviceProvisioned()) {
            spType.setSelection(DeviceProvisioning.DeviceType.valueOf(mCurrentDeviceSighting.getDeviceType().name()).ordinal());
            spIdentifierType.setSelection(DeviceProvisioning.IdentifierType.valueOf(mCurrentDeviceSighting.getIdentifierType().name()).ordinal());

            spIdentifierType.setEnabled(false);
        } else {
            if (StringUtils.isNullOrBlank(mCurrentDeviceSighting.getMac())) {
                spIdentifierType.setSelection(DeviceProvisioning.IdentifierType.UUID.ordinal());
                spIdentifierType.setEnabled(false);
            } else if (StringUtils.isNullOrBlank(mCurrentDeviceSighting.getMac())) {
                spIdentifierType.setSelection(DeviceProvisioning.IdentifierType.MAC.ordinal());
                spIdentifierType.setEnabled(false);
            } else {
                spIdentifierType.setEnabled(true);
            }
        }
    }

    private void provisionDevice() {

        runToastOnUIThread("Provisioning device...", false);

        JsonObject metadata = new JsonObject();
        JsonObject device = new JsonObject();
        device.addProperty("manufacturer", mCurrentDeviceSighting.getManufacturer());
        metadata.add("device", device);

        String uid = "";
        switch (mSelectedIdentifierType) {
            case UUID:
                uid = mCurrentDeviceSighting.getUUID();
                break;
            case MAC:
            default:
                uid = mCurrentDeviceSighting.getMac();
                break;
        }

        DeviceProvisioning d = new DeviceProvisioning(mSelectedDeviceType
                , uid
                , mEtDeviceName.getText().toString()
                , mSwitchIsActive.isChecked()
                , metadata);

        getIVigilateManager().provisionDevice(d, new IVigilateApiCallback<String>() {
            @Override
            public void success(String resultMessage) {
                // Update internal list of provisioned devices
                String uid = mSelectedIdentifierType == DeviceProvisioning.IdentifierType.MAC ?
                        mCurrentDeviceSighting.getMac() : mCurrentDeviceSighting.getUUID();
                Device updatedDevice = mProvisionedDevices.get(uid);
                if (updatedDevice != null) {
                    updatedDevice.setDeviceType(mSelectedDeviceType);
                    updatedDevice.setName(mEtDeviceName.getText().toString());
                    updatedDevice.setIsActive(mSwitchIsActive.isChecked());

                    mProvisionedDevices.put(updatedDevice.getUid(), updatedDevice);
                } else {
                    Device newDevice = new Device(mEtDeviceName.getText().toString()
                            , mSelectedDeviceType
                            , mCurrentDeviceSighting.getBattery()
                            , mSwitchIsActive.isChecked()
                            , uid);

                    mProvisionedDevices.put(newDevice.getUid(), newDevice);
                }

                // Update UI list of sightings
                mCurrentDeviceSighting.setDeviceType(mSelectedDeviceType);
                mCurrentDeviceSighting.setIdentifierType(mSelectedIdentifierType);
                mCurrentDeviceSighting.setDeviceName(mEtDeviceName.getText().toString());
                mCurrentDeviceSighting.setDeviceProvisioned(true);
                mCurrentDeviceSighting.setDeviceActive(mSwitchIsActive.isChecked());
                mCurrentDeviceSighting.setTypeIconId(getIconByType(mSelectedDeviceType));

                String key = mCurrentDeviceSighting.getMac() + "|" + mCurrentDeviceSighting.getUUID();
                //Overwrite the previous entry on the sightings map
                mSightings.put(key, mCurrentDeviceSighting);



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

    private int getIconByType(DeviceProvisioning.DeviceType deviceType) {
        int typeIconId = R.drawable.bf_icon;
        switch(deviceType){
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
        return typeIconId;
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

