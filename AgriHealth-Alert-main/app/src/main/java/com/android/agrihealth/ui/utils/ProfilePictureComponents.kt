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

/**
 * Used to give the [EditableProfilePicture] the photo it must display and it's source (to decide
 * how to display it)
 */
sealed interface PhotoUi {
  /** An empty profile picture */
  data object Empty : PhotoUi
  /** A profile picture stored remote at address [url] */
  data class Remote(val url: String) : PhotoUi
  /** A currently locally selected profile picture the user just recently chose */
  data class Local(val bytes: ByteArray) : PhotoUi
}

/** The various test tags associated with the components of a profile picture */
object ProfilePictureComponentsTestTags {
  const val PROFILE_PICTURE = "ProfilePicture"
  const val PROFILE_PICTURE_EDIT_BUTTON = "ProfilePictureEditButton"
  const val ERROR_DIALOG_OK_BUTTON = "ProfilePictureErrorDialogOkButton"
  const val ERROR_DIALOG = "ProfilePictureErrorDialog"
}

/** The various texts used by the components of a profile picture */
object ProfilePictureComponentsTexts {
  const val DIALOG_LOADING_ERROR =
      "The supplied image is invalid, not supported, or you don't have the required permissions to read it..."
  const val DIALOG_SAVING_ERROR =
      "The result could not be saved. The selected area is likely to big, try selecting a smaller area..."
  const val DIALOG_TITLE = "Error!"
}

/** To make type more clear */
typealias ImageCropperLauncher = (Uri) -> Unit

/**
 * Initializes and remembers an image cropper created by default
 *
 * @param imageCropper The supplied [ImageCropper]
 * @param cropMaxSize The maximum size of the crop region. Prefer leaving it as is
 * @param scope The scope at which new coroutines will be created
 * @param onCropSuccess Called when the user succesfully chose and cropped a photo
 * @param onCropError Called when the user chose a picture and tried to crop if but it failed
 * @param onCropCancelled Called when the user cancels the crop (by dismissing the window for
 *   example)
 */
@Composable
fun rememberDefaultImageCropperLauncher(
    imageCropper: ImageCropper,
    cropMaxSize: IntSize = IntSize(8192, 8192),
    scope: CoroutineScope,
    onCropSuccess: (ByteArray) -> Unit,
    onCropError: (String) -> Unit,
    onCropCancelled: () -> Unit = {},
): ImageCropperLauncher {
  val context = LocalContext.current

  // Not sure what keys to put there (if any at all?)
  return remember(imageCropper, onCropSuccess, onCropError, onCropCancelled) {
    { uri: Uri ->
      scope.launch {
        val bitmap = uri.toBitmap(context).asImageBitmap()
        when (val result = imageCropper.crop(cropMaxSize, bmp = bitmap)) {
          is CropResult.Cancelled -> onCropCancelled()
          is CropError ->
              onCropError(
                  when (result) {
                    CropError.LoadingError -> ProfilePictureComponentsTexts.DIALOG_LOADING_ERROR
                    CropError.SavingError -> ProfilePictureComponentsTexts.DIALOG_SAVING_ERROR
                  })
          is CropResult.Success -> onCropSuccess(result.bitmap.toByteArray())
        }
      }
    }
  }
}

/**
 * A profile picture is displayed, with a small icon that allows the user to either remove their
 * existing profile picture or add a enw one. When the user clicks on the button when no photo is
 * displayed, the user is asked to upload a photo from either device storage or camera, and then the
 * user is asked to crop the image into a circle (as the profile picture is a circle)
 *
 * @param photo The picture to display and its source
 * @param isEditable true to allow the user to edit the displayed photo (i.e remove it or add one),
 *   false to make the profile picture read-only
 * @param imageViewModel The [ImageViewModel] used to download / upload the remote profile picture
 * @param modifier A modifier applied to the composable
 * @param imageSize The size of the profile picture, in [dp]
 * @param onPhotoPicked Called when the user picked (and cropped) a photo. This lambda contains the
 *   chosen photo as an argument
 * @param onPhotoRemoved Called when the user decides to remove the current profile picture
 * @param imageCropperLauncher The launcher of the image cropper. Specify one for testing, prefer
 *   leaving this empty normally
 */
