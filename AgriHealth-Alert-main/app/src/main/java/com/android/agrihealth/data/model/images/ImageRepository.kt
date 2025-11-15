package com.android.agrihealth.data.model.images

interface ImageRepository {
    /**
     * Uploads the provided image to the backend storage to be downloaded later. Returns the URL/URI
     */
    suspend fun uploadImage()

    /**
     * Downloads an image from the backend storage at the given URL/URI. Returns a bitmap
     */
    suspend fun downloadImage()

    /**
     * Returns whether the given image is too big to be uploaded to the storage
     */
    fun isFileTooLarge()

    /**
     * Reduces the file size of the provided image by compressing, resizing, ... Returns the modified image
     */
    fun lowerFileSize()

    /**
     * Resizes the provided image to a lower resolution to reduce file size. Returns the modified image
     */
    fun resizeImage()

    /**
     * Compresses the provided image to a lower quality to reduce file size. Returns the modified image
     */
    fun compressImage()
}