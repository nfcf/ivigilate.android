package com.ivigilate.android.classes;

public class Sighting {
    public long timestamp;
    public String company_id;
    public String detector_uid;
    public String beacon_uid;
    public int rssi;
    public int battery;
    public GPSLocation location;

    public Sighting() {
        this.timestamp = System.currentTimeMillis();
    }

    public Sighting(String company_id, String detector_uid, String beacon_uid, int rssi, int battery, GPSLocation location){
        this();
        this.company_id = company_id;
        this.detector_uid = detector_uid;
        this.beacon_uid = beacon_uid;
        this.rssi = rssi;
        this.battery = battery;
        this.location = location;
    }
}