@Composable
fun EditableProfilePictureWithUI(
    photo: PhotoUi,
    isEditable: Boolean,
    imageViewModel: ImageViewModel,
    modifier: Modifier = Modifier,
    imageSize: Dp = 120.dp,
    onPhotoPicked: (ByteArray) -> Unit,
    onPhotoRemoved: () -> Unit,
    imageCropperLauncher: ImageCropperLauncher? = null,
) {
  val scope = rememberCoroutineScope()

  var showImagePicker by rememberSaveable { mutableStateOf(false) }
  var showErrorDialog by rememberSaveable { mutableStateOf(false) }
  var errorDialogMessage by rememberSaveable { mutableStateOf("") }

  val imageCropper = rememberImageCropper()
  val defaultLauncher: ImageCropperLauncher =
      rememberDefaultImageCropperLauncher(
          imageCropper = imageCropper,
          scope = scope,
          onCropSuccess = onPhotoPicked,
          onCropError = { errorMessage ->
            showErrorDialog = true
            errorDialogMessage = errorMessage
          },
      )

  val effectiveLauncher = imageCropperLauncher ?: defaultLauncher

  val croppingIsOngoing = imageCropper.cropState != null

  EditableProfilePicture(
      photo = photo,
      isEditable = isEditable,
      imageViewModel = imageViewModel,
      modifier = modifier,
      imageSize = imageSize,
      onPhotoPicked = { showImagePicker = true },
      onPhotoRemoved = onPhotoRemoved)

  if (showErrorDialog) {
    ErrorDialog(
        dialogTitle = ProfilePictureComponentsTexts.DIALOG_TITLE,
        errorMessage = errorDialogMessage,
        onDismiss = { showErrorDialog = false })
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
        })
  }
}

/**
 * A profile picture is displayed, with a small icon that allows the user to either remove their
 * existing profile picture or add a enw one. What happens when the user clicks on the button must
 * be defined with [onPhotoPicked] and [onPhotoRemoved] respectfully. Prefer using
 * [EditableProfilePictureWithUI] for photo picker and image cropper support
 *
 * @param photo The picture to display and its source
 * @param isEditable true to allow the user to edit the displayed photo (i.e remove it or add one),
 *   false to make the profile picture read-only
 * @param imageViewModel The [ImageViewModel] used to download / upload the remote profile picture
 * @param modifier A modifier applied to the composable
 * @param imageSize The size of the profile picture, in [dp]
 * @param onPhotoPicked Called when the user picked (and cropped) a photo. This lambda contains the
 *   chosen photo as an argument
 * @param onPhotoRemoved Called when the user decides to remove the current profile picture
 */
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
      modifier = modifier.size(imageSize).testTag(ProfilePictureComponentsTestTags.PROFILE_PICTURE),
      contentAlignment = Alignment.Center,
  ) {
    when (photo) {
      is PhotoUi.Remote ->
          RemotePhotoDisplay(
              photoURL = photo.url,
              imageViewModel = imageViewModel,
              modifier =
                  Modifier.size(imageSize)
                      .clip(CircleShape),
              contentDescription = "Photo",
              showPlaceHolder = true)
      is PhotoUi.Local ->
          LocalPhotoDisplay(
              photoByteArray = photo.bytes,
              modifier =
                  Modifier.size(imageSize)
                      .clip(CircleShape),
              showPlaceHolder = true)
      PhotoUi.Empty ->
          LocalPhotoDisplay(
              photoByteArray = null,
              modifier =
                  Modifier.size(imageSize)
                      .clip(CircleShape),
              showPlaceHolder = true)
    }

    if (isEditable) {
      val removeMode = photo != PhotoUi.Empty
      FloatingActionButton(
          onClick = { if (removeMode) onPhotoRemoved() else onPhotoPicked() },
          modifier =
              Modifier.size(40.dp)
                  .align(Alignment.BottomEnd)
                  .testTag(ProfilePictureComponentsTestTags.PROFILE_PICTURE_EDIT_BUTTON),
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
            Icon(
                imageVector = if (removeMode) Icons.Default.Clear else Icons.Default.CameraAlt,
                contentDescription = if (removeMode) "Remove photo" else "Add photo",
                modifier = Modifier.size(20.dp))
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
