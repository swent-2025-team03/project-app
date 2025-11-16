package com.android.agrihealth.data.model.images

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.graphics.scale
import com.android.agrihealth.data.model.images.ImageRepository.Companion.toBitmap
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.tasks.await

class ImageRepositoryFirebase : ImageRepository {
  val MAX_FILE_SIZE: Long = 3 * 1024 * 1024
  val IMAGE_MAX_WIDTH: Int = 2048
  val IMAGE_MAX_HEIGHT: Int = 2048
  val IMAGE_FORMAT = Bitmap.CompressFormat.JPEG
  val storage = FirebaseStorage.getInstance()

  /*override suspend fun uploadImage(imageUri: Uri): Result<String> {
    return try {
      val inputStream =
          resolver.openInputStream(imageUri)
              ?: throw IllegalArgumentException("Could not read file with URI $imageUri")
      val byteArray = inputStream.readBytes()
      uploadImage(byteArray)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }*/

  override suspend fun uploadImage(byteArray: ByteArray): Result<String> {
    return try {
      val fileName = System.currentTimeMillis()
      val imageRef = storage.reference.child("images/$fileName.jpg")

      imageRef.putBytes(byteArray).await()

      val path = imageRef.path

      Result.success(path)
    } catch (e: Exception) {
      Log.e("ImageRepository", "Failed to upload image: ${e.message}")
      Result.failure(e)
    }
  }

  override suspend fun downloadImage(path: String): Result<ByteArray> {
    return try {
      val storageRef = storage.reference.child(path)

      val byteArray = storageRef.getBytes(MAX_FILE_SIZE).await()

      Result.success(byteArray)
    } catch (e: Exception) {
      Log.e("ImageRepository", "Failed to download image: ${e.message}")
      Result.failure(e)
    }
  }

  override fun isFileTooLarge(bytes: ByteArray): Boolean {
    return bytes.size >= MAX_FILE_SIZE
  }

  override fun reduceFileSize(bytes: ByteArray): ByteArray {
    val initBitmap = bytes.toBitmap()
    val resizedBitmap = resizeImage(initBitmap)
    return compressImage(resizedBitmap)
  }

  override fun resizeImage(bitmap: Bitmap): Bitmap {
    val ratio =
        minOf(IMAGE_MAX_WIDTH.toFloat() / bitmap.width, IMAGE_MAX_HEIGHT.toFloat() / bitmap.height)

    val newWidth = (bitmap.width * ratio).toInt()
    val newHeight = (bitmap.height * ratio).toInt()

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
