package com.ivigilate.android.library.classes;

import com.google.gson.annotations.SerializedName;

public class Device {
    public enum Type {
        @SerializedName("BF")
        BeaconFixed,
        @SerializedName("BM")
        BeaconMovable,
        @SerializedName("DF")
        DetectorFixed,
        @SerializedName("DM")
        DetectorMovable,
        @SerializedName("DU")
        DetectorUser;
    }

    public Type type;
    public String uid;
    public String name;
    public String metadata;

    public Device() {}

    public Device(Type type, String uid, String name) {
        this.type = type;
        this.uid = uid.toLowerCase().replace(":", "").replace("-", "");
        this.name = name;
    }

    public Device(Type type, String uid, String name, String metadata) {
        this.type = type;
        this.uid = uid.toLowerCase().replace(":", "").replace("-", "");
        this.name = name;
        this.metadata = metadata;
    }
}
