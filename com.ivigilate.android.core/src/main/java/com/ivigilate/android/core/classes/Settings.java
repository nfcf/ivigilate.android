package com.ivigilate.android.core.classes;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class Settings {
	private static final String SETTINGS_FILENAME = "com.ivigilate.android.core.settings";
	private static final String DEFAULT_SERVER_ADDRESS = "https://portal.ivigilate.com";
	private static final int DEFAULT_SERVICE_SEND_INTERVAL = 1000;

	public static final String SETTINGS_SERVER_ADDRESS = "server_address";
	public static final String SETTINGS_SERVER_TIME_OFFSET = "server_time_offset";
	public static final String SETTINGS_SERVICE_SEND_INTERVAL = "service_send_interval";
	public static final String SETTINGS_USER = "user";
	public static final String SETTINGS_SERVICE_ENABLED = "service_enabled";

	public SharedPreferences sharedPreferences;
	private final Gson gson;

	public Settings(Context ctx) {
		sharedPreferences = ctx.getSharedPreferences(SETTINGS_FILENAME, Context.MODE_PRIVATE);
		gson = new Gson();
	}

    public String getServerAddress() { return sharedPreferences.getString(SETTINGS_SERVER_ADDRESS, DEFAULT_SERVER_ADDRESS); }

    public void setServerAddress(String value){ sharedPreferences.edit().putString(SETTINGS_SERVER_ADDRESS, value != null ? value : "").commit(); }

	public long getServerTimeOffset() { return sharedPreferences.getLong(SETTINGS_SERVER_TIME_OFFSET, System.currentTimeMillis()); }

	public void setServerTimeOffset(long value){ sharedPreferences.edit().putLong(SETTINGS_SERVER_TIME_OFFSET, value).commit(); }

	public int getServiceSendInterval() { return sharedPreferences.getInt(SETTINGS_SERVICE_SEND_INTERVAL, DEFAULT_SERVICE_SEND_INTERVAL); }

	public void setServiceSendInterval(int value){ sharedPreferences.edit().putInt(SETTINGS_SERVICE_SEND_INTERVAL, value > 0 ? value : DEFAULT_SERVICE_SEND_INTERVAL).commit(); }

	public User getUser() {
		String userString = sharedPreferences.getString(SETTINGS_USER, null);
		if (userString != null) {
			return gson.fromJson(userString, User.class);
		} else {
			return null;
		}
	}

	public void setUser(User value){
		if (value != null) {
			sharedPreferences.edit().putString(SETTINGS_USER, gson.toJson(value)).commit();
		} else {
			sharedPreferences.edit().remove(SETTINGS_USER).commit();
		}
	}
	
	public boolean getServiceEnabled()
	{
		return sharedPreferences.getBoolean(SETTINGS_SERVICE_ENABLED,false);
	}

	public void setServiceEnabled(boolean value){
		sharedPreferences.edit().putBoolean(SETTINGS_SERVICE_ENABLED,value).commit();
	}

}
