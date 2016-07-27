package com.ivigilate.android.app.classes;

import com.ivigilate.android.library.classes.BleDeviceSighting;
import com.ivigilate.android.library.classes.Sighting;
import com.ivigilate.android.library.interfaces.IDeviceSighting;
import com.ivigilate.android.library.interfaces.ISighting;

/**
 * Created by joanaPeixoto on 24-Jun-16.
 * Creates a common ground between all types of Sightings
 * so methods can be accessed as necessary
 */
public abstract class BaseSightingEx implements ISighting {

    private ISighting mSighting;


    public BaseSightingEx(ISighting sighting) {
        mSighting = sighting;
    }

    @Override
    public String getUUID() {
        return mSighting.getUUID();
    }

    @Override
    public String getType() {
        return mSighting.getType();
    }

    public String getMac() {
        String mac;
        return mac = mSighting instanceof BleDeviceSighting ? ((BleDeviceSighting) mSighting).getMac() :
                null;
    }

    public String getName() {
        String name;
        return name = mSighting instanceof BleDeviceSighting ? ((BleDeviceSighting) mSighting).getName() :
                null;
    }

    public void setDeviceName(String mDeviceName) {
        if (!(mSighting instanceof BleDeviceSighting)) {
            return;
        }
        ((BleDeviceSighting) mSighting).setDeviceName(mDeviceName);
    }

    public String getManufacturer() {
        String manufacturer;
        return manufacturer = mSighting instanceof IDeviceSighting ? ((IDeviceSighting) mSighting).getManufacturer() :
                null;
    }

    public String getData() {
        String data;
        return data = mSighting instanceof BleDeviceSighting ? ((BleDeviceSighting) mSighting).getData() :
                null;
    }

    public String getPayload() {
        String payload;
        return payload = mSighting instanceof IDeviceSighting ? ((IDeviceSighting) mSighting).getPayload() :
                null;
    }

    public int getRssi() {
        int rssi;
        return rssi = mSighting instanceof IDeviceSighting ? ((IDeviceSighting) mSighting).getRssi() :
                0;
    }

    public void setRssi(int rssi) {
        if (!(mSighting instanceof IDeviceSighting)) {
            return;
        }
        ((IDeviceSighting) mSighting).setRssi(rssi);
    }

    public int getBattery() {
        int battery;
        return battery = mSighting instanceof BleDeviceSighting ? ((BleDeviceSighting) mSighting).getBattery() :
                0;
    }

    public Sighting.Status getStatus() {
        Sighting.Status status;
        return status = mSighting instanceof IDeviceSighting ? ((IDeviceSighting) mSighting).getStatus() :
                null;
    }
}
