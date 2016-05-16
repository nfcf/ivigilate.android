package com.ivigilate.android.library.classes;

import android.bluetooth.BluetoothDevice;

import com.ivigilate.android.library.utils.BleAdvUtils;
import com.ivigilate.android.library.utils.StringUtils;

public class DeviceSighting {

    private BluetoothDevice mBluetoothDevice;  // this can be used to connect to BT services
    private int mRssi;
    private byte[] mBytes;

    private String mPayload;

    private String mDeviceName;
    private boolean mProvisioned = false;
    private int mTypeIconId;
    private String mDeviceType;
    private String mDeviceUUIDType;

    public DeviceSighting() {}

    public DeviceSighting(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
        mBluetoothDevice = bluetoothDevice;
        mRssi = rssi;
        mBytes = bytes;
    }

    public String getMac() {
        if (getManufacturer().contains("C6A0")) {
            return "";
        } else {
            return mBluetoothDevice.getAddress().replace(":", "");
        }
    }

    public void setDeviceName(String mDeviceName) {
        this.mDeviceName = mDeviceName;
    }

    public String getName() {
        return mDeviceName == null ? mBluetoothDevice.getName() : mDeviceName;
    }

    public String getManufacturer() {
        if (getPayload().length() > 14) {
            String manufacturer = getPayload().substring(10, 14);
            String description = BleAdvUtils.getManufacturerDescription(manufacturer);

            return manufacturer + " " + description;
        } else {
            return "";
        }
    }

    public String getBleType() {
        if (getPayload().length() > 18) {
            return getPayload().substring(14, 18);
        } else {
            return "";
        }
    }

    public String getUUID() {
        if (getPayload().length() > 50) {
            String uuid = getPayload().substring(18, 50);
            return !getManufacturer().contains("4C00") &&
                    uuid.contains("0000") ? "" : uuid;
        } else {
            return "";
        }
    }

    public String getData() {
        if (getUUID() == "" && getPayload().length() > 18) {
            return StringUtils.trimRight(getPayload().substring(18), '0');
        }
        else if (getPayload().length() > 50) {
            return StringUtils.trimRight(getPayload().substring(50), '0');
        } else {
            return "";
        }
    }

    public String getPayload() {
        if (mPayload == null) {
            mPayload = StringUtils.bytesToHexString(mBytes);
        }
        return mPayload;
    }

    public int getRssi() {
        return mRssi;
    }

    public void setRssi(int rssi) {
        mRssi = rssi;
    }

    public int getBattery() {
        return getUUID() != "" && getData() != "" && getData().length() > 11 ?
                Integer.parseInt(getData().substring(9,11), 16) : 0;
    }

    public boolean isProvisioned() {
        return mProvisioned;
    }

    public void setProvisioned(boolean provisioned) {
        this.mProvisioned = provisioned;
    }

    public int getTypeIconId() {
        return mTypeIconId;
    }

    public void setTypeIconId(int iconTypeId) {
        this.mTypeIconId = iconTypeId;
    }

    public String getDeviceType() {
        return mDeviceType;
    }

    public void setDeviceType(String type) {
        this.mDeviceType = type;
    }

    public String getDeviceUUIDType() {
        return mDeviceUUIDType;
    }

    public void setDeviceUUIDType(String uuidType) {
        this.mDeviceUUIDType = uuidType;
    }
}
