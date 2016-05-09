package com.ivigilate.android.app.activities;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.ivigilate.android.library.classes.DeviceProvisioning;
import com.ivigilate.android.library.classes.DeviceSighting;
import com.ivigilate.android.library.interfaces.ISightingListener;
import com.ivigilate.android.library.interfaces.IVigilateApiCallback;

import java.util.LinkedHashMap;

public class MainActivity extends BaseActivity {
    // UI references.
    private ImageView mIvLogout;
    private TextView mTvEmptySightings;

    private ListView mLvSightings;

    private Button mBtnStartStop;

    private SightingAdapter mSightingAdapter;
    private LinkedHashMap<String, DeviceSighting> mSightings;

    private boolean isScanning;

    private DeviceProvisioning.Type selectedType;
    private DeviceProvisioning.UUID selectedUUID;

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
    protected void onResume() {
        super.onResume();

        checkForRequiredEnabledFeatures();
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
                final DeviceSighting deviceSighting = (DeviceSighting) parent.getItemAtPosition(position);
                showProvisionDialog(deviceSighting);

            }
        });
    }

    private void showHideViews() {
        mIvLogout.setVisibility(View.VISIBLE);
        mBtnStartStop.setVisibility(View.VISIBLE);
        mLvSightings.setVisibility(View.VISIBLE);
        mTvEmptySightings.setVisibility(mSightings.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void checkForRequiredEnabledFeatures() {
        // Verify if location services are enabled
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!service.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable the GPS (location services).", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            return;
        }

        // Verify if Bluetooth is enabled
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
            return;
        }
    }
	
	private void showProvisionDialog(final DeviceSighting deviceSighting){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.provision_dialog, null);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setTitle("Provision Device");
        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                provisionDevice(deviceSighting);
                dialog.dismiss();
            }
        });

        dialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        populateSpinners(dialogView, deviceSighting.getName());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void populateSpinners(View dialogView, String deviceName){
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
                selectedType = (DeviceProvisioning.Type) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner spUUID = (Spinner) dialogView.findViewById(R.id.spinnerUUID);

        ArrayAdapter<DeviceProvisioning.UUID> uuidArrayAdapter =
                new ArrayAdapter<DeviceProvisioning.UUID>(dialogView.getContext()
                        , android.R.layout.simple_spinner_item
                        , DeviceProvisioning.UUID.values());

        typeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUUID.setAdapter(uuidArrayAdapter);
        spUUID.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUUID = (DeviceProvisioning.UUID) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        TextView tvDialogName = (TextView) dialogView.findViewById(R.id.tvDialogName);
        tvDialogName.setText(deviceName);
    }

    private void provisionDevice(DeviceSighting deviceSighting){

        runToastOnUIThread("Provisioning device...", false);

        JsonObject metadata = new JsonObject();
        JsonObject device = new JsonObject();
        device.addProperty("manufacturer", deviceSighting.getManufacturer());
        metadata.add("device", device);

        String currentUUID = "";
        switch(selectedUUID){
            case MAC:
                currentUUID = deviceSighting.getUUID();
                break;
            case UUID:
                currentUUID = deviceSighting.getMac();
                break;
            default:
                currentUUID = deviceSighting.getMac();
                break;
        }

        DeviceProvisioning d = new DeviceProvisioning(selectedType
                , currentUUID
                , deviceSighting.getName()
                , metadata);

        getIVigilateManager().provisionDevice(d, new IVigilateApiCallback<Void>() {
            @Override
            public void success(Void data) {
                runToastOnUIThread("Success!", true);
            }

            @Override
            public void failure(String errorMsg) {
                // GA 2016-05-09 - I think this error message should be reviewed. It's too long...
                runToastOnUIThread("Failed! Error:" + errorMsg, true);
            }
        });
    }

    private void runToastOnUIThread(final String toastText, final boolean isLong){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getApplicationContext(), toastText, isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}

