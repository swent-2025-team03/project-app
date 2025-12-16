package com.android.agrihealth.ui.utils

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.android.agrihealth.data.model.images.ImageViewModel
import com.android.agrihealth.ui.profile.LocalPhotoDisplay
import com.android.agrihealth.ui.profile.RemotePhotoDisplay
import com.android.agrihealth.ui.report.AddReportDialogTexts
import com.mr0xf00.easycrop.CropError
import com.mr0xf00.easycrop.CropResult
import com.mr0xf00.easycrop.ImageCropper
import com.mr0xf00.easycrop.crop
import com.mr0xf00.easycrop.rememberImageCropper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// TODO Write kdoc
sealed interface PhotoUi {
  data object Empty : PhotoUi
  data class Remote(val url: String) : PhotoUi
  data class Local(val bytes: ByteArray) : PhotoUi
}

object ProfilePictureComponentsTestTags {
  const val PROFILE_PICTURE = "ProfilePicture"
  const val PROFILE_PICTURE_EDIT_BUTTON = "ProfilePictureEditButton"
  const val ERROR_DIALOG_OK_BUTTON = "ProfilePictureErrorDialogOkButton"
  const val ERROR_DIALOG = "ProfilePictureErrorDialog"
}

object ProfilePictureComponentsTexts {
  const val DIALOG_LOADING_ERROR = "The supplied image is invalid, not supported, or you don't have the required permissions to read it..."
  const val DIALOG_SAVING_ERROR = "The result could not be saved. The selected area is likely to big, try selecting a smaller area..."
  const val DIALOG_TITLE = "Error!"
}

typealias ImageCropLauncher = (Uri) -> Unit

@Composable
fun rememberDefaultImageCropLauncher(
  imageCropper: ImageCropper,
  cropMaxSize: IntSize = IntSize(4096, 4096),
  scope: CoroutineScope,
  onCropSuccess: (ByteArray) -> Unit,
  onCropError: (String) -> Unit,
  onCropCancelled: () -> Unit = {},
): ImageCropLauncher {
  val context = LocalContext.current

  // Not sure what keys to put there (if any at all?)
  return remember(imageCropper, onCropSuccess, onCropError, onCropCancelled) {
    { uri: Uri ->
      scope.launch {
        val bitmap = uri.toBitmap(context).asImageBitmap()
        when (val result = imageCropper.crop(cropMaxSize, bmp = bitmap)) {
          is CropResult.Cancelled -> onCropCancelled()
          is CropError -> onCropError(
            when (result) {
              CropError.LoadingError -> ProfilePictureComponentsTexts.DIALOG_LOADING_ERROR
              CropError.SavingError -> ProfilePictureComponentsTexts.DIALOG_SAVING_ERROR
            }
          )
          is CropResult.Success -> onCropSuccess(result.bitmap.toByteArray())
        }
      }
    }
  }
}


@Composable
fun EditableProfilePictureWithUI(
  photo: PhotoUi,
  isEditable: Boolean,
  imageViewModel: ImageViewModel,
  modifier: Modifier = Modifier,
  imageSize: Dp = 120.dp,
  onPhotoPicked: (ByteArray) -> Unit,
  onPhotoRemoved: () -> Unit,
  launchImageCropper: ImageCropLauncher? = null,
) {
  val scope = rememberCoroutineScope()

  var showImagePicker by rememberSaveable { mutableStateOf(false) }
  var showErrorDialog by rememberSaveable { mutableStateOf(false) }
  var errorDialogMessage by rememberSaveable { mutableStateOf("") }

  val imageCropper = rememberImageCropper()
  val defaultLauncher: ImageCropLauncher = rememberDefaultImageCropLauncher(
    imageCropper = imageCropper,
    scope = scope,
    onCropSuccess = onPhotoPicked,
    onCropError = { errorMessage ->
      showErrorDialog = true
      errorDialogMessage = errorMessage
    },
  )

  val effectiveLauncher = launchImageCropper ?: defaultLauncher

  val croppingIsOngoing = imageCropper.cropState != null

  EditableProfilePicture(
    photo = photo,
    isEditable = isEditable,
    imageViewModel = imageViewModel,
    modifier = modifier,
    imageSize = imageSize,
    onPhotoPicked = { showImagePicker = true },
    onPhotoRemoved = onPhotoRemoved
  )

  if (showErrorDialog) {
    ErrorDialog(dialogTitle = ProfilePictureComponentsTexts.DIALOG_TITLE, errorMessage = errorDialogMessage, onDismiss = { showErrorDialog = false })
  }

  if (croppingIsOngoing) {
    ShowImageCropperDialog(imageCropper)
  }

  if (showImagePicker) {
    ImagePickerDialog(
      onDismiss = { showImagePicker = false },
      onImageSelected = { uri ->
        showImagePicker = false
        effectiveLauncher(uri)
      }
    )
  }
}

