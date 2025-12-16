package com.android.agrihealth.data.model.images

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import com.android.agrihealth.data.helper.runWithTimeout
import com.android.agrihealth.data.model.images.ImageRepository.Companion.toBitmap
import com.android.agrihealth.ui.user.UserViewModel
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/** Image repository communicating with Firebase Storage to handle images */
class ImageRepositoryFirebase : ImageRepository {
  override val MAX_FILE_SIZE: Long = 3 * 1024 * 1024
  override val IMAGE_MAX_WIDTH: Int = 2048
  override val IMAGE_MAX_HEIGHT: Int = 2048
  override val IMAGE_FORMAT = Bitmap.CompressFormat.JPEG
  val storage = FirebaseStorage.getInstance()

  override suspend fun uploadImage(bytes: ByteArray): Result<String> {
    return try {
      val uid = UserViewModel().uiState.value.user.uid
      val fileName = System.currentTimeMillis()
      val childPath = "$uid/$fileName.jpg"
      val imageRef = storage.reference.child(childPath)

      imageRef.putBytes(bytes)

      val fullPath = imageRef.path

      Result.success(fullPath)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun downloadImage(path: String): Result<ByteArray> {
    return try {
      val storageRef = storage.reference.child(path)

      val bytes = runWithTimeout(storageRef.getBytes(MAX_FILE_SIZE))

      Result.success(bytes)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override fun isFileTooLarge(bytes: ByteArray): Boolean {
    return bytes.size >= MAX_FILE_SIZE
  }

  override fun reduceFileSize(bytes: ByteArray): ByteArray {
    val initBitmap = bytes.toBitmap()

    val resizedBitmap = resizeImage(initBitmap)

    // Always finish with compression
    return compressImage(resizedBitmap)
  }

  override fun resizeImage(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    if (width <= IMAGE_MAX_WIDTH && height <= IMAGE_MAX_HEIGHT) return bitmap

    val ratio = minOf(IMAGE_MAX_WIDTH.toFloat() / width, IMAGE_MAX_HEIGHT.toFloat() / height)

    val newWidth = (width * ratio).toInt()
    val newHeight = (height * ratio).toInt()

    return bitmap.scale(newWidth, newHeight)
  }

  override fun compressImage(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    var quality = 100
    do {
      stream.reset()
      bitmap.compress(IMAGE_FORMAT, quality, stream)
      quality -= 10
    } while (isFileTooLarge(stream.toByteArray()) && quality > 10)

    return stream.toByteArray()
  }

  // (Mostly) written with the help of an LLM
  override fun resolveUri(uri: Uri): ByteArray {
    val resolver: ContentResolver = storage.app.applicationContext.contentResolver
    val inputStream =
        resolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Could not read file with URI $uri")
    val bytes = inputStream.use { it.readBytes() }

    // Read orientation and physically rotate the bitmap to ensure correct orientation when
    // uploading to Firebase Storage
    val orientation =
        ByteArrayInputStream(bytes).use { stream ->
          val exif = ExifInterface(stream)
          exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        }

    val bitmap = bytes.toBitmap()
    val rotatedBitmap =
        when (orientation) {
          ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
          ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
          ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
          else -> bitmap
        }

    // Now compress the correctly-oriented bitmap
    return ByteArrayOutputStream().use { stream ->
      rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
      stream.toByteArray()
    }
  }

  // Written with the help of an LLM
  private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = android.graphics.Matrix()
    matrix.postRotate(degrees)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
  }
}
