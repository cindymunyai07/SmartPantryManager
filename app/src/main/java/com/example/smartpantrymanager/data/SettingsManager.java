package com.example.smartpantrymanager.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Saves and reads the user's settings using SharedPreferences.
 * The values survive closing the app, like the database does.
 */
public class SettingsManager {

    private static final String PREFS_NAME = "smart_pantry_settings";
    private static final String KEY_ALERT_ENABLED = "expiry_alert_enabled";
    private static final String KEY_ALERT_DAYS = "expiry_alert_days";

    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Whether items close to their expiry date should be highlighted (default: on). */
    public boolean isExpiryAlertEnabled() {
        return prefs.getBoolean(KEY_ALERT_ENABLED, true);
    }

    public void setExpiryAlertEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ALERT_ENABLED, enabled).apply();
    }

    /** How many days before expiry an item counts as "expiring soon" (default: 3). */
    public int getExpiryDays() {
        return prefs.getInt(KEY_ALERT_DAYS, 3);
    }

    public void setExpiryDays(int days) {
        prefs.edit().putInt(KEY_ALERT_DAYS, days).apply();
    }
}