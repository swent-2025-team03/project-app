package com.android.agrihealth.data.model.images

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

interface ImageRepository {
  /**
   * Uploads the provided byte array as an image to the backend storage to be downloaded later.
   * Returns the storage path
   */
  suspend fun uploadImage(byteArray: ByteArray): Result<String>

  /**
   * Downloads an image from the backend storage at the given backend storage path. Returns a byte
   * array of the image data, can be converted to a bitmap with toBitmap()
   */
  suspend fun downloadImage(path: String): Result<ByteArray>

  /** Returns whether the given image is too big to be uploaded to the storage */
  fun isFileTooLarge(bytes: ByteArray): Boolean

  /**
   * Reduces the file size of the provided image by compressing, resizing, ... Returns the modified
   * image
   */
  fun reduceFileSize(bytes: ByteArray): ByteArray

  /**
   * Resizes the provided image to a lower resolution to reduce file size. Returns the modified
   * image
   */
  fun resizeImage(bitmap: Bitmap): Bitmap

  /**
   * Compresses the provided image to a lower quality to reduce file size. Returns the modified
   * image
   */
  fun compressImage(bitmap: Bitmap): ByteArray

  /** Gets the content from the specified URI and returns the corresponding bytes */
  fun resolveUri(uri: Uri): ByteArray

  companion object {
    /** Converts a byte array to a bitmap */
    fun ByteArray.toBitmap(): Bitmap = BitmapFactory.decodeByteArray(this, 0, this.size)

    /**
     * Converts a byte array result to a bitmap result. Made to be used like:
     * downloadImage(path).toBitmap()
     */
    fun Result<ByteArray>.toBitmap(): Result<Bitmap> =
        fold(
            onSuccess = { byteArray -> Result.success(byteArray.toBitmap()) },
            onFailure = { e -> Result.failure(e) })
  }
}
