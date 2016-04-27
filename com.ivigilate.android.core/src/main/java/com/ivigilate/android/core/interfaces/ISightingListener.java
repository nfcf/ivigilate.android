package com.ivigilate.android.core.interfaces;

public interface ISightingListener {

    void onDeviceSighted(String mac, String uid, int rssi);

}
