package com.ivigilate.android.library.interfaces;

import com.ivigilate.android.library.classes.Sighting;

/**
 * Created by joanaPeixoto on 14-Jun-16.
 */
public interface IDeviceSighting extends ISighting{

    public int getRssi();

    public void setRssi(int rssi);

    public String getManufacturer();

    public String getPayload();

    public Sighting.Status getStatus();


}
