package com.android.agrihealth.testhelpers

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.android.agrihealth.testhelpers.fakes.FakeImageRepository
import java.io.File

/**
 * Utility class used during testing when dealing with test assets such as pictures, videos, etc...
 */
object FileTestUtils {

  // Constants for commonly used test files
  /** Fake photo used when creating a new report */
  const val TEST_IMAGE = "report_image_cat.jpg"
  const val FAKE_PHOTO_PATH = "some/fake/path/to/photo.jpg"

  /**
   * Returns the Uri of a given file. File must be located in "androidTest/assets"
   *
   * @param fileName Name of the file
   * @return The Uri of the given file
   */
  fun getUriFrom(fileName: String): Uri {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val cacheFile = File(instrumentation.targetContext.cacheDir, fileName)

    instrumentation.context.assets.open(fileName).use { input ->
      cacheFile.outputStream().use { output -> input.copyTo(output) }
    }

    return Uri.fromFile(cacheFile)
  }

  /** Retrieves the data of the provided image path, using its URI, returning bytes */
  fun getBytesFromFile(fileName: String): ByteArray {
    val uri = getUriFrom(fileName)
    val context = InstrumentationRegistry.getInstrumentation().context
    return context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
  }

  fun addPlaceholderPhotoToRepository(imageRepository: FakeImageRepository) {
    val fakePhotoBytes = getBytesFromFile(TEST_IMAGE)
    imageRepository.forceUploadImage(fakePhotoBytes)
  }

  /**
   * Clears all files from cache.
   *
   * Use this after all tests are run so that tests have no side effects on each others.
   */
  fun cleanupTestAssets() {
    val cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
    cacheDir.listFiles()?.forEach { file -> file.delete() }
  }
}
