package com.ivigilate.android.library.classes;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.ivigilate.android.library.interfaces.IDeviceSighting;
import com.ivigilate.android.library.interfaces.ISighting;
import com.ivigilate.android.library.utils.StringUtils;

public class Sighting {

    public enum Type {
        @SerializedName("AC")
        AutoClosing,
        @SerializedName("MC")
        ManualClosing,
        @SerializedName("GPS")
        GPS;
    }

    public enum Status {
        @SerializedName("N")
        Normal("N"),
        @SerializedName("P")
        Panic("P"),
        @SerializedName("F")
        Fall("F");

        private String key;

        Status (String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    private long timestamp;
    private Type type;
    private String detector_uid;
    private int detector_battery;
    private String beacon_mac;
    private String beacon_uid;
    private int beacon_battery;
    private int rssi;
    private GPSLocation location;
    private String metadata;
    private boolean is_active;

    public Sighting() {
        this.timestamp = System.currentTimeMillis();
        this.is_active = true;
    }

    public Sighting(long timestamp, Type type, String detector_uid, int detector_battery, String beacon_mac, String uuid, int beacon_battery, int rssi, GPSLocation location, JsonObject metadata){
        this();
        this.timestamp = timestamp;
        this.type = type;
        this.detector_uid = detector_uid;
        this.detector_battery = detector_battery;
        this.beacon_mac = beacon_mac != null ? beacon_mac.toLowerCase().replace(":", "") : "";
        this.beacon_uid = uuid != null ? uuid.toLowerCase().replace("-", "") : "";
        this.beacon_battery = beacon_battery;
        this.rssi = rssi;
        this.location = location;

        Gson gson = new Gson();
        this.metadata = gson.toJson(metadata);
    }

    public String getKey() {
        return beacon_mac + beacon_uid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getDetectorUid() {
        return detector_uid;
    }

    public void setDetectorUid(String detectorUid) {
        this.detector_uid = detectorUid;
    }

    public int getDetectorBattery() {
        return detector_battery;
    }

    public void setDetectorBattery(int detectorBattery) {
        this.detector_battery = detectorBattery;
    }

    public String getBeaconMac() {
        return beacon_mac;
    }

    public void setBeaconMac(String beaconMac) {
        this.beacon_mac = beaconMac;
    }

    public String getBeaconUid() {
        return beacon_uid;
    }

    public void setBeaconUid(String beaconUid) {
        this.beacon_uid = beaconUid;
    }

    public int getBeaconBattery() {
        return beacon_battery;
    }

    public void setBeaconBattery(int beaconBattery) {
        this.beacon_battery = beaconBattery;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public GPSLocation getLocation() {
        return location;
    }

    public void setLocation(GPSLocation location) {
        this.location = location;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public boolean isActive() {
        return is_active;
    }

    public void setActive(boolean isActive) {
        this.is_active = isActive;
    }

    @Override
    public boolean equals(Object sighting){
        return this.getKey().equals(((Sighting)sighting).getKey());
    }
}
