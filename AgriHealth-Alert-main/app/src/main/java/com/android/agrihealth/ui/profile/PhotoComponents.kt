package com.android.agrihealth.ui.profile

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
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
}

object PhotoComponentsTexts {
  const val PHOTO_ERROR_TEXT = "Failed to load image"
  const val PHOTO_ILLEGAL_TEXT = "An unexpected error happened."

  /** Texts on the button used to upload/remove a photo */
  const val UPLOAD_IMAGE = "Upload Image"
  const val REMOVE_IMAGE = "Remove Image"
}

/**
 * Handles the display of the remote photo Url, not local uri
 *
 * @param photoURL Url of the photo, already uploaded and in remote
 * @param imageViewModel
 * @param modifier Can change between normal pictures on reports or profile pictures
 * @param contentDescription
 * @param showPlaceHolder if photoURL is empty or null, whether it'll display the default account
 *   icon or not
 */
@Composable
fun RemotePhotoDisplay(
    photoURL: String?,
    imageViewModel: ImageViewModel,
    modifier: Modifier = Modifier,
    contentDescription: String?,
    showPlaceHolder: Boolean = false
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
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Default icon",
            modifier = modifier)
      } else {
        Text(
            text = PhotoComponentsTexts.PHOTO_ERROR_TEXT,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier.testTag(PhotoComponentsTestTags.PHOTO_ERROR_TEXT))
      }
    }
    else -> {
      if (showPlaceHolder) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Default icon",
            modifier = modifier)
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
 * Displays the photo that was picked by the user before being uploaded and possible compressed by
 * the image repository
 *
 * @param photoURI URI of the photo, not yet uploaded so it is local
 * @param modifier Can change between normal pictures on reports or profile pictures
 * @param showPlaceHolder Whether it'll display the default account icon or not, when photoURI is
 *   empty or null
 */
@Composable
fun LocalPhotoDisplay(
    photoURI: Uri?,
    modifier: Modifier = Modifier,
    showPlaceHolder: Boolean = false
) {
  if (photoURI != null) {
    AsyncImage(
        model = photoURI,
        contentDescription = "Uploaded image",
        modifier = modifier.testTag(PhotoComponentsTestTags.IMAGE_PREVIEW),
        contentScale = ContentScale.Fit)
  } else if (showPlaceHolder) {
    Icon(
        imageVector = Icons.Default.AccountCircle,
        contentDescription = "Default icon",
        modifier = Modifier.size(120.dp).clip(CircleShape))
  }
}

/**
 * Handles adding or removing a photo from the report
 *
 * @param photoAlreadyPicked true if a photo has already ben added to the report, false otherwise
 * @param onPhotoPicked Called when a photo has been picked for the report
 * @param onPhotoRemoved Called when the selected photo has been removed from the report
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
