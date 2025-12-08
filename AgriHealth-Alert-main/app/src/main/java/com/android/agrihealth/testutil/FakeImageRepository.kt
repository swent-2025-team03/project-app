package com.android.agrihealth.testutil

import android.graphics.Bitmap
import android.net.Uri
import com.android.agrihealth.data.model.images.ImageRepository
import kotlinx.coroutines.delay

class FakeImageRepository(private var connectionIsFrozen: Boolean = true) : ImageRepository {

  override val MAX_FILE_SIZE: Long
    get() = 10 * 1024 * 1024

  override val IMAGE_MAX_WIDTH: Int
    get() = 4096

  override val IMAGE_MAX_HEIGHT: Int
    get() = 4096

  override val IMAGE_FORMAT: Bitmap.CompressFormat
    get() = Bitmap.CompressFormat.JPEG

  private var photoStored: ByteArray? = null
  private var doesThrowError = false

  fun makeRepoWorkAgain() {
    doesThrowError = false
  }

  fun makeRepoThrowError() {
    doesThrowError = true
  }

  fun freezeRepoConnection() {
    connectionIsFrozen = true
  }

  fun unfreezeRepoConnection() {
    connectionIsFrozen = false
  }

  fun forceUploadImage(bytes: ByteArray) {
    photoStored = bytes
  }

  override suspend fun uploadImage(bytes: ByteArray): Result<String> {
    throw NotImplementedError()
  }

  override suspend fun downloadImage(path: String): Result<ByteArray> {
    // Wait while connection is frozen
    while (connectionIsFrozen) {
      delay(100) // Check every 100ms to avoid busy-waiting
    }

    return if (doesThrowError || photoStored == null) {
      Result.failure(Exception("Connection to repository failed!"))
    } else {
      Result.success(photoStored!!)
    }
  }

  override fun isFileTooLarge(bytes: ByteArray): Boolean {
    TODO("Not implemented")
  }

  override fun reduceFileSize(bytes: ByteArray): ByteArray {
    TODO("Not implemented")
  }

  override fun resizeImage(bitmap: Bitmap): Bitmap {
    TODO("Not implemented")
  }

  override fun compressImage(bitmap: Bitmap): ByteArray {
    TODO("Not implemented")
  }

  override fun resolveUri(uri: Uri): ByteArray {
    TODO("Not implemented")
  }
}
