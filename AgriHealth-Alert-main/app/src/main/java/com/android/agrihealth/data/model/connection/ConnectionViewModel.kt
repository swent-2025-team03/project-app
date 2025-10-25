package com.android.agrihealth.data.model.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConnectionViewModel(
    private val repository: ConnectionRepository = ConnectionRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<ConnectionUiState>(ConnectionUiState.Idle)
    val state = _state.asStateFlow()

    fun generateCode(vetId: String) = viewModelScope.launch {
        _state.value = ConnectionUiState.Loading
        val result = repository.generateCode(vetId)
        _state.value = result.fold(
            onSuccess = { ConnectionUiState.CodeGenerated(it) },
            onFailure = { ConnectionUiState.Error(it.message ?: "Error generating code") }
        )
    }

    fun claimCode(code: String, farmerId: String) = viewModelScope.launch {
        _state.value = ConnectionUiState.Loading
        val result = repository.claimCode(code, farmerId)
        _state.value = result.fold(
            onSuccess = { ConnectionUiState.Connected(it) },
            onFailure = { ConnectionUiState.Error(it.message ?: "Error claiming code") }
        )
    }

    fun resetState() {
        _state.value = ConnectionUiState.Idle
    }
}
