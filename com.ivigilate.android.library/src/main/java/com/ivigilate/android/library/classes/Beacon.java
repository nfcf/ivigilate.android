package com.ivigilate.android.library.classes;

/**
 * Created by GoAlves on 11/05/2016.
 */
public class Beacon {

    private String id;
    private String name;
    private String type;
    private int battery;
    private boolean active;
    private String uid;

    public Beacon(){}

    public Beacon(String id, String name, String type, int battery, boolean active, String uuid){
        this.id = id;
        this.name = name;
        this.type = type;
        this.battery = battery;
        this.active = active;
        this.uid = uuid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
