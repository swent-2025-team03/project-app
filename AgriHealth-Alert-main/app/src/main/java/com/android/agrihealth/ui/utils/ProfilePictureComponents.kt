package com.android.agrihealth.ui.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.agrihealth.data.model.images.ImageViewModel
import com.android.agrihealth.ui.profile.LocalPhotoDisplay
import com.android.agrihealth.ui.profile.RemotePhotoDisplay

// TODO Write kdoc
sealed interface PhotoUi {
  data object Empty : PhotoUi
  data class Remote(val url: String) : PhotoUi
  data class Local(val bytes: ByteArray) : PhotoUi
}

@Composable
fun EditableProfilePicture(
  photo: PhotoUi,
  isEditable: Boolean,
  imageViewModel: ImageViewModel,
  modifier: Modifier = Modifier,
  imageSize: Dp = 120.dp,
  profilePictureTestTag: String,
  editButtonTestTag: String,
  onAddClicked: () -> Unit,
  onRemoveClicked: () -> Unit,
) {
  Box(
    modifier = modifier.size(imageSize),
    contentAlignment = Alignment.Center,
  ) {
    when (photo) {
      is PhotoUi.Remote -> RemotePhotoDisplay(
        photoURL = photo.url,
        imageViewModel = imageViewModel,
        modifier = Modifier.size(imageSize).clip(CircleShape).testTag(profilePictureTestTag),
        contentDescription = "Photo",
        showPlaceHolder = true
      )
      is PhotoUi.Local -> LocalPhotoDisplay(
        photoByteArray = photo.bytes,
        modifier = Modifier.size(imageSize).clip(CircleShape).testTag(profilePictureTestTag),
        showPlaceHolder = true
      )
      PhotoUi.Empty -> LocalPhotoDisplay(
        photoByteArray = null,
        modifier = Modifier.size(imageSize).clip(CircleShape).testTag(profilePictureTestTag),
        showPlaceHolder = true
      )
    }

    if (isEditable) {
      val removeMode = photo != PhotoUi.Empty
      FloatingActionButton(
        onClick = { if (removeMode) onRemoveClicked() else onAddClicked() },
        modifier = Modifier
          .size(40.dp)
          .align(Alignment.BottomEnd)
          .testTag(editButtonTestTag),
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