// TODO Add support for square pictures (so office profile pictures can be square)
@Composable
fun EditableProfilePicture(
  photo: PhotoUi,
  isEditable: Boolean,
  imageViewModel: ImageViewModel,
  modifier: Modifier = Modifier,
  imageSize: Dp = 120.dp,
  onPhotoPicked: () -> Unit,
  onPhotoRemoved: () -> Unit,
) {
  Box(
    modifier = modifier.size(imageSize),
    contentAlignment = Alignment.Center,
  ) {
    when (photo) {
      is PhotoUi.Remote -> RemotePhotoDisplay(
        photoURL = photo.url,
        imageViewModel = imageViewModel,
        modifier = Modifier.size(imageSize).clip(CircleShape).testTag(ProfilePictureComponentsTestTags.PROFILE_PICTURE),
        contentDescription = "Photo",
        showPlaceHolder = true
      )
      is PhotoUi.Local -> LocalPhotoDisplay(
        photoByteArray = photo.bytes,
        modifier = Modifier.size(imageSize).clip(CircleShape).testTag(ProfilePictureComponentsTestTags.PROFILE_PICTURE),
        showPlaceHolder = true
      )
      PhotoUi.Empty -> LocalPhotoDisplay(
        photoByteArray = null,
        modifier = Modifier.size(imageSize).clip(CircleShape).testTag(ProfilePictureComponentsTestTags.PROFILE_PICTURE),
        showPlaceHolder = true
      )
    }

    if (isEditable) {
      val removeMode = photo != PhotoUi.Empty
      FloatingActionButton(
        onClick = { if (removeMode) onPhotoRemoved() else onPhotoPicked() },
        modifier = Modifier
          .size(40.dp)
          .align(Alignment.BottomEnd)
          .testTag(ProfilePictureComponentsTestTags.PROFILE_PICTURE_EDIT_BUTTON),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
      ) {
        Icon(
          imageVector = if (removeMode) Icons.Default.Clear else Icons.Default.CameraAlt,
          contentDescription = if (removeMode) "Remove photo" else "Add photo",
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

// TODO: Place this in its own file, as it is a copy of the one in AddReportScreen.kt
/**
 * A dialog shown when an error happened and a report couldn't be created
 *
 * @param dialogTitle Title of the dialog
 * @param errorMessage The error message received when attempting to create a report
 * @param onDismiss Executed when the user dismisses the dialog
 */
@Composable
fun ErrorDialog(dialogTitle: String, errorMessage: String, onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag(ProfilePictureComponentsTestTags.ERROR_DIALOG_OK_BUTTON),
        colors =
          ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface)) {
        Text(AddReportDialogTexts.OK)
      }
    },
    title = { Text(dialogTitle) },
    text = { Text(errorMessage) },
    modifier = Modifier.testTag(ProfilePictureComponentsTestTags.ERROR_DIALOG))
}
