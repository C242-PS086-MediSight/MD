package com.example.medisight.helper

import android.content.Context
import android.content.SharedPreferences

class PreferencesHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREF_NAME = "MediSightPrefs"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
    }

    fun setNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply()
    }

    fun isNotificationEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATION_ENABLED, false)
    }
}