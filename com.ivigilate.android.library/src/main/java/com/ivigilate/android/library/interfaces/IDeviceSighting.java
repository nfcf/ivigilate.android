package com.ivigilate.android.library.interfaces;

/**
 * Created by joanaPeixoto on 14-Jun-16.
 */
public interface IDeviceSighting {

    public String getMac();

    public void setDeviceName(String mDeviceName);

    public String getName();

    public String getManufacturer();

    public String getUUID();

    public String getData();

    public String getPayload();

    public int getRssi();

    public void setRssi(int rssi);

    public int getBattery();
}
