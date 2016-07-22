package com.ivigilate.android.library.interfaces;

import com.ivigilate.android.library.classes.GPSLocation;

/**
 * Created by joanaPeixoto on 19-Jul-16.
 */
public interface ISighting {

    public String getUUID();

    public int getRssi();

    public void setRssi(int rssi);

    public String getType();//this can be confused with Sighting.type...

}
