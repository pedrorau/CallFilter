package com.pedrorau.callfilter.repository

import com.russhwolf.settings.Settings

class PreferencesRepository(private val settings: Settings = Settings()) {

    companion object {
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_SERVICE_EVER_INVOKED = "service_ever_invoked"
    }

    fun isNotificationsEnabled(): Boolean {
        return settings.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
    }

    fun isOnboardingCompleted(): Boolean {
        return settings.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        settings.putBoolean(KEY_ONBOARDING_COMPLETED, completed)
    }

    fun isServiceEverInvoked(): Boolean {
        return settings.getBoolean(KEY_SERVICE_EVER_INVOKED, false)
    }

    fun markServiceInvoked() {
        settings.putBoolean(KEY_SERVICE_EVER_INVOKED, true)
    }
}
