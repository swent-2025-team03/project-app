package com.android.agrihealth.ui.profile

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.android.agrihealth.data.model.images.ImageUIState
import com.android.agrihealth.data.model.images.ImageViewModel

@Composable
fun RemotePhotoDisplay(
    photoURL: String?,
    imageViewModel: ImageViewModel,
    modifier: Modifier = Modifier,
    contentDescription: String?
) {
  val imageUiState by imageViewModel.uiState.collectAsState()

  LaunchedEffect(photoURL) {
    if (photoURL != null) {
      imageViewModel.download(photoURL)
    }
  }

  when (val state = imageUiState) {
    is ImageUIState.DownloadSuccess -> {
      Log.d("ProfilePhotoDisplays", "DownloadSuccess → showing image")
      AsyncImage(
          model = state.imageData,
          contentDescription = contentDescription,
          modifier = modifier,
          contentScale = ContentScale.Fit,
      )
    }
    is ImageUIState.Loading -> {
      Log.d("ProfilePhotoDisplays", "Loading… showing CircularProgressIndicator")
      Box(modifier = modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    }
    is ImageUIState.Error -> {
      Log.d("ProfilePhotoDisplays", "Error → showing default icon. Error = ${state.e}")
      Icon(
          imageVector = Icons.Default.AccountCircle,
          contentDescription = "Default icon",
          modifier = modifier)
    }
    else -> {
      Log.d("ProfilePhotoDisplays", "Else branch")
      Icon(
          imageVector = Icons.Default.AccountCircle,
          contentDescription = "Default icon",
          modifier = modifier)
    }
  }
}
