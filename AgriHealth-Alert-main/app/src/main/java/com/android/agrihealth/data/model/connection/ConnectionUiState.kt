package com.android.agrihealth.data.model.connection

sealed class ConnectionUiState {
  object Idle : ConnectionUiState()

  object Loading : ConnectionUiState()

  data class CodeGenerated(val code: String) : ConnectionUiState()

  data class Connected(val vetId: String) : ConnectionUiState()

  data class Error(val message: String) : ConnectionUiState()
}
