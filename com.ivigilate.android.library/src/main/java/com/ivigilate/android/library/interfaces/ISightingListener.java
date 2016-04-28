package com.ivigilate.android.library.interfaces;

public interface ISightingListener {

    void onDeviceSighted(String mac, String uid, int rssi);

}
