package com.ivigilate.android.classes;

public class Sighting {
    public String mac;
    public String uuid;
    public int rssi;

    public Sighting (String mac, String uuid, int rssi) {
        this.mac = mac.toLowerCase();
        this.uuid = uuid.contains("0000") ? "" : uuid.toLowerCase(); // What are the odds of a uuid containing 4 0's in a row?
        this.rssi = rssi;
    }

    public String getKey () {
        return mac + (uuid.length() > 0 ? "|" + uuid : "");
    }
}
