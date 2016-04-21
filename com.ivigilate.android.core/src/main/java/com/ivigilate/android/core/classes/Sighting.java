package com.ivigilate.android.core.classes;

public class Sighting {
    public long timestamp;
    public String detector_uid;
    public int detector_battery;
    public String beacon_uid;
    public int beacon_battery;
    public int rssi;
    public GPSLocation location;
    public String metadata;

    public Sighting() {
        this.timestamp = System.currentTimeMillis();
    }

    public Sighting(long timestamp, String detector_uid, int detector_battery, String beacon_uid, int beacon_battery, int rssi, GPSLocation location){
        this();
        this.timestamp = timestamp;
        this.detector_uid = detector_uid;
        this.detector_battery = detector_battery;
        this.beacon_uid = beacon_uid;
        this.beacon_battery = beacon_battery;
        this.rssi = rssi;
        this.location = location;
    }
}
