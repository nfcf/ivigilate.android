package com.ivigilate.android.core.interfaces;

import com.ivigilate.android.core.classes.GPSLocation;

public interface ILocationListener {

    void onLocationChanged(GPSLocation location);

}
