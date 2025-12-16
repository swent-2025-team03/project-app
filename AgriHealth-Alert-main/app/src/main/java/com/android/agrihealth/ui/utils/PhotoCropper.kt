package com.android.agrihealth.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.mr0xf00.easycrop.AspectRatio
import com.mr0xf00.easycrop.CircleCropShape
import com.mr0xf00.easycrop.CropState
import com.mr0xf00.easycrop.CropperStyle
import com.mr0xf00.easycrop.ImageCropper
import com.mr0xf00.easycrop.ui.ImageCropperDialog


// TODO Allow crop region to be square (to allow later to have square profile picture shape for office
/**
 *  Display the photo cropper dialog which allows the user to crop the selected photo
 *  An [ImageCropper] must be supplied beforehand, which should be created and remembered in the composition
 *  with ```rememberImageCropper()```
 *
 *  @param imageCropper The supplied [imageCropper]
 */
@Composable
fun ShowImageCropperDialog(imageCropper: ImageCropper) {
  val cropState = imageCropper.cropState!!

  // Setting this once
  LaunchedEffect(cropState) {
    setImageCropperShapeCircle(cropState)
  }

  ImageCropperDialog(
    state = cropState,
    topBar = { ImageCropperCustomTopBar(cropState) },
    cropControls = {},
    style = CropperStyle(
      autoZoom = true,
      guidelines = null,
      shapes = listOf(CircleCropShape),
      overlay = Color.Black.copy(alpha = .6f),
      aspects = listOf(AspectRatio(1, 1))
    ))
}

// Replacing the default topbar of the image cropper (specifically changes the reset button)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageCropperCustomTopBar(state: CropState) {
  TopAppBar(title = {},
    navigationIcon = {
      IconButton(onClick = { state.done(accept = false) }) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel cropping")
      }
    },
    actions = {
      IconButton(onClick = {
        setImageCropperShapeCircle(state)
      }) {
        Icon(Icons.Default.Replay, contentDescription = "Restore")
      }
      IconButton(onClick = { state.done(accept = true) }, enabled = !state.accepted) {
        Icon(Icons.Default.Done, contentDescription = "Submit")
      }
    },
    colors = topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
  )
}

// Force the shape of the image cropper to be a circle
private fun setImageCropperShapeCircle(state: CropState) {
  state.reset()

  // Force the region to be square
  val currentRegion = state.region
  val size = minOf(currentRegion.width, currentRegion.height)
  val centerX = currentRegion.center.x
  val centerY = currentRegion.center.y

  state.region = Rect(
    left = centerX - size / 2f,
    top = centerY - size / 2f,
    right = centerX + size / 2f,
    bottom = centerY + size / 2f
  )

  state.aspectLock = true
  state.shape = CircleCropShape
}