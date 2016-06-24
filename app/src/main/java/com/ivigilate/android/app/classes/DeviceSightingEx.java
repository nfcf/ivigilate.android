package com.ivigilate.android.app.classes;

import com.ivigilate.android.library.classes.DeviceProvisioning;
import com.ivigilate.android.library.interfaces.IDeviceSighting;

public class DeviceSightingEx extends BaseDeviceSightingEx {

    private boolean mDeviceActive = false;
    private boolean mDeviceProvisioned = false;
    private int mTypeIconId;
    private DeviceProvisioning.DeviceType mDeviceType;
    private DeviceProvisioning.IdentifierType mIdentifierType;

    public boolean isDeviceActive() {
        return mDeviceActive;
    }

    public void setDeviceActive(boolean deviceActive) {
        this.mDeviceActive = deviceActive;
    }

    public boolean isDeviceProvisioned() {
        return mDeviceProvisioned;
    }

    public void setDeviceProvisioned(boolean deviceProvisioned) {
        this.mDeviceProvisioned = deviceProvisioned;
    }

    public int getTypeIconId() {
        return mTypeIconId;
    }

    public void setTypeIconId(int iconTypeId) {
        this.mTypeIconId = iconTypeId;
    }

    public DeviceProvisioning.DeviceType getDeviceType() {
        return mDeviceType;
    }

    public void setDeviceType(DeviceProvisioning.DeviceType type) {
        this.mDeviceType = type;
    }

    public DeviceProvisioning.IdentifierType getIdentifierType() {
        return mIdentifierType;
    }

    public void setIdentifierType(DeviceProvisioning.IdentifierType identifierType) {
        this.mIdentifierType = identifierType;
    }

    public DeviceSightingEx(IDeviceSighting deviceSighting) {
        super(deviceSighting);
    }
}
