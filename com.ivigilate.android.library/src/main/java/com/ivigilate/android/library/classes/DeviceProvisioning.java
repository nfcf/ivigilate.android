package com.ivigilate.android.library.classes;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeviceProvisioning {
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

    public DeviceProvisioning() {}

    public DeviceProvisioning(Type type, String uid, String name) {
        this.type = type;
        this.uid = uid.toLowerCase().replace(":", "").replace("-", "");
        this.name = name;
    }

    public DeviceProvisioning(Type type, String uid, String name, JsonObject metadata) {
        this.type = type;
        this.uid = uid.toLowerCase().replace(":", "").replace("-", "");
        this.name = name;

        Gson gson = new Gson();
        this.metadata = gson.toJson(metadata);
    }
}
