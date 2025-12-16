package com.android.agrihealth.ui.common

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.android.agrihealth.core.utils.FileProviderUtils
import com.android.agrihealth.data.model.device.openAppPermissionsSettings
import java.io.File

/**
 * Generative AI was used in the making of this file in order to move the original code in
 * AddReportScreen.kt into its own standalone file
 *
 * A complete, self-contained image picker component that handles:
 * - Showing a dialog to choose between gallery and camera
 * - Requesting camera permissions
 * - Opens the app permission settings if camera permission was denied
 * - Creating temporary files for camera captures
 * - Returning the selected/captured image URI
 *
 * Example usage:
 * ```
 * if (showImagePicker) {
 *    ImagePickerDialog(
 *        onDismiss = { showImagePicker = false },
 *        onImageSelected = { uri ->
 *         onPhotoPicked(uri)
 *        }
 *    )
 * }
 * ```
 *
 * @param onDismiss Called when the dialog is dismissed without selecting an image
 * @param onImageSelected Called when an image is successfully selected or captured
 * @param modifier Optional modifier for the dialog
 */
@Composable
fun ImagePickerDialog(
    onDismiss: () -> Unit,
    onImageSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

  // Gallery launcher
  val galleryLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri?
        ->
        uri?.let {
          onImageSelected(it)
          onDismiss()
        }
      }

  // Camera launcher
  val cameraLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success
        ->
        if (success && tempPhotoUri != null) {
          onImageSelected(tempPhotoUri!!)
          onDismiss()
        }
      }

  // Camera permission launcher
  val cameraPermissionLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
          granted ->
        if (granted) {
          tempPhotoUri?.let { cameraLauncher.launch(it) }
        } else {
          Toast.makeText(context, ImagePickerTexts.PERMISSION_REQUIRED, Toast.LENGTH_LONG).show()
          openAppPermissionsSettings(context)
        }
      }

  // The dialog UI
  AlertDialog(
      modifier = modifier.testTag(ImagePickerTestTags.DIALOG),
      onDismissRequest = onDismiss,
      title = { Text(ImagePickerTexts.DIALOG_TITLE) },
      text = { Text(ImagePickerTexts.DIALOG_MESSAGE) },
      confirmButton = {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          TextButton(
              modifier = Modifier.testTag(ImagePickerTestTags.GALLERY_BUTTON),
              onClick = { galleryLauncher.launch("image/*") },
              colors =
                  ButtonDefaults.textButtonColors(
                      contentColor = MaterialTheme.colorScheme.onSurface),
          ) {
            Text(ImagePickerTexts.GALLERY)
          }
          TextButton(
              modifier = Modifier.testTag(ImagePickerTestTags.CAMERA_BUTTON),
              onClick = {
                // Create temp file and launch camera with permission check
                val imageFile = File.createTempFile("temp_image_", ".jpg", context.cacheDir)
                tempPhotoUri =
                    FileProvider.getUriForFile(
                        context, FileProviderUtils.authority(context), imageFile)
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
              },
              colors =
                  ButtonDefaults.textButtonColors(
                      contentColor = MaterialTheme.colorScheme.onSurface),
          ) {
            Text(ImagePickerTexts.CAMERA)
          }
        }
      },
      dismissButton = {
        TextButton(
            modifier = Modifier.testTag(ImagePickerTestTags.CANCEL_BUTTON),
            onClick = onDismiss,
            colors =
                ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
        ) {
          Text(ImagePickerTexts.CANCEL)
        }
      },
  )
}

/** Text constants for the image picker */
object ImagePickerTexts {
  const val DIALOG_TITLE = "Select Image Source"
  const val DIALOG_MESSAGE = "Choose from gallery or take a new photo."
  const val GALLERY = "Gallery"
  const val CAMERA = "Camera"
  const val CANCEL = "Cancel"
  const val PERMISSION_REQUIRED = "Camera permission is required to take a photo"
}

/** Test tags for the image picker */
object ImagePickerTestTags {
  const val DIALOG = "imagePickerDialog"
  const val GALLERY_BUTTON = "imagePickerGallery"
  const val CAMERA_BUTTON = "imagePickerCamera"
  const val CANCEL_BUTTON = "imagePickerCancel"
}
