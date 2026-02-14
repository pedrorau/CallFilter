package com.pedrorau.callfilter

import android.Manifest
import android.app.role.RoleManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
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

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "CallFilterMain"
    }

    private lateinit var systemStateChecker: SystemStateChecker
    private var systemState by mutableStateOf(SystemState.NOT_CONFIGURED)

    private val requestRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        systemState = systemStateChecker.getSystemState()
        requestPhonePermissions()
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.forEach { (permission, granted) ->
            Log.d(TAG, "Permission $permission granted: $granted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        systemStateChecker = SystemStateChecker(this)
        systemState = systemStateChecker.getSystemState()

        if (systemState == SystemState.NOT_CONFIGURED) {
            requestCallScreeningRole()
        } else {
            requestPhonePermissions()
        }

        setContent {
            CallFilterTheme {
                AppNavigation(systemState = systemState)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        systemState = systemStateChecker.getSystemState()
        Log.d(TAG, "SystemState: $systemState")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(ROLE_SERVICE) as? RoleManager
            val hasRole = roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) ?: false
            Log.d(TAG, "ROLE_CALL_SCREENING held: $hasRole")
        }
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

    private fun requestPhonePermissions() {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.READ_PHONE_STATE)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.ANSWER_PHONE_CALLS)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissions.isNotEmpty()) {
            Log.d(TAG, "Requesting permissions: $permissions")
            requestPermissionsLauncher.launch(permissions.toTypedArray())
        }
    }
}
