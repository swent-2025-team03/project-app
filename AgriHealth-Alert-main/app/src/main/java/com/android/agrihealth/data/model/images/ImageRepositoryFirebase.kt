package com.android.agrihealth.data.model.images

class ImageRepositoryFirebase : ImageRepository {
    val MAX_FILE_SIZE = 3 * 1024 * 1024

    override suspend fun uploadImage() {
        TODO("Not yet implemented")
    }

    override suspend fun downloadImage() {
        TODO("Not yet implemented")
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