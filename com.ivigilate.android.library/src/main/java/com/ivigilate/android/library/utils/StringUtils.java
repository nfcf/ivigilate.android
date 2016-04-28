package com.ivigilate.android.library.utils;

public class StringUtils {

	public static final String EMPTY_STRING = "";

    public static boolean isNullOrBlank(String param) {
	    return param == null || param.trim().length() == 0;
	}

	public static String bytesToHexString(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}
