package com.android.agrihealth.ui.utils

import android.net.Uri
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
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import com.mr0xf00.easycrop.AspectRatio
import com.mr0xf00.easycrop.CircleCropShape
import com.mr0xf00.easycrop.CropError
import com.mr0xf00.easycrop.CropResult
import com.mr0xf00.easycrop.CropState
import com.mr0xf00.easycrop.CropperStyle
import com.mr0xf00.easycrop.ImageCropper
import com.mr0xf00.easycrop.crop
import com.mr0xf00.easycrop.ui.ImageCropperDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


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
 * Display the photo cropper dialog which allows the user to crop the selected photo An
 * [ImageCropper] must be supplied beforehand, which should be created and remembered in the
 * composition with ```rememberImageCropper()```
 *
 * @param imageCropper The supplied [imageCropper]
 */
@Composable
fun ShowImageCropperDialog(imageCropper: ImageCropper) {
  val cropState = imageCropper.cropState!!

  // Setting this once
  LaunchedEffect(cropState) { setImageCropperShapeCircle(cropState) }

  ImageCropperDialog(
      state = cropState,
      topBar = { ImageCropperCustomTopBar(cropState) },
      cropControls = {},
      style =
          CropperStyle(
              autoZoom = true,
              guidelines = null,
              shapes = listOf(CircleCropShape),
              overlay = Color.Black.copy(alpha = .6f),
              aspects = listOf(AspectRatio(1, 1))))
}

// Replacing the default topbar of the image cropper (specifically changes the reset button)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageCropperCustomTopBar(state: CropState) {
  TopAppBar(
      title = {},
      navigationIcon = {
        IconButton(onClick = { state.done(accept = false) }) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel cropping")
        }
      },
      actions = {
        IconButton(onClick = { setImageCropperShapeCircle(state) }) {
          Icon(Icons.Default.Replay, contentDescription = "Restore")
        }
        IconButton(onClick = { state.done(accept = true) }, enabled = !state.accepted) {
          Icon(Icons.Default.Done, contentDescription = "Submit")
        }
      },
      colors = topAppBarColors(containerColor = MaterialTheme.colorScheme.surface))
}

// Force the shape of the image cropper to be a circle
private fun setImageCropperShapeCircle(state: CropState) {
  state.reset()

  // Force the region to be square
  val currentRegion = state.region
  val size = minOf(currentRegion.width, currentRegion.height)
  state.region = Rect(currentRegion.center, size /2f)

  state.aspectLock = true
  state.shape = CircleCropShape
}
