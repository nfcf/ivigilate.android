package com.ivigilate.android.library.classes;

import android.nfc.NdefRecord;
import android.nfc.Tag;

import com.ivigilate.android.library.interfaces.IDeviceSighting;
import com.ivigilate.android.library.utils.NFCUtils;
import com.ivigilate.android.library.utils.StringUtils;

/**
 * Created by joanaPeixoto on 14-Jun-16.
 */
public class NdfDeviceSighting implements IDeviceSighting {

    private Tag mTag;
    private int mRssi;

    private NdefRecord[] mRecords;

    public NdfDeviceSighting(NdfDeviceSighting deviceSighting) {
        mTag = deviceSighting.mTag;
        mRecords = deviceSighting.mRecords;
        mRssi = deviceSighting.mRssi;
    }

    public NdfDeviceSighting(Tag tag, NdefRecord[] records) {
        mTag = tag;
        mRecords = records;
        mRssi = 0;
    }

    @Override
    public String getUUID() {
        return "NFC" + StringUtils.bytesToHexString(mTag.getId());
    }

    @Override
    public String getType() {
        String tech = getTechList()[0];
        int i = tech.lastIndexOf(".");
        return getTechList()[0].substring(i + 1, tech.length());
    }

    @Override
    public String getManufacturer() {
        String manufacturerCode = StringUtils.bytesToHexString(mTag.getId()).substring(0, 2);
        return manufacturerCode + " " + NFCUtils.getManufacturerDescription(manufacturerCode);
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

    @Override
    public int getRssi() {
        return mRssi;
    }

    @Override
    public void setRssi(int mRssi) {
        this.mRssi = mRssi;
    }

    @Override
    public Sighting.Status getStatus() {
        return Sighting.Status.Normal;
    }

    public String[] getTechList() {
        if (mTag != null) return mTag.getTechList();
        return null;
    }
}
