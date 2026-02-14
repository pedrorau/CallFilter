package com.pedrorau.callfilter.ui

import androidx.lifecycle.ViewModel
import com.pedrorau.callfilter.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class OnboardingState(
    val currentPage: Int = 0,
    val totalPages: Int = 3
)

class OnboardingViewModel(
    private val preferencesRepository: PreferencesRepository = PreferencesRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state

    fun nextPage() {
        val current = _state.value
        if (current.currentPage < current.totalPages - 1) {
            _state.value = current.copy(currentPage = current.currentPage + 1)
        }
    }

    fun previousPage() {
        val current = _state.value
        if (current.currentPage > 0) {
            _state.value = current.copy(currentPage = current.currentPage - 1)
        }
    }

    fun completeOnboarding() {
        preferencesRepository.setOnboardingCompleted(true)
    }

    fun isOnboardingCompleted(): Boolean {
        return preferencesRepository.isOnboardingCompleted()
    }
}
