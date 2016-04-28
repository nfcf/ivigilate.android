package com.ivigilate.android.library.classes;

import com.google.gson.annotations.SerializedName;

public class Sighting {
    public enum Type {
        @SerializedName("A")
        AutoClosing,
        @SerializedName("M")
        ManualClosing;
    }

    public long timestamp;
    public Type type;
    public String detector_uid;
    public int detector_battery;
    public String beacon_mac;
    public String beacon_uid;
    public int beacon_battery;
    public int rssi;
    public GPSLocation location;
    public String metadata;
    public boolean is_active;

    public Sighting() {
        this.timestamp = System.currentTimeMillis();
        this.is_active = true;
    }

    public Sighting(long timestamp, Type type, String detector_uid, int detector_battery, String beacon_mac, String beacon_uid, int beacon_battery, int rssi, GPSLocation location, String metadata){
        this();
        this.timestamp = timestamp;
        this.type = type;
        this.detector_uid = detector_uid;
        this.detector_battery = detector_battery;
        this.beacon_mac = beacon_mac.toLowerCase().replace(":", "");
        this.beacon_uid = beacon_uid.toLowerCase().replace("-", "");
        this.beacon_battery = beacon_battery;
        this.rssi = rssi;
        this.location = location;
        this.metadata = metadata;
    }

    public String getKey() {
        return detector_uid + beacon_mac + beacon_uid;
    }
}
