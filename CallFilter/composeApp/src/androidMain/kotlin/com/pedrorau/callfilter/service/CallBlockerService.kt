package com.pedrorau.callfilter.service

import android.telecom.Call
import android.telecom.CallScreeningService
import com.pedrorau.callfilter.engine.RuleEngine
import com.pedrorau.callfilter.model.RuleResult
import com.pedrorau.callfilter.notification.BlockNotificationManager
import com.pedrorau.callfilter.repository.BlockedNumberRepository
import com.pedrorau.callfilter.repository.PreferencesRepository
import com.pedrorau.callfilter.repository.RuleRepository

class CallBlockerService : CallScreeningService() {

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

        val rules = ruleRepository.getRules()

        val result = ruleEngine.evaluate(phoneNumber, rules)

        val response = CallResponse.Builder()

        if (result == RuleResult.REJECT) {
            response.setDisallowCall(true)
            response.setRejectCall(true)
            response.setSkipCallLog(false)
            response.setSkipNotification(false)

            if (preferencesRepository.isNotificationsEnabled()) {
                notificationManager.showBlockedNotification(phoneNumber)
            }
        }

        respondToCall(callDetails, response.build())
    }
}
