package com.android.agrihealth.ui.loading

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow

suspend fun <S> MutableStateFlow<S>.withLoadingState(
    applyLoading: (S, Boolean) -> S,
    block: suspend () -> Unit
) {
  // Log avant passage à isLoading=true
  Log.d("DEBUG_LOADING", "withLoadingState: setting isLoading=true (before)")
  value = applyLoading(value, true)

  try {
    block()
  } catch (e: Exception) {
    throw e
  } finally {
    // Log avant passage à isLoading=false
    Log.d("DEBUG_LOADING", "withLoadingState: setting isLoading=false (finally)")
    value = applyLoading(value, false)
  }
}
