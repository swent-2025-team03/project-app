package com.android.agrihealth.ui.profile

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.android.agrihealth.data.model.images.ImageUIState
import com.android.agrihealth.data.model.images.ImageViewModel
import com.android.agrihealth.ui.utils.ImagePickerDialog

object PhotoComponentsTestTags {
  const val PHOTO_RENDER = "PhotoRender"
  const val PHOTO_LOADING_ANIMATION = "PhotoLoadingAnimation"
  const val PHOTO_ERROR_TEXT = "PhotoErrorText"
  const val PHOTO_ILLEGAL_TEXT = "PhotoIllegalStateText"
  const val UPLOAD_IMAGE_BUTTON = "uploadImageButton"
  const val IMAGE_PREVIEW = "imageDisplay"
  const val DEFAULT_ICON = "DefaultIconPicture"
}

object PhotoComponentsTexts {
  const val PHOTO_ERROR_TEXT = "Failed to load image"
  const val PHOTO_ILLEGAL_TEXT = "An unexpected error happened."

  /** Texts on the button used to upload/remove a photo */
  const val UPLOAD_IMAGE = "Upload Image"
  const val REMOVE_IMAGE = "Remove Image"
}

/**
 * Handles the display of a remote photo URL (already uploaded).
 *
 * @param photoURL URL of the photo that is already uploaded and stored remotely.
 * @param imageViewModel ViewModel responsible for downloading and exposing the image state.
 * @param modifier Modifier used to customize the layout (e.g. profile picture vs report image).
 * @param contentDescription Content description for accessibility.
 * @param showPlaceHolder If true and the photoURI is null, a default account icon is displayed
 *   instead.
 * @param placeholder Composable shown when `photoURL` is null and `showPlaceHolder` is true.
 *   Defaults to a standard account icon.
 */
@Composable
fun RemotePhotoDisplay(
    photoURL: String?,
    imageViewModel: ImageViewModel,
    modifier: Modifier = Modifier,
    contentDescription: String?,
    showPlaceHolder: Boolean = false,
    placeholder: @Composable (Modifier) -> Unit = { DefaultIconPlaceholder(it) }
) {
  val imageUiState by imageViewModel.uiState.collectAsState()

  LaunchedEffect(photoURL) {
    if (photoURL != null) {
      imageViewModel.download(photoURL)
    }
  }

  when (val currentState = imageUiState) {
    is ImageUIState.DownloadSuccess -> {
      AsyncImage(
          model = currentState.imageData,
          contentDescription = contentDescription,
          modifier = modifier.testTag(PhotoComponentsTestTags.PHOTO_RENDER),
          contentScale = ContentScale.Fit,
      )
    }
    is ImageUIState.Loading -> {
      Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.testTag(PhotoComponentsTestTags.PHOTO_LOADING_ANIMATION))
      }
    }
    is ImageUIState.Error -> {
      if (showPlaceHolder) {
        placeholder(modifier)
      } else {
        Text(
            text = PhotoComponentsTexts.PHOTO_ERROR_TEXT,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier.testTag(PhotoComponentsTestTags.PHOTO_ERROR_TEXT))
      }
    }
    else -> {
      if (showPlaceHolder) {
        placeholder(modifier)
      } else {
        Text(
            text = PhotoComponentsTexts.PHOTO_ILLEGAL_TEXT,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier.testTag(PhotoComponentsTestTags.PHOTO_ILLEGAL_TEXT))
      }
    }
  }
}

/**
 * Displays the photo picked by the user before it is uploaded
 * * (local image referenced by a URI).
 *
 * @param photoURI URI of the local photo that has not yet been uploaded.
 * @param modifier Modifier used to customize the layout (e.g. profile picture vs report image).
 * @param showPlaceHolder If true and the photoURI is null, a default account icon is displayed
 *   instead.
 * @param placeholder Composable shown when `photoURI` is null and `showPlaceHolder` is true.
 *   Defaults to a standard account icon.
 */
@Composable
fun LocalPhotoDisplay(
    photoURI: Uri?,
    modifier: Modifier = Modifier,
    showPlaceHolder: Boolean = false,
    placeholder: @Composable (Modifier) -> Unit = { DefaultIconPlaceholder(it) }
) {
  if (photoURI != null) {
    AsyncImage(
        model = photoURI,
        contentDescription = "Uploaded image",
        modifier = modifier.testTag(PhotoComponentsTestTags.IMAGE_PREVIEW),
        contentScale = ContentScale.Fit)
  } else if (showPlaceHolder) {
    placeholder(modifier)
  }
}

@Composable
fun DefaultIconPlaceholder(modifier: Modifier = Modifier) {
  Icon(
      imageVector = Icons.Default.AccountCircle,
      contentDescription = "Default icon",
      modifier = modifier.testTag(PhotoComponentsTestTags.DEFAULT_ICON))
}

/**
 * Handles adding or removing a photo from the report
 *
 * @param photoAlreadyPicked True if a photo has already been selected, false otherwise.
 * @param onPhotoPicked Called when a photo has been picked by the user.
 * @param onPhotoRemoved Called when the currently selected photo is removed.
 */
@Composable
fun UploadRemovePhotoButton(
    photoAlreadyPicked: Boolean,
    onPhotoPicked: (Uri?) -> Unit,
    onPhotoRemoved: () -> Unit
) {
  var showImagePicker by remember { mutableStateOf(false) }
  Button(
      onClick = { if (photoAlreadyPicked) onPhotoRemoved() else showImagePicker = true },
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 16.dp)
              .testTag(PhotoComponentsTestTags.UPLOAD_IMAGE_BUTTON),
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
  ) {
    Text(
        text =
            if (photoAlreadyPicked) PhotoComponentsTexts.REMOVE_IMAGE
            else PhotoComponentsTexts.UPLOAD_IMAGE)
  }

  if (showImagePicker) {
    ImagePickerDialog(
        onDismiss = { showImagePicker = false }, onImageSelected = { uri -> onPhotoPicked(uri) })
  }
}
