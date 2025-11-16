package com.android.agrihealth.data.model.images

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ImageRepositoryFirebase : ImageRepository {
  val MAX_FILE_SIZE: Long = 3 * 1024 * 1024
  val storage = FirebaseStorage.getInstance()
  val resolver: ContentResolver = storage.app.applicationContext.contentResolver

  override suspend fun uploadImage(imageUri: Uri): Result<String> {
    return try {
      val inputStream =
          resolver.openInputStream(imageUri)
              ?: throw IllegalArgumentException("Could not read file with URI $imageUri")
      val byteArray = inputStream.readBytes()
      uploadImage(byteArray)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

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

  override fun isFileTooLarge() {
    TODO("Not yet implemented")
  }

  override fun lowerFileSize() {
    TODO("Not yet implemented")
  }

  override fun resizeImage() {
    TODO("Not yet implemented")
  }

  override fun compressImage() {
    TODO("Not yet implemented")
  }
}
