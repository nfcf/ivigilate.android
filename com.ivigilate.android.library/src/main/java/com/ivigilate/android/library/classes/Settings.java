package com.ivigilate.android.library.classes;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

public class Settings {
	private static final String SETTINGS_FILENAME = "com.ivigilate.android.settings";

	private static final String DEFAULT_SERVER_ADDRESS = "https://portal.ivigilate.com";
	private static final int DEFAULT_SERVICE_SEND_INTERVAL = 1 * 1000;
	private static final int DEFAULT_SERVICE_STATE_CHANGE_INTERVAL = 0;

	private static final int DEFAULT_LOCATION_REQUEST_INTERVAL = 30 * 1000;
	private static final int DEFAULT_LOCATION_REQUEST_FASTEST_INTERVAL = 20 * 1000;
	private static final int DEFAULT_LOCATION_REQUEST_SMALLEST_DISPLACEMENT = 10;

	public static final String SETTINGS_SERVER_ADDRESS = "server_address";
	public static final String SETTINGS_SERVER_TIME_OFFSET = "server_time_offset";

	public static final String SETTINGS_SERVICE_ACTIVE_SIGHTINGS = "service_current_sightings";
	public static final String SETTINGS_SERVICE_SEND_INTERVAL = "service_send_interval";
	public static final String SETTINGS_SERVICE_STATE_CHANGE_INTERVAL = "service_state_change_interval";
	public static final String SETTINGS_SERVICE_SIGHTING_METADATA = "service_sighting_metadata";

	public static final String SETTINGS_LOCATION_REQUEST_INTERVAL = "location_request_interval";
	public static final String SETTINGS_LOCATION_REQUEST_FASTEST_INTERVAL = "location_request_fastest_interval";
	public static final String SETTINGS_LOCATION_REQUEST_SMALLEST_DISPLACEMENT = "location_request_smallest_displacement";
	public static final String SETTINGS_USER = "user";

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


	public HashMap<String, Sighting> getServiceActiveSightings() {
		Type type = new TypeToken<HashMap<String, Sighting>>() {
		}.getType();
		return gson.fromJson(sharedPreferences.getString(SETTINGS_SERVICE_ACTIVE_SIGHTINGS, "{}"), type);
	}

	public void setServiceActiveSightings(HashMap<String, Sighting> activeSightings){
		sharedPreferences.edit().putString(SETTINGS_SERVICE_ACTIVE_SIGHTINGS, activeSightings != null ? gson.toJson(activeSightings) : "{}").commit();
	}

	public int getServiceSendInterval() { return sharedPreferences.getInt(SETTINGS_SERVICE_SEND_INTERVAL, DEFAULT_SERVICE_SEND_INTERVAL); }

	public void setServiceSendInterval(int value){ sharedPreferences.edit().putInt(SETTINGS_SERVICE_SEND_INTERVAL, value > 0 ? value : DEFAULT_SERVICE_SEND_INTERVAL).commit(); }

	public int getServiceStateChangeInterval() {
		return sharedPreferences.getInt(SETTINGS_SERVICE_STATE_CHANGE_INTERVAL, DEFAULT_SERVICE_STATE_CHANGE_INTERVAL); }

	public void setServiceStateChangeInterval(int value) {
		sharedPreferences.edit().putInt(SETTINGS_SERVICE_STATE_CHANGE_INTERVAL, value >= 0 ? value : DEFAULT_SERVICE_STATE_CHANGE_INTERVAL).commit(); }

	public String getServiceSendSightingMetadata() { return sharedPreferences.getString(SETTINGS_SERVICE_SIGHTING_METADATA, ""); }

	public void setServiceSendSightingMetadata(String value){ sharedPreferences.edit().putString(SETTINGS_SERVICE_SIGHTING_METADATA, value != null ? value : "").commit(); }


	public int getLocationRequestInterval() { return sharedPreferences.getInt(SETTINGS_LOCATION_REQUEST_INTERVAL, DEFAULT_LOCATION_REQUEST_INTERVAL); }

	public void setLocationRequestInterval(int value){ sharedPreferences.edit().putInt(SETTINGS_LOCATION_REQUEST_INTERVAL, value > 0 ? value : DEFAULT_LOCATION_REQUEST_INTERVAL).commit(); }

	public int getLocationRequestFastestInterval() { return sharedPreferences.getInt(SETTINGS_LOCATION_REQUEST_FASTEST_INTERVAL, DEFAULT_LOCATION_REQUEST_FASTEST_INTERVAL); }

	public void setLocationRequestFastestInterval(int value){ sharedPreferences.edit().putInt(SETTINGS_LOCATION_REQUEST_FASTEST_INTERVAL, value > 0 ? value : DEFAULT_LOCATION_REQUEST_FASTEST_INTERVAL).commit(); }

	public int getLocationRequestSmallestDisplacement() { return sharedPreferences.getInt(SETTINGS_LOCATION_REQUEST_SMALLEST_DISPLACEMENT, DEFAULT_LOCATION_REQUEST_SMALLEST_DISPLACEMENT); }

	public void setLocationRequestSmallestDisplacement(int value){ sharedPreferences.edit().putInt(SETTINGS_LOCATION_REQUEST_SMALLEST_DISPLACEMENT, value > 0 ? value : DEFAULT_LOCATION_REQUEST_SMALLEST_DISPLACEMENT).commit(); }

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

}
