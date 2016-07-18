package com.ivigilate.android.library.classes;

import com.ivigilate.android.library.interfaces.IDeviceSighting;

/**
 * Created by joanaPeixoto on 28-Jun-16.
 * Scanned barcode or QR Code sightings
 */
public class ScanSighting implements IDeviceSighting{

    private String mScanContent;
    private String mScanFormat;


    private String mDeviceName;

    public ScanSighting(String scanContent, String scanFormat) {
        mScanContent = scanContent;
        mScanFormat = scanFormat;
    }


    @Override
    public String getMac() {
        return "";
    }

    @Override
    public void setDeviceName(String deviceName) {
        mDeviceName = deviceName;
    }

    @Override
    public String getName() {
        return mDeviceName;
    }

    @Override
    public String getManufacturer() {
        return null;
    }

    @Override
    public String getUUID() {
        return mScanContent;
    }

    @Override
    public String getData() {
        return "";
    }

    @Override
    public String getPayload() {
        return "";
    }

    @Override
    public int getRssi() {
        return 0;
    }

    @Override
    public void setRssi(int rssi) {
        //not needed
    }

    @Override
    public int getBattery() {
        return 0;
    }

    @Override
    public String getType() {
        return mScanFormat;
    }

    @Override
    public Sighting.Status getStatus() {
        return Sighting.Status.Normal;
    }
}
