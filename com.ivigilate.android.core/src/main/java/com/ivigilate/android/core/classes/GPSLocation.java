package com.ivigilate.android.core.classes;

public class GPSLocation {
    public String type;
    public double[] coordinates;

    public GPSLocation() { }

    public GPSLocation(double longitude, double latitude, double altitude){
        this.type = "Point";
        this.coordinates = new double[2];
        this.coordinates[0] = longitude;
        this.coordinates[1] = latitude;
        //this.coordinates[2] = altitude;
    }

    public double getLongitude() {
        return this.coordinates[0];
    }

    public double getLatitude() {
        return this.coordinates[1];
    }
}
