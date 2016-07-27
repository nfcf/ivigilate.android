package com.ivigilate.android.library.classes;

import android.bluetooth.BluetoothDevice;

import com.google.android.gms.cast.internal.DeviceStatus;
import com.ivigilate.android.library.interfaces.IDeviceSighting;
import com.ivigilate.android.library.utils.BleAdvUtils;
import com.ivigilate.android.library.utils.StringUtils;

public class BleDeviceSighting implements IDeviceSighting {

    private BluetoothDevice mBluetoothDevice;  // this can be used to connect to BT services
    private int mRssi;
    private byte[] mBytes;

    private String mPayload;

    private String mDeviceName;

    public BleDeviceSighting() {}

    public BleDeviceSighting(BleDeviceSighting deviceSighting) {
        mBluetoothDevice = deviceSighting.mBluetoothDevice;
        mRssi = deviceSighting.mRssi;
        mBytes = deviceSighting.mBytes;
    }

    public BleDeviceSighting(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
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

    public String getType() {
        if (getPayload().length() > 18) {
            return getPayload().substring(14, 18);
        } else {
            return "";
        }
    }

    public String getUUID() {
        if (getPayload().length() > 50 && getManufacturer().contains("4C00") && getType().contains("0215")) {  // iBeacon
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
        if (getManufacturer().contains("6561") && getType().contains("636F") &&
                getData().length() >= 32) {  // EM Micro
            //To map
            //[A, B] --> [a, b]
            //use this formula
            //(val - A)*(b-a)/(B-A) + a
            float emBatteryLevel = Integer.parseInt(getData().substring(30, 32)) / (float)10;
            return (int)((emBatteryLevel - (float)0.9) * ((float)100 - (float)0) /
                    ((float)3 - (float)0.9) + ((float)0));
        } else if (!StringUtils.isNullOrBlank(getUUID()) &&
                !StringUtils.isNullOrBlank(getData()) && getData().length() > 11) {  // Not sure which devices send battery info this way...
            return Integer.parseInt(getData().substring(9,11), 16);
        } else {
            return 0;
        }
    }

    public Sighting.Status getStatus(){
        Sighting.Status status = Sighting.Status.Normal;

        if (getManufacturer().contains("6561") && getType().contains("636F") &&
                getData().length() >= 44) {  // EM Micro
            String sightingStatus = getData().substring(40, 44);
            switch (sightingStatus) {
                case "FFFF":
                    status = Sighting.Status.Panic;
                    break;
                case "AAAA":
                    status = Sighting.Status.Fall;
                    break;
            }
        }

        return status;
    }
}
