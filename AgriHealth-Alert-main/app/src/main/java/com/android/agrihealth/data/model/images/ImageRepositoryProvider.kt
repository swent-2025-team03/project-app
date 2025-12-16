package com.android.agrihealth.data.model.images

/** Singleton providing the image repository used throughout the app */
object ImageRepositoryProvider {
  val repository: ImageRepository = ImageRepositoryFirebase()
}
