package com.ivigilate.android.classes;

public class GPSLocation {
    public String type;
    public double[] coordinates;

    public GPSLocation() { }

    public GPSLocation(double longitude, double latitude){
        this.type = "Point";
        this.coordinates = new double[2];
        this.coordinates[0] = longitude;
        this.coordinates[1] = latitude;
    }
}
