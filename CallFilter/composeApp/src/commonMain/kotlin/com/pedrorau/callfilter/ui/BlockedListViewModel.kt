package com.pedrorau.callfilter.ui

import androidx.lifecycle.ViewModel
import com.pedrorau.callfilter.model.BlockedNumber
import com.pedrorau.callfilter.repository.BlockedNumberRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class BlockedListState(
    val numbers: List<BlockedNumber> = emptyList(),
    val inputNumber: String = ""
)

class BlockedListViewModel(
    private val repository: BlockedNumberRepository = BlockedNumberRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(BlockedListState())
    val state: StateFlow<BlockedListState> = _state

    fun loadNumbers() {
        _state.value = _state.value.copy(numbers = repository.getBlockedNumbers())
    }

    fun updateInput(number: String) {
        _state.value = _state.value.copy(inputNumber = number)
    }

    fun addNumber() {
        val number = _state.value.inputNumber.trim()
        if (number.isNotEmpty()) {
            repository.addNumber(number)
            _state.value = _state.value.copy(inputNumber = "")
            loadNumbers()
        }
    }

    fun removeNumber(id: String) {
        repository.removeNumber(id)
        loadNumbers()
    }
}
