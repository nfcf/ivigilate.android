package com.ivigilate.android.library.classes;

import com.ivigilate.android.library.utils.StringUtils;

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
    private DeviceProvisioning.Type dpType;

    public Beacon(){}

    public Beacon(String id, String name, String type, int battery, boolean active, String uuid){
        this.id = id;
        this.name = name;
        this.type = type;
        this.battery = battery;
        this.active = active;
        this.uid = uuid;
    }

    private void convertStrToType(){
        if(!StringUtils.isNullOrBlank(type)) {
            switch (type) {
                case "F":
                    this.dpType = DeviceProvisioning.Type.BeaconFixed;
                    break;
                case "M":
                default:
                    this.dpType = DeviceProvisioning.Type.BeaconMovable;
                    break;
            }
        }else{
            this.dpType = DeviceProvisioning.Type.BeaconMovable;
        }
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

    public DeviceProvisioning.Type getDpType() {
        if(dpType == null){
            convertStrToType();
        }
        return dpType;
    }

    public void setDpType(DeviceProvisioning.Type dpType) {
        this.dpType = dpType;
    }
}
