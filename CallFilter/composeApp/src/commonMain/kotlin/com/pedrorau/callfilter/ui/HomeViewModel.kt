package com.pedrorau.callfilter.ui

import androidx.lifecycle.ViewModel
import com.pedrorau.callfilter.model.SystemState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class HomeState(
    val systemState: SystemState = SystemState.NOT_CONFIGURED
)

class HomeViewModel : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    fun updateSystemState(systemState: SystemState) {
        _state.value = _state.value.copy(systemState = systemState)
    }
}
