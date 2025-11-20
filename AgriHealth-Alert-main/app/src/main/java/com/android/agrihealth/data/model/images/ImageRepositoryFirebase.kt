package com.android.agrihealth.data.model.images

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.graphics.scale
import com.android.agrihealth.data.model.images.ImageRepository.Companion.toBitmap
import com.android.agrihealth.ui.user.UserViewModel
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.tasks.await

/**
 * Image repository communicating with Firebase Storage to handle images
 */
class ImageRepositoryFirebase : ImageRepository {
  override val MAX_FILE_SIZE: Long = 3 * 1024 * 1024
  override val IMAGE_MAX_WIDTH: Int = 2048
  override val IMAGE_MAX_HEIGHT: Int = 2048
  override val IMAGE_FORMAT = Bitmap.CompressFormat.JPEG
  val storage = FirebaseStorage.getInstance()

  override suspend fun uploadImage(bytes: ByteArray): Result<String> {
    return try {
      val uid = UserViewModel().user.value.uid
      val fileName = System.currentTimeMillis()
      val childPath = "$uid/$fileName.jpg"
      val imageRef = storage.reference.child(childPath)

      imageRef.putBytes(bytes).await()

      val fullPath = imageRef.path

      Result.success(fullPath)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun downloadImage(path: String): Result<ByteArray> {
    return try {
      val storageRef = storage.reference.child(path)

      val bytes = storageRef.getBytes(MAX_FILE_SIZE).await()

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

  override fun resolveUri(uri: Uri): ByteArray {
    val resolver: ContentResolver = storage.app.applicationContext.contentResolver
    val inputStream =
        resolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Could not read file with URI $uri")
    return inputStream.readBytes()
  }
}
