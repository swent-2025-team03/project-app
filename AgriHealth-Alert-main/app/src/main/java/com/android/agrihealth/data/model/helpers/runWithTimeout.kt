package com.android.agrihealth.data.model.helpers

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

const val TIMEOUT = 2_000L

/**
 * Helper function to let Tasks timeout if they are used in poor connections settings or if they are
 * offline.
 */
suspend fun <T> runWithTimeout(block: Task<T>): T {
  return withTimeout(TIMEOUT) { block.await() }
}
