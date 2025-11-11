package com.android.agrihealth.utils

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

/**
 *    Utility class used during testing when dealing with test assets such as pictures, videos, etc...
 */
object TestAssetUtils {

  // Constants for commonly used test files
  /** Fake photo used when creating a new report */
  const val FAKE_PHOTO_FILE = "report_image_cat.jpg"

  /**
   *   Returns the Uri of a given file. File must be located in "androidTest/assets"
   *
   *   @property fileName Name of the file
   *   @return The Uri of the given file
   */
  fun getUriFrom(fileName: String): Uri {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val cacheFile = File(instrumentation.targetContext.cacheDir, fileName)

    instrumentation.context.assets.open(fileName).use { input ->
      cacheFile.outputStream().use { output -> input.copyTo(output) }
    }

    return Uri.fromFile(cacheFile)
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
