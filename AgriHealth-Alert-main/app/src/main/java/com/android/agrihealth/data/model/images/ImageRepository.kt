package com.android.agrihealth.data.model.images

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

/**
 * Interface to communicate with the photos backend, to upload and download images, and handle image data
 */
interface ImageRepository {
  val MAX_FILE_SIZE: Long
  val IMAGE_MAX_WIDTH: Int
  val IMAGE_MAX_HEIGHT: Int
  val IMAGE_FORMAT: Bitmap.CompressFormat

  /**
   * Uploads the provided byte array as an image to the backend storage to be downloaded later.
   * Returns the storage path
   */
  suspend fun uploadImage(bytes: ByteArray): Result<String>

  /**
   * Downloads an image from the backend storage at the given backend storage path. Returns a byte
   * array of the image data, result can be directly converted to a bitmap with toBitmap()
   */
  suspend fun downloadImage(path: String): Result<ByteArray>

  /** Returns whether the given image is too big to be uploaded to the storage */
  fun isFileTooLarge(bytes: ByteArray): Boolean

  /**
   * Reduces the file size of the provided image by resizing, compressing, ... Returns the modified
   * image
   */
  fun reduceFileSize(bytes: ByteArray): ByteArray

  /**
   * (Intended for internal usage) Resizes the provided image to a lower resolution to reduce file size. Leaves the image
   * unchanged if it was already small enough. Returns the modified image
   */
  fun resizeImage(bitmap: Bitmap): Bitmap

  /**
   * (Intended for internal usage) Compresses the provided image to a lower quality to reduce file size. Returns the modified
   * image as a byte array
   */
  fun compressImage(bitmap: Bitmap): ByteArray

  /** Gets the content from the specified URI and returns the corresponding byte array */
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
