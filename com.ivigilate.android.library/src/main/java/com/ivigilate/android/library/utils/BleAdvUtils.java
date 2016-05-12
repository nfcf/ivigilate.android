package com.ivigilate.android.library.utils;

public class BleAdvUtils {

    public static String getManufacturerDescription(String manufacturerHex){
        String knownDescription = "";
        String l2bEndian = manufacturerHex.substring(2,4) + manufacturerHex.substring(0,2);
        switch (Integer.parseInt(l2bEndian, 16)) {
            case MANUFACTURER_APPLE:
                knownDescription = "(Apple Inc.)";
                break;
            case MANUFACTURER_ALTBEACON:
                knownDescription = "(AltBeacon)";
                break;
            case MANUFACTURER_EDDYSTONE:
                knownDescription = "(Eddystone)";
                break;
            case MANUFACTURER_FOREVER:
                knownDescription = "(Forever)";
                break;
            case MANUFACTURER_GIMBAL:
                knownDescription = "(Gimbal)";
                break;
            case MANUFACTURER_JABRA:
                knownDescription = "(Jabra)";
                break;
            case MANUFACTURER_MOOV:
                knownDescription = "(Moov)";
                break;
            case MANUFACTURER_SAMSUNG:
                knownDescription = "(Samsung)";
                break;
            case MANUFACTURER_TI:
                knownDescription = "(TI)";
                break;
            case MANUFACTURER_EM:
                knownDescription = "(EM Micro)";
                break;
            case MANUFACTURER_PEBBLE:
                knownDescription = "(Pebble)";
                break;
            default:
                knownDescription = "(Unknown)";
        }
        return knownDescription;
    }


    private static final int MANUFACTURER_APPLE = 0x004c;
    private static final int MANUFACTURER_ALTBEACON = 0xbeac;
    private static final int MANUFACTURER_EDDYSTONE = 0xfeaa;
    private static final int MANUFACTURER_MOOV = 0x8eda;
    private static final int MANUFACTURER_FOREVER = 0x7665;
    private static final int MANUFACTURER_GIMBAL = 0xa0c6; //"ad7700c6";
    private static final int MANUFACTURER_JABRA = 0x180d;
    private static final int MANUFACTURER_TI = 0xaa80;
    private static final int MANUFACTURER_EM = 0x6165;
    private static final int MANUFACTURER_SAMSUNG = 0x0075;
    private static final int MANUFACTURER_PEBBLE = 0xfed9;

}
