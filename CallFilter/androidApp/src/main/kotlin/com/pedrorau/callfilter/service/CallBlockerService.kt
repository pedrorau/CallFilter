package com.pedrorau.callfilter.service

import android.os.Handler
import android.os.Looper
import android.telecom.Call
import android.telecom.CallScreeningService
import com.pedrorau.callfilter.engine.RuleEngine
import com.pedrorau.callfilter.engine.platformPhoneNumberNormalizer
import com.pedrorau.callfilter.model.RuleResult
import com.pedrorau.callfilter.notification.BlockNotificationManager
import com.pedrorau.callfilter.repository.BlockedNumberRepository
import com.pedrorau.callfilter.repository.PreferencesRepository
import com.pedrorau.callfilter.repository.RuleRepository
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class CallBlockerService : CallScreeningService() {

    companion object {
        // Android allows ~5s to respond; if evaluation (e.g. a pathological regex) takes longer, let the call through.
        private const val EVALUATION_TIMEOUT_MS = 2_000L
    }

    private val ruleRepository by lazy { RuleRepository() }
    private val blockedNumberRepository by lazy { BlockedNumberRepository() }
    private val preferencesRepository by lazy { PreferencesRepository() }
    private val notificationManager by lazy { BlockNotificationManager(this) }
    private val normalizer by lazy { platformPhoneNumberNormalizer() }

    private val ruleEngine by lazy {
        RuleEngine(blockedNumberLookup = { blockedNumberRepository.containsNumber(it) })
    }

    // Cached pool so a stuck evaluation never blocks screening of the next call.
    private val executor = Executors.newCachedThreadPool()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onScreenCall(callDetails: Call.Details) {
        preferencesRepository.markServiceInvoked()

        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: ""
        val responded = AtomicBoolean(false)

        fun respond(result: RuleResult) {
            if (!responded.compareAndSet(false, true)) return

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

        val timeout = Runnable { respond(RuleResult.ALLOW) }
        mainHandler.postDelayed(timeout, EVALUATION_TIMEOUT_MS)

        executor.execute {
            val result = try {
                ruleEngine.evaluate(normalizer.normalize(phoneNumber), ruleRepository.getRules())
            } catch (_: Exception) {
                RuleResult.ALLOW
            }
            mainHandler.removeCallbacks(timeout)
            respond(result)
        }
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}
