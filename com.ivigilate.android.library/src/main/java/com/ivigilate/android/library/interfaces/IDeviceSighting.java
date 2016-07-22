package com.ivigilate.android.library.interfaces;

import com.ivigilate.android.library.classes.Sighting;

/**
 * Created by joanaPeixoto on 14-Jun-16.
 */
public interface IDeviceSighting extends ISighting{

    public String getMac();

    public void setDeviceName(String deviceName);

    public String getName();

    public String getManufacturer();

    public String getData();

    public String getPayload();

    public int getBattery();

    public Sighting.Status getStatus();


}
