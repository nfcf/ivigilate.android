package com.ivigilate.android.library.utils;

public class StringUtils {

	public static final String EMPTY_STRING = "";

    public static boolean isNullOrBlank(String param) {
	    return param == null || param.trim().length() == 0;
	}

	public static String trimLeft(final String string, final char trimChar)
	{
		final int stringLength = string.length();
		int i;

		for (i = 0; i < stringLength && string.charAt(i) == trimChar; i++) {
            /* increment i until it is at the location of the first char that
             * does not match the trimChar given. */
		}

		if (i == 0) {
			return string;
		} else {
			return string.substring(i);
		}
	}

	public static String trimRight(final String string, final char trimChar)
	{
		final int lastChar = string.length() - 1;
		int i;

		for (i = lastChar; i >= 0 && string.charAt(i) == trimChar; i--) {
            /* Decrement i until it is equal to the first char that does not
             * match the trimChar given. */
		}

		if (i < lastChar) {
			// the +1 is so we include the char at i
			return string.substring(0, i+1);
		} else {
			return string;
		}
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
