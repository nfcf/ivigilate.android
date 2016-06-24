package com.ivigilate.android.library.classes;

import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.widget.Adapter;

import com.ivigilate.android.library.interfaces.IDeviceSighting;
import com.ivigilate.android.library.utils.NFCUtils;
import com.ivigilate.android.library.utils.StringUtils;

/**
 * Created by joanaPeixoto on 14-Jun-16.
 */
public class NFCdeviceSighting implements IDeviceSighting {

    private Tag mTag;
    private int mRssi;

    private String mDeviceName;

    private NdefRecord[] mRecords;


    public NFCdeviceSighting() {
    }

    public NFCdeviceSighting(NFCdeviceSighting deviceSighting) {
        mTag = deviceSighting.mTag;
        mRecords = deviceSighting.mRecords;
        mRssi = deviceSighting.mRssi;
    }

    public NFCdeviceSighting(Tag tag, NdefRecord[] records) {
        mTag = tag;
        mRecords = records;
        mRssi = 0;
    }

    @Override
    public String getMac() {
        return "N/A";
    }

    @Override
    public void setDeviceName(String mDeviceName) {
        this.mDeviceName = mDeviceName;
    }

    @Override
    public String getName() {
        return mDeviceName == null ? "N/A" : mDeviceName;
    }

    @Override
    public String getManufacturer() {
        String manufacturerCode = StringUtils.bytesToHexString(mTag.getId()).substring(0,2);
        return manufacturerCode + " " + NFCUtils.getManufacturerDescription(manufacturerCode);
    }

    @Override
    public String getUUID() {
        return "NFC" + StringUtils.bytesToHexString(mTag.getId());
    }

    @Override
    public String getData() {
        return "";
    }

    @Override
    public String getPayload() {
        String payload = "";
        if (mRecords != null) {
            for (int i = 0; i < mRecords.length; i++) {
                payload += StringUtils.bytesToHexString(mRecords[i].getPayload()) + "\n";
            }
        }
        return payload;
    }

    public int getRssi() {
        return mRssi;
    }

    public void setRssi(int mRssi) {
        this.mRssi = mRssi;
    }

    public int getBattery() {
        return 0;
    }

    public String[] getTechList() {
        if(mTag != null) return mTag.getTechList();
        return null;
    }


    public String getType() {
         String tech = getTechList()[0];
         int i = tech.lastIndexOf(".");
         return getTechList()[0].substring(i + 1, tech.length());
    }
}
