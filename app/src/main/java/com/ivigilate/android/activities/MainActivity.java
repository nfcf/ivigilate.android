package com.ivigilate.android.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.ivigilate.android.AppContext;
import com.ivigilate.android.R;
import com.ivigilate.android.utils.Logger;

public class MainActivity extends ActionBarActivity {

    /*private Switch mSwitchServiceEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.d("Started...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        final AppContext appContext = ((AppContext) getApplication());

        mSwitchServiceEnabled = (Switch) findViewById(R.id.switchServiceEnabled);
        mSwitchServiceEnabled.setChecked(appContext.settings.getServiceEnabled());
        mSwitchServiceEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean isServiceEnabled = appContext.settings.getServiceEnabled();
                if (isChecked) {
                    if (!isServiceEnabled) {
                        if (isBluetoothEnabled()) {
                            appContext.startService();
                        } else {
                            mSwitchServiceEnabled.setChecked(isServiceEnabled);
                        }
                    }
                } else {
                    if (isServiceEnabled) {
                        appContext.stopService();
                    }
                }
            }
        });
        Logger.d("Finished.");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button_rounded_corners, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_website) {
            Logger.d("Opening website...");
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(AppContext.SERVER_BASE_URL));
            startActivity(i);
        } else if (id == R.id.action_logout) {
            Logger.d("Logging out...");
            AppContext appContext = ((AppContext) getApplication());
            appContext.stopService();
            appContext.settings.setUser(null);
            gotoLoginActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isBluetoothEnabled() {
        Logger.d("Checking if bluetooth is enabled...");
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
            return false;
        } else {
            return true;
        }
    }

    private void gotoLoginActivity() {
        Logger.d("Replacing current activity...");
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    */
}
