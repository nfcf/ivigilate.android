package com.ivigilate.android.library.classes;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

    private DeviceType type;
    private String uid;
    private String name;
    private String metadata;
    private boolean is_active;

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

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

    public boolean getActive() {
        return is_active;
    }

    public void setActive(boolean is_active) {
        this.is_active = is_active;
    }

    public JsonObject getMetadata() {
        JsonParser parser = new JsonParser();
        return parser.parse(this.metadata).getAsJsonObject();
    }

    public void setMetadata(JsonObject metadata) {
        Gson gson = new Gson();
        this.metadata = gson.toJson(metadata);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
