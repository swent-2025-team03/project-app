package com.android.agrihealth.utils

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

object TestAssetUtils {

  // Constants for commonly used test files
  const val FAKE_PHOTO_FILE = "report_image_cat.jpg"

  fun getUriFrom(assetName: String): Uri {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val cacheFile = File(instrumentation.targetContext.cacheDir, assetName)

    instrumentation.context.assets.open(assetName).use { input ->
      cacheFile.outputStream().use { output ->
        input.copyTo(output)
      }
    }

    return Uri.fromFile(cacheFile)
  }

  /**
   *  Clears all files from cache.
   *
   *  Use this after all tests are run so that tests have no side effects on each others.
   */
  fun cleanupTestAssets() {
    val cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
    cacheDir.listFiles()?.forEach { file ->
      file.delete()
    }
  }
}