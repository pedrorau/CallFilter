package com.pedrorau.callfilter.system

import android.app.role.RoleManager
import android.content.Context
import android.os.Build
import com.pedrorau.callfilter.model.SystemState
import com.pedrorau.callfilter.repository.PreferencesRepository

class SystemStateChecker(
    private val context: Context,
    private val preferencesRepository: PreferencesRepository = PreferencesRepository()
) {

    fun getSystemState(): SystemState {
        if (!isCallScreeningRoleHeld()) {
            return SystemState.NOT_CONFIGURED
        }
        if (isPotentiallyIncompatible()) {
            return SystemState.POTENTIALLY_INCOMPATIBLE
        }
        return SystemState.PROTECTION_ACTIVE
    }

    private fun isCallScreeningRoleHeld(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            ?: return false
        return roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    private fun isPotentiallyIncompatible(): Boolean {
        if (preferencesRepository.isServiceEverInvoked()) return false
        val manufacturer = Build.MANUFACTURER.lowercase()
        return manufacturer == "samsung" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
}
