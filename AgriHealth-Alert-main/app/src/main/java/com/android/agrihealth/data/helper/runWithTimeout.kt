package com.android.agrihealth.data.helper

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

object TimeoutConstant {
  const val TIMEOUT = 2_000L
}

suspend fun <T> runWithTimeout(block: Task<T>) {
  withTimeout(TimeoutConstant.TIMEOUT) { block.await() }
}
