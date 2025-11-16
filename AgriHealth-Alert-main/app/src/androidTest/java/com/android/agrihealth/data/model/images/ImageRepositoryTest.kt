package com.android.agrihealth.data.model.images

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.layout.size
import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsTest
import java.io.ByteArrayOutputStream
import kotlin.random.Random
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.math.sqrt

class ImageRepositoryTest : FirebaseEmulatorsTest() {
  val repository = ImageRepositoryProvider.repository

  // For image viewing debugging
  // @get:Rule val composeTestRule = createComposeRule()

  // val pattern = (x < width/2 && y < height/2) || (x >= width/2 && y >= height/2)
  val testImageBM = generateTestImage()
  val testImageBA = bitmapToByteArray(testImageBM)

  private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val byteStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream)
    return byteStream.toByteArray()
  }

  private fun randomColor(): Int {
    return Random.nextInt() or 0xFF000000.toInt()
  }

  private fun generateTestImage(
      width: Int = 50,
      height: Int = 50,
      pattern: (Int, Int) -> Boolean = { x, y -> (x + y) % 2 == 0 },
      randomColor: Boolean = false
  ): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

    for (x in 0 until width) {
      for (y in 0 until height) {
        val color =
            if (randomColor) randomColor() else if (pattern(x, y)) Color.MAGENTA else Color.BLACK
        bitmap.setPixel(x, y, color)
      }
    }

    return bitmap
  }

  fun generateBigImage(): Bitmap {
    // Probably big enough
    val width = (sqrt(repository.MAX_FILE_SIZE.toFloat() / 4) * 1.35).toInt()
    // PNG compression is good at reducing size for patterns
    return generateTestImage(width, width, randomColor = true)
  }

  @Test
  fun uploadImage_canSucceed() = runTest {
    val result = repository.uploadImage(testImageBA)

    if (result.isFailure) Log.e("ImagesTest", "Error: ${result.exceptionOrNull()?.message}")
    assert(result.isSuccess)
    Log.w("ImagesTest", "Uploaded image: ${result.getOrNull()}")
  }

  @Test
  fun downloadImage_getsIdenticalAsUpload() = runTest {
    val uploadResult = repository.uploadImage(testImageBA)
    assert(uploadResult.isSuccess)
    val imageUrl = uploadResult.getOrNull()

    val downloadResult = repository.downloadImage(imageUrl!!)
    assert(downloadResult.isSuccess)
    val downloadedByteArray = downloadResult.getOrNull()!!

    // Debug to check if the images are indeed different
    // Be careful: image compression and encoding can cause identical byte arrays to look
    // like different bitmaps!

    /*
    val downloadedBitmap = downloadResult.toBitmap().getOrNull()!!
    composeTestRule.setContent {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Reference")
            Image(bitmap = testImageBM.asImageBitmap(), contentDescription = null, modifier = Modifier.size(100.dp))

            HorizontalDivider()

            Text(text = "Result")
            Image(bitmap = downloadedBitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.size(100.dp))
        }
    }

    composeTestRule.waitUntil(10000) {
        false
    }
    */

    assert(testImageBA.size == downloadedByteArray.size)
    assert(testImageBA.contentEquals(downloadedByteArray))
  }

  @Test
  fun isFileTooLarge_worksAsExpected() {
    val smallImage = bitmapToByteArray(generateTestImage(1, 1, randomColor = true))

    val bigImage = bitmapToByteArray(generateBigImage())

    assert(!repository.isFileTooLarge(smallImage))
    assert(repository.isFileTooLarge(bigImage))
  }

  @Test
  fun resizeImage_reduceByHalf() {
    val factor: Int = 2
    val bigWidth = repository.IMAGE_MAX_WIDTH * factor
    val bigHeight = repository.IMAGE_MAX_HEIGHT * factor
    val largeImage = generateTestImage(bigWidth, bigHeight)

    val resizedImage = repository.resizeImage(largeImage)
    val newWidth = resizedImage.width
    val newHeight = resizedImage.height

    assert(bigWidth == newWidth * factor)
    assert(bigHeight == newHeight * factor)
  }

  @Test
  fun resizeImage_untouchedIfSmall() {
    val smallImage = generateTestImage(10, 10)

    val resizedImage = repository.resizeImage(smallImage)

    assert(smallImage.width == resizedImage.width)
    assert(smallImage.height == resizedImage.height)
  }

  @Test
  fun compressImage_loopsUntilSmall() {
    val bigImage = generateBigImage()

    assert(repository.isFileTooLarge(bitmapToByteArray(bigImage)))

    val compressedImage = repository.compressImage(bigImage)

    assert(!repository.isFileTooLarge(compressedImage))
  }
}
