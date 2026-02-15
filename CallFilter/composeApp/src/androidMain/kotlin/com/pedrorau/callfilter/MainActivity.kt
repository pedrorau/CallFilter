package com.pedrorau.callfilter

import android.Manifest
import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.pedrorau.callfilter.model.SystemState
import com.pedrorau.callfilter.system.SystemStateChecker
import com.pedrorau.callfilter.ui.AppNavigation
import com.pedrorau.callfilter.ui.theme.CallFilterTheme
import androidx.core.net.toUri

class MainActivity : ComponentActivity() {

    private lateinit var systemStateChecker: SystemStateChecker
    private var systemState by mutableStateOf(SystemState.NOT_CONFIGURED)
    private var isBatteryOptimized by mutableStateOf(false)
    private var notificationPermissionResult by mutableStateOf<Boolean?>(null)

    private val requestRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshState()
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationPermissionResult = granted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        systemStateChecker = SystemStateChecker(this)
        refreshState()

        if (systemState == SystemState.NOT_CONFIGURED) {
            requestCallScreeningRole()
        }

        setContent {
            CallFilterTheme {
                AppNavigation(
                    systemState = systemState,
                    isBatteryOptimized = isBatteryOptimized,
                    getNotificationPermissionResult = { notificationPermissionResult },
                    onRequestBatteryOptimization = { requestBatteryOptimization() },
                    onRequestNotificationPermission = { requestNotificationPermission() },
                    onNotificationPermissionConsumed = { notificationPermissionResult = null }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshState()
    }

    private fun refreshState() {
        systemState = systemStateChecker.getSystemState()
        isBatteryOptimized = systemStateChecker.isBatteryOptimized()
    }

    private fun requestCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(ROLE_SERVICE) as? RoleManager ?: return
            if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                requestRoleLauncher.launch(intent)
            }
        }
    }

    @SuppressLint("BatteryLife")
    private fun requestBatteryOptimization() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = "package:$packageName".toUri()
        }
        startActivity(intent)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                notificationPermissionResult = true
            } else {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            notificationPermissionResult = true
        }
    }
}
