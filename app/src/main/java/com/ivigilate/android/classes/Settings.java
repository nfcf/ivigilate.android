package com.ivigilate.android.classes;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class Settings {
	private static final String SETTINGS_FILENAME = "com.ivigilate.android.Settings";

	public static final String SETTINGS_DEBUG_SERVER_ADDRESS = "debug_server_address";
	public static final String SETTINGS_USER = "user";
	public static final String SETTINGS_SERVICE_ENABLED = "service_enabled";

	public SharedPreferences sharedPreferences;
	private final Gson gson;

	public Settings(Context ctx) {
		sharedPreferences = ctx.getSharedPreferences(SETTINGS_FILENAME, Context.MODE_PRIVATE);
		gson = new Gson();
	}

    public String getDebugServerAddress() { return sharedPreferences.getString(SETTINGS_DEBUG_SERVER_ADDRESS, ""); }

    public void setDebugServerAddress(String value){ sharedPreferences.edit().putString(SETTINGS_DEBUG_SERVER_ADDRESS, value != null ? value : "").commit(); }

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
