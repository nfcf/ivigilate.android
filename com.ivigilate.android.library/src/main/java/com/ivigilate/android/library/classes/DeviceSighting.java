package com.ivigilate.android.library.classes;

import android.bluetooth.BluetoothDevice;

import com.ivigilate.android.library.utils.StringUtils;

public class DeviceSighting {
    private final String MANUFACTURER_IBEACON = "4c000215";
    private final String MANUFACTURER_ALTBEACON = "4c00beac";
    private final String MANUFACTURER_FOREVER = "65766572";
    private final String MANUFACTURER_GIMBAL = "c6a00099"; //"ad7700c6";
    private final String MANUFACTURER_JABRA = "0d181418";
    private final String MANUFACTURER_MOOV = "da8eeab8";

    private BluetoothDevice mBluetoothDevice;  // this can be used to connect to BT services
    private int mRssi;
    private byte[] mBytes;

    private String mPayload;

    public DeviceSighting() {}

    public DeviceSighting(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
        mBluetoothDevice = bluetoothDevice;
        mRssi = rssi;
        mBytes = bytes;
    }

    public String getMac() {
        return mBluetoothDevice.getAddress().replace(":", "");
    }

    public String getManufacturer() {
        if (getPayload().length() > 18) {
            String manufacturer = getPayload().substring(10, 18);
            String knownDescription = "";
            switch (manufacturer.toLowerCase()) {
                case MANUFACTURER_IBEACON:
                    knownDescription = " (iBeacon)";
                    break;
                case MANUFACTURER_ALTBEACON:
                    knownDescription = " (AltBeacon)";
                    break;
                case MANUFACTURER_FOREVER:
                    knownDescription = " (Forever)";
                    break;
                case MANUFACTURER_GIMBAL:
                    knownDescription = " (Gimbal)";
                    break;
                case MANUFACTURER_JABRA:
                    knownDescription = " (Jabra)";
                    break;
                case MANUFACTURER_MOOV:
                    knownDescription = " (Moov)";
                    break;
            }
            return manufacturer + knownDescription;
        } else {
            return "";
        }
    }

    public String getUUID() {
        if (getPayload().length() > 50) {
            String uuid = getPayload().substring(18, 50);
            return uuid.contains("0000") ? "" : uuid;
        } else {
            return "";
        }
    }

    public String getData() {
        if (getUUID() == "" && getPayload().length() > 18) {
            return getPayload().substring(18).replace("0000","");
        }
        else if (getPayload().length() > 50) {
            return getPayload().substring(50).replace("0000","");
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
        return getUUID() != "" && getData() != "" ? Integer.parseInt(getData().substring(9,11), 16) : 0;
    }
}
