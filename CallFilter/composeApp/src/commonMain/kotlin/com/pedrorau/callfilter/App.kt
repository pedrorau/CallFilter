package com.pedrorau.callfilter

import androidx.compose.runtime.Composable
import com.pedrorau.callfilter.model.SystemState
import com.pedrorau.callfilter.ui.AppNavigation
import com.pedrorau.callfilter.ui.theme.CallFilterTheme

@Composable
fun App(systemState: SystemState = SystemState.NOT_CONFIGURED) {
    CallFilterTheme {
        AppNavigation(systemState = systemState)
    }
}
