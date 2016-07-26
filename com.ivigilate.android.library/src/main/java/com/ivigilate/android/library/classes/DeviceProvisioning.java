package com.ivigilate.android.library.classes;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class DeviceProvisioning {
    public enum DeviceType {
        @SerializedName("BF")
        TagFixed("Tag Fixed"),
        @SerializedName("BM")
        TagMovable("Tag Movable"),
        @SerializedName("DF")
        DetectorFixed("Detector Fixed"),
        @SerializedName("DM")
        DetectorMovable("Detector Movable"),
        @SerializedName("DU")
        DetectorUser("Detector User");

        private String type;

        DeviceType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    public enum IdentifierType {
        @SerializedName("MAC")
        MAC,
        @SerializedName("UUID")
        UUID;
    }

    public DeviceType type;
    public String uid;
    public String name;
    public String metadata;
    public boolean is_active;

    public DeviceProvisioning() {}

    public DeviceProvisioning(DeviceType type, String uid, String name, boolean isActive) {
        this.type = type;
        this.uid = uid != null ? uid.toLowerCase().replace(":", "").replace("-", "") : "";
        this.name = name;
        this.is_active = isActive;
    }

    public DeviceProvisioning(DeviceType type, String uid, String name, boolean isActive, JsonObject metadata) {
        this.type = type;
        this.uid = uid != null ? uid.toLowerCase().replace(":", "").replace("-", "") : "";
        this.name = name;
        this.is_active = isActive;

        Gson gson = new Gson();
        this.metadata = gson.toJson(metadata);
    }
}
