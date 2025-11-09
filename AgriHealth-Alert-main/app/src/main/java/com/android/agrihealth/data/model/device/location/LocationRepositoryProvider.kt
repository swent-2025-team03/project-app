package com.android.agrihealth.data.model.device.location

import android.annotation.SuppressLint

/**
 * Singleton to have a centralized repository. Not meant to be used directly, use the ViewModel
 * instead
 */
object LocationRepositoryProvider {
  @SuppressLint("StaticFieldLeak") lateinit var repository: LocationRepository
}
