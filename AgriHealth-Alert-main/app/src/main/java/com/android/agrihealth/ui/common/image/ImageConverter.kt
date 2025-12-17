package com.android.agrihealth.ui.common.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.ByteArrayOutputStream

// Created with the help of an LLM
/**
 * Converts this [ImageBitmap] into a [ByteArray]
 *
 * @param format The output format, default to a PNG
 * @param quality The quality of the output, defaults to 100% (i.e no compression)
 */
fun ImageBitmap.toByteArray(
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100
): ByteArray {
  val bitmap = this.asAndroidBitmap()
  val outputStream = ByteArrayOutputStream()
  bitmap.compress(format, quality, outputStream)
  return outputStream.toByteArray()
}

// Created with the help of an LLM
/**
 * Returns a Bitmap representing the file that this [Uri] points to
 *
 * @param context The environment [Context]
 */
fun Uri.toBitmap(context: Context): Bitmap {
  val source = ImageDecoder.createSource(context.contentResolver, this)

  // Forcing to use software allocator (instead of hardware) and forcing bitmap to be mutable to
  // prevent issues when cropping the photo
  return ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
    decoder.isMutableRequired = true
  }
}
