package com.pedrorau.callfilter.service

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.pedrorau.callfilter.engine.RuleEngine
import com.pedrorau.callfilter.model.RuleResult
import com.pedrorau.callfilter.notification.BlockNotificationManager
import com.pedrorau.callfilter.repository.BlockedNumberRepository
import com.pedrorau.callfilter.repository.PreferencesRepository
import com.pedrorau.callfilter.repository.RuleRepository

class CallBlockerService : CallScreeningService() {

    companion object {
        private const val TAG = "CallBlockerService"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service CREATED")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service DESTROYED")
    }

    private val ruleRepository by lazy { RuleRepository() }
    private val blockedNumberRepository by lazy { BlockedNumberRepository() }
    private val preferencesRepository by lazy { PreferencesRepository() }
    private val notificationManager by lazy { BlockNotificationManager(this) }

    private val ruleEngine by lazy {
        RuleEngine(blockedNumberLookup = { blockedNumberRepository.containsNumber(it) })
    }

    override fun onScreenCall(callDetails: Call.Details) {
        preferencesRepository.markServiceInvoked()

        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: ""
        Log.d(TAG, "onScreenCall invoked for number: $phoneNumber")

        val rules = ruleRepository.getRules()
        Log.d(TAG, "Active rules: ${rules.filter { it.enabled }.map { it.type }}")

        val result = ruleEngine.evaluate(phoneNumber, rules)
        Log.d(TAG, "Evaluation result: $result")

        val response = CallResponse.Builder()

        if (result == RuleResult.REJECT) {
            Log.d(TAG, "REJECTING call from: $phoneNumber")
            response.setDisallowCall(true)
            response.setRejectCall(true)
            response.setSkipCallLog(false)
            response.setSkipNotification(false)

            if (preferencesRepository.isNotificationsEnabled()) {
                notificationManager.showBlockedNotification(phoneNumber)
            }
        } else {
            Log.d(TAG, "ALLOWING call from: $phoneNumber")
        }

        respondToCall(callDetails, response.build())
    }
}
