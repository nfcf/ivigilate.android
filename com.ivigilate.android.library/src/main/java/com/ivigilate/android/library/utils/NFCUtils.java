package com.ivigilate.android.library.utils;

/**
 * Created by joanaPeixoto on 24-Jun-16.
 */
public class NFCUtils {
    public static String getManufacturerDescription(String manufacturerHex){
        String knownDescription = "";
        switch (Integer.parseInt(manufacturerHex, 16)) {
            case MANUFACTURER_MOTOROLA:
                knownDescription = "(Motorola)";
                break;
            case MANUFACTURER_ST_MICROELECTRONICS:
                knownDescription = "(ST Microelectronics)";
                break;
            case MANUFACTURER_HITACHI_LTD:
                knownDescription = "(Hitachi Ltd)";
                break;
            case MANUFACTURER_NXP_SEMICONDUCTORS:
                knownDescription = "(Nxp Semiconductors)";
                break;
            case MANUFACTURER_INFINEON_TECHNOLOGIES_AG:
                knownDescription = "(Infineon Tech)";
                break;
            case MANUFACTURER_CYLINK:
                knownDescription = "(Cylink)";
                break;
            case MANUFACTURER_TEXAS_INSTRUMENT:
                knownDescription = "(Texas Instrument)";
                break;
            case MANUFACTURER_FUJITSU_LIMITED:
                knownDescription = "(Fujitsu Ltd)";
                break;
            case MANUFACTURER_MATSUSHITA_ELECTRONICS_CORPORATION:
                knownDescription = "(Matsushita)";
                break;
            case MANUFACTURER_NEC:
                knownDescription = "(NEC)";
                break;
            case MANUFACTURER_OKI_ELECTRIC_INDUSTRY_CO:
                knownDescription = "(Oki Electric)";
                break;
            case MANUFACTURER_TOSHIBA_CORP:
                knownDescription = "(Toshiba Corp)";
                break;
            case MANUFACTURER_MITSUBISHI_ELECTRIC_CORP:
                knownDescription = "(Mitsubishi)";
                break;
            case MANUFACTURER_SAMSUNG_ELECTRONICS_CO:
                knownDescription = "(Samsung)";
                break;
            case MANUFACTURER_HYUNDAI_ELECTRONICS_INDUSTRIES_CO:
                knownDescription = "(Hyundai)";
                break;
            case MANUFACTURER_EMOSYN_EM_MICROELECTRONICS:
                knownDescription = "(Emosyn EM)";
                break;
            case MANUFACTURER_LG_SEMICONDUCTORS_CO:
                knownDescription = "(LG)";
                break;
            case MANUFACTURER_INSIDE_TECHNOLOGY:
                knownDescription = "(Inside Tech.)";
                break;
            case MANUFACTURER_ORGA_KARTENSYSTEME_GMBH:
                knownDescription = "(Orga)";
                break;
            case MANUFACTURER_SHARP_CORPORATION:
                knownDescription = "(Sharp)";
                break;
            case MANUFACTURER_ATMEL:
                knownDescription = "(Atmel)";
                break;
            case MANUFACTURER_EM_MICROELECTRONIC_MARIN_SA:
                knownDescription = "(EM)";
                break;
            case MANUFACTURER_KSW_MICROTEC_GMBH:
                knownDescription = "(KSW Microtec)";
                break;
            case MANUFACTURER_XICOR_INC:
                knownDescription = "(Xicor Inc.)";
                break;
            case MANUFACTURER_DALLAS_SEMICONDUCTOR_MAXIM:
                knownDescription = "(Dallas)";
                break;
            default:
                knownDescription = "(Unknown)";
        }
        return knownDescription;
    }


    private static final int MANUFACTURER_MOTOROLA = 0x01;
    private static final int MANUFACTURER_ST_MICROELECTRONICS = 0x02;
    private static final int MANUFACTURER_HITACHI_LTD = 0x03;
    private static final int MANUFACTURER_NXP_SEMICONDUCTORS = 0x04;
    private static final int MANUFACTURER_INFINEON_TECHNOLOGIES_AG = 0x05;
    private static final int MANUFACTURER_CYLINK = 0x06;
    private static final int MANUFACTURER_TEXAS_INSTRUMENT = 0x07;
    private static final int MANUFACTURER_FUJITSU_LIMITED = 0x08;
    private static final int MANUFACTURER_MATSUSHITA_ELECTRONICS_CORPORATION = 0x09;
    private static final int MANUFACTURER_NEC = 0x0a;
    private static final int MANUFACTURER_OKI_ELECTRIC_INDUSTRY_CO = 0x0b;
    private static final int MANUFACTURER_TOSHIBA_CORP= 0x0c;
    private static final int MANUFACTURER_MITSUBISHI_ELECTRIC_CORP = 0x0d;
    private static final int MANUFACTURER_SAMSUNG_ELECTRONICS_CO = 0x0e;
    private static final int MANUFACTURER_HYUNDAI_ELECTRONICS_INDUSTRIES_CO = 0x0f;
    private static final int MANUFACTURER_LG_SEMICONDUCTORS_CO = 0x10;
    private static final int MANUFACTURER_EMOSYN_EM_MICROELECTRONICS = 0x11;
    private static final int MANUFACTURER_INSIDE_TECHNOLOGY = 0x12;
    private static final int MANUFACTURER_ORGA_KARTENSYSTEME_GMBH = 0x13;
    private static final int MANUFACTURER_SHARP_CORPORATION = 0x14;
    private static final int MANUFACTURER_ATMEL = 0x15;
    private static final int MANUFACTURER_EM_MICROELECTRONIC_MARIN_SA = 0x16;
    private static final int MANUFACTURER_KSW_MICROTEC_GMBH = 0x17;
    private static final int MANUFACTURER_XICOR_INC = 0x19;
    private static final int MANUFACTURER_DALLAS_SEMICONDUCTOR_MAXIM = 0x2b;

}
