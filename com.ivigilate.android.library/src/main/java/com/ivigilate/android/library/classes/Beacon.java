package com.ivigilate.android.library.classes;

/**
 * Created by GoAlves on 11/05/2016.
 */
public class Beacon {

    private String id;
    private String name;
    private DeviceProvisioning.Type type;
    private int battery;
    private boolean active;

    public Beacon(){}

    public Beacon(String id, String name, DeviceProvisioning.Type type, int battery, boolean active){
        this.id = id;
        this.name = name;
        this.type = type;
        this.battery = battery;
        this.active = active;
    }
}
