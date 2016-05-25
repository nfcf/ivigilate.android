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

    public DeviceSighting() {}

    public DeviceSighting(DeviceSighting deviceSighting) {
        mBluetoothDevice = deviceSighting.mBluetoothDevice;
        mRssi = deviceSighting.mRssi;
        mBytes = deviceSighting.mBytes;
    }

    public DeviceSighting(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
        mBluetoothDevice = bluetoothDevice;
        mRssi = rssi;
        mBytes = bytes;
    }

    public String getMac() {
        if (getManufacturer().contains("C6A0")) { // Gimbal
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
        if (getPayload().length() > 50 && getManufacturer().contains("4C00")) {  // iBeacon
            return getPayload().substring(18, 50);
        } else if (getPayload().length() >= 62 && getManufacturer().contains("C6A0")) {  // Gimbal
            return getPayload().substring(44, 62);
        } else {
            return "";
        }
    }

    public String getData() {
        if (StringUtils.isNullOrBlank(getUUID()) && getPayload().length() > 18) {
            return StringUtils.trimRight(getPayload().substring(18), '0');
        }
        else if (getPayload().length() > 50 && !getManufacturer().contains("C6A0")) {  // NOT Gimbal
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
        return !StringUtils.isNullOrBlank(getUUID()) && !StringUtils.isNullOrBlank(getData()) && getData().length() > 11 ?
                Integer.parseInt(getData().substring(9,11), 16) : 0;
    }
}
