package com.android.agrihealth.ui.common

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

/**
 * Created with the help of an LLM (just moved the code out of the addReportScreen)
 *
 * A composable function that provides image picking functionality from gallery or camera.
 * Handles camera permissions and temporary file creation automatically.
 *
 * @param onImageSelected Callback invoked when an image is successfully selected or captured
 * @return ImagePickerState object containing methods to launch gallery or camera
 */
@Composable
fun rememberImagePickerLauncher(
  onImageSelected: (Uri?) -> Unit
): ImagePickerState {
  val context = LocalContext.current
  var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

  val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) onImageSelected(uri)
  }

  val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture()
  ) { success ->
    if (success && tempPhotoUri != null) {
      onImageSelected(tempPhotoUri)
    }
  }

  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { granted ->
    if (granted) {
      tempPhotoUri?.let { cameraLauncher.launch(it) }
    }
  }

  return remember(galleryLauncher, cameraLauncher, cameraPermissionLauncher) {
    ImagePickerState(
      context = context,
      galleryLauncher = { galleryLauncher.launch("image/*") },
      cameraLauncher = { uri ->
        tempPhotoUri = uri
        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
      }
    )
  }
}

/**
 * State holder for image picker functionality
 */
class ImagePickerState(
  private val context: Context,
  private val galleryLauncher: () -> Unit,
  private val cameraLauncher: (Uri) -> Unit
) {
  /**
   * Launch the gallery picker
   */
  fun launchGallery() {
    galleryLauncher()
  }

  /**
   * Launch the camera
   */
  fun launchCamera() {
    val imageFile = File.createTempFile(
      "temp_image_",
      ".jpg",
      context.cacheDir
    )
    val photoUri = FileProvider.getUriForFile(
      context,
      getFileProviderAuthority(context),
      imageFile
    )
    cameraLauncher(photoUri)
  }

  private fun getFileProviderAuthority(context: Context): String {
    return context.packageName + ".fileprovider"
  }
}
