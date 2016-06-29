package com.ivigilate.android.library;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.location.LocationRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.ivigilate.android.library.classes.Sighting;
import com.ivigilate.android.library.classes.User;

import java.lang.reflect.Type;
import java.util.HashMap;

class Settings {
	private static final String SETTINGS_FILENAME = "com.ivigilate.android.settings";

	private static final String DEFAULT_SERVER_ADDRESS = "https://portal.ivigilate.com";
	private static final int DEFAULT_SERVICE_SEND_INTERVAL = 1 * 1000;
	private static final int DEFAULT_SERVICE_SIGHTING_STATE_CHANGE_INTERVAL = 0;

	private static final int DEFAULT_LOCATION_REQUEST_INTERVAL = 30 * 1000;
	private static final int DEFAULT_LOCATION_REQUEST_FASTEST_INTERVAL = 20 * 1000;
	private static final int DEFAULT_LOCATION_REQUEST_SMALLEST_DISPLACEMENT = 10;
	private static final int DEFAULT_LOCATION_REQUEST_PRIORITY = LocationRequest.PRIORITY_LOW_POWER;

	private static final int DEFAULT_NOTIFICATION_ICON = R.drawable.ic_notification;
	private static final int DEFAULT_NOTIFICATION_COLOR = 0xFF000000; // lazy way to set black as default...


	public static final String SETTINGS_SERVER_ADDRESS = "server_address";
	public static final String SETTINGS_SERVER_TIME_OFFSET = "server_time_offset";

    public static final String SETTINGS_SERVICE_ENABLED = "service_enabled";
	public static final String SETTINGS_SERVICE_INVALID_BEACONS = "service_invalid_beacons";
	public static final String SETTINGS_SERVICE_ACTIVE_SIGHTINGS = "service_active_sightings";
	public static final String SETTINGS_SERVICE_SEND_INTERVAL = "service_send_interval";
	public static final String SETTINGS_SERVICE_SIGHTING_STATE_CHANGE_INTERVAL = "service_sighting_state_change_interval";
	public static final String SETTINGS_SERVICE_SIGHTING_METADATA = "service_sighting_metadata";

	public static final String SETTINGS_LOCATION_REQUEST_INTERVAL = "location_request_interval";
	public static final String SETTINGS_LOCATION_REQUEST_FASTEST_INTERVAL = "location_request_fastest_interval";
	public static final String SETTINGS_LOCATION_REQUEST_SMALLEST_DISPLACEMENT = "location_request_smallest_displacement";
	public static final String SETTINGS_LOCATION_REQUEST_PRIORITY = "location_request_priority";

	public static final String SETTINGS_NOTIFICATION_ICON = "notification_icon";
	public static final String SETTINGS_NOTIFICATION_COLOR = "notification_color";
	public static final String SETTINGS_NOTIFICATION_TITLE = "notification_title";
	public static final String SETTINGS_NOTIFICATION_MESSAGE = "notification_message";

	public static final String SETTINGS_USER = "user";

	public SharedPreferences sharedPreferences;
    private Context context;
	private final Gson gson;

	public Settings(Context ctx) {
		sharedPreferences = ctx.getSharedPreferences(SETTINGS_FILENAME, Context.MODE_PRIVATE);
        context = ctx;
		gson = new Gson();
	}

    public String getServerAddress() { return sharedPreferences.getString(SETTINGS_SERVER_ADDRESS, DEFAULT_SERVER_ADDRESS); }

    public void setServerAddress(String value){ sharedPreferences.edit().putString(SETTINGS_SERVER_ADDRESS, value != null ? value : "").commit(); }

	public long getServerTimeOffset() { return sharedPreferences.getLong(SETTINGS_SERVER_TIME_OFFSET, System.currentTimeMillis()); }

	public void setServerTimeOffset(long value){ sharedPreferences.edit().putLong(SETTINGS_SERVER_TIME_OFFSET, value).commit(); }


    public boolean getServiceEnabled() { return sharedPreferences.getBoolean(SETTINGS_SERVICE_ENABLED, false); }

    public void setServiceEnabled(boolean value){ sharedPreferences.edit().putBoolean(SETTINGS_SERVICE_ENABLED, value).commit(); }

	public HashMap<String, Long> getServiceInvalidBeacons() {
		Type type = new TypeToken<HashMap<String, Long>>() {
		}.getType();
		try {
			return gson.fromJson(sharedPreferences.getString(SETTINGS_SERVICE_INVALID_BEACONS, "{}"), type);
		} catch (Exception ex) {
			return gson.fromJson("{}", type);
		}
	}

	public void setServiceInvalidBeacons(HashMap<String, Long> ignoreSightings){
		sharedPreferences.edit().putString(SETTINGS_SERVICE_INVALID_BEACONS, ignoreSightings != null ? gson.toJson(ignoreSightings) : "{}").commit();
	}

	public HashMap<String, Sighting> getServiceActiveSightings() {
		Type type = new TypeToken<HashMap<String, Sighting>>() {
		}.getType();
        try {
            return gson.fromJson(sharedPreferences.getString(SETTINGS_SERVICE_ACTIVE_SIGHTINGS, "{}"), type);
        } catch (Exception ex) {
            return gson.fromJson("{}", type);
        }
	}

