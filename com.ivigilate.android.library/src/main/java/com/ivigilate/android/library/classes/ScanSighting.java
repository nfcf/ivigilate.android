package com.ivigilate.android.library.classes;

import com.ivigilate.android.library.interfaces.ISighting;

/**
 * Created by joanaPeixoto on 28-Jun-16.
 * Scanned barcode or QR Code sightings
 */
public class ScanSighting implements ISighting {

    private String mScanContent;
    private String mScanFormat;

    public ScanSighting(String scanContent, String scanFormat) {
        mScanContent = scanContent;
        mScanFormat = scanFormat;
    }

    @Override
    public String getUUID() {
        return mScanContent;
    }

    @Override
    public String getType() {
        return mScanFormat;
    }

}
