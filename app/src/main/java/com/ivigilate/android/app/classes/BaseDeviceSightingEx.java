package com.ivigilate.android.app.classes;

import com.ivigilate.android.library.classes.Sighting;
import com.ivigilate.android.library.interfaces.IDeviceSighting;

/**
 * Created by joanaPeixoto on 24-Jun-16.
 */
public abstract class BaseDeviceSightingEx implements IDeviceSighting{

    private IDeviceSighting mDeviceSighting;


    public BaseDeviceSightingEx(IDeviceSighting deviceSighting){
        mDeviceSighting = deviceSighting;
    }

    @Override
    public String getMac() {
        return mDeviceSighting.getMac();
    }

    @Override
    public void setDeviceName(String mDeviceName) {
        mDeviceSighting.setDeviceName(mDeviceName);
    }

    @Override
    public String getName() {
        return mDeviceSighting.getName();
    }

    @Override
    public String getManufacturer() {
        return mDeviceSighting.getManufacturer();
    }

    @Override
    public String getUUID() {
        return mDeviceSighting.getUUID();
    }

    @Override
    public String getData() {
        return mDeviceSighting.getData();
    }

    @Override
    public String getPayload() {
        return mDeviceSighting.getPayload();
    }

    @Override
    public int getRssi() {
        return mDeviceSighting.getRssi();
    }

    @Override
    public void setRssi(int rssi) {
        mDeviceSighting.setRssi(rssi);
    }

    @Override
    public int getBattery() {
        return mDeviceSighting.getBattery();
    }

    @Override
    public String getType() {
        return mDeviceSighting.getType();
    }

    public Sighting.Status getStatus(){
        return mDeviceSighting.getStatus();
    }

    public String getStatus(){
        return mDeviceSighting.getStatus();
    }
}