	public void setServiceActiveSightings(HashMap<String, Sighting> activeSightings){
		sharedPreferences.edit().putString(SETTINGS_SERVICE_ACTIVE_SIGHTINGS, activeSightings != null ? gson.toJson(activeSightings) : "{}").commit();
	}

	public int getServiceSendInterval() { return sharedPreferences.getInt(SETTINGS_SERVICE_SEND_INTERVAL, DEFAULT_SERVICE_SEND_INTERVAL); }

	public void setServiceSendInterval(int value){ sharedPreferences.edit().putInt(SETTINGS_SERVICE_SEND_INTERVAL, value > 0 ? value : DEFAULT_SERVICE_SEND_INTERVAL).commit(); }

	public int getServiceSightingStateChangeInterval() {
		return sharedPreferences.getInt(SETTINGS_SERVICE_SIGHTING_STATE_CHANGE_INTERVAL, DEFAULT_SERVICE_SIGHTING_STATE_CHANGE_INTERVAL); }

	public void setServiceSightingStateChangeInterval(int value) {
		sharedPreferences.edit().putInt(SETTINGS_SERVICE_SIGHTING_STATE_CHANGE_INTERVAL, value >= 0 ? value : DEFAULT_SERVICE_SIGHTING_STATE_CHANGE_INTERVAL).commit(); }

	public JsonObject getServiceSightingMetadata() {
		JsonParser parser = new JsonParser();
		return parser.parse(sharedPreferences.getString(SETTINGS_SERVICE_SIGHTING_METADATA, "{}")).getAsJsonObject();
	}

	public void setServiceSightingMetadata(JsonObject value){ sharedPreferences.edit().putString(SETTINGS_SERVICE_SIGHTING_METADATA, value != null ? value.toString() : "{}").commit(); }


    public int getLocationRequestInterval() { return sharedPreferences.getInt(SETTINGS_LOCATION_REQUEST_INTERVAL, DEFAULT_LOCATION_REQUEST_INTERVAL); }

	public void setLocationRequestInterval(int value){ sharedPreferences.edit().putInt(SETTINGS_LOCATION_REQUEST_INTERVAL, value > 0 ? value : DEFAULT_LOCATION_REQUEST_INTERVAL).commit(); }

	public int getLocationRequestFastestInterval() { return sharedPreferences.getInt(SETTINGS_LOCATION_REQUEST_FASTEST_INTERVAL, DEFAULT_LOCATION_REQUEST_FASTEST_INTERVAL); }

	public void setLocationRequestFastestInterval(int value){ sharedPreferences.edit().putInt(SETTINGS_LOCATION_REQUEST_FASTEST_INTERVAL, value > 0 ? value : DEFAULT_LOCATION_REQUEST_FASTEST_INTERVAL).commit(); }

	public int getLocationRequestSmallestDisplacement() { return sharedPreferences.getInt(SETTINGS_LOCATION_REQUEST_SMALLEST_DISPLACEMENT, DEFAULT_LOCATION_REQUEST_SMALLEST_DISPLACEMENT); }

	public void setLocationRequestSmallestDisplacement(int value){ sharedPreferences.edit().putInt(SETTINGS_LOCATION_REQUEST_SMALLEST_DISPLACEMENT, value > 0 ? value : DEFAULT_LOCATION_REQUEST_SMALLEST_DISPLACEMENT).commit(); }

	public void setLocationRequestPriority(int value){ sharedPreferences.edit().putInt(SETTINGS_LOCATION_REQUEST_PRIORITY, value > 0 ? value : DEFAULT_LOCATION_REQUEST_PRIORITY).commit(); }

	public int getLocationRequestPriority() { return sharedPreferences.getInt(SETTINGS_LOCATION_REQUEST_PRIORITY, DEFAULT_LOCATION_REQUEST_PRIORITY); }


    public void setNotificationIcon(int resId){ sharedPreferences.edit().putInt(SETTINGS_NOTIFICATION_ICON, resId != 0 ? resId : DEFAULT_NOTIFICATION_ICON).commit(); }

    public int getNotificationIcon() { return sharedPreferences.getInt(SETTINGS_NOTIFICATION_ICON, DEFAULT_NOTIFICATION_ICON); }

    public void setNotificationColor(int argb){ sharedPreferences.edit().putInt(SETTINGS_NOTIFICATION_COLOR, argb != 0 ? argb : DEFAULT_NOTIFICATION_COLOR).commit(); }

    public int getNotificationColor() { return sharedPreferences.getInt(SETTINGS_NOTIFICATION_COLOR, DEFAULT_NOTIFICATION_COLOR); }

    public void setNotificationTitle(String value){ sharedPreferences.edit().putString(SETTINGS_NOTIFICATION_TITLE, value != null ? value : context.getString(R.string.notification_title)).commit(); }

    public String getNotificationTitle() { return sharedPreferences.getString(SETTINGS_NOTIFICATION_TITLE, context.getString(R.string.notification_title)); }

    public void setNotificationMessage(String value){ sharedPreferences.edit().putString(SETTINGS_NOTIFICATION_MESSAGE, value != null ? value : context.getString(R.string.notification_message)).commit(); }

    public String getNotificationMessage() { return sharedPreferences.getString(SETTINGS_NOTIFICATION_MESSAGE, context.getString(R.string.notification_message)); }

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
