package com.android.agrihealth.core.utils

import android.content.Context

/** Various reusable utility functions when dealing with the Android file system */
object FileProviderUtils {

  /**
   * Helper function to get the FileProvider authority
   *
   * Note: This must be the *exact* same as seen in AndroidManifest.xml, in the <provider> tag,
   * under "android:authorities"
   */
  fun authority(context: Context): String {
    return context.packageName + ".fileprovider"
  }
}
