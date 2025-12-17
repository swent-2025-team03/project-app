package com.android.agrihealth.data.model.images

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.Log
import com.android.agrihealth.testhelpers.templates.FirebaseTest
import com.google.firebase.auth.FirebaseAuth
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import java.io.ByteArrayOutputStream
import kotlin.math.sqrt
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImageRepositoryFirebaseTest : FirebaseTest() {
  val repository = ImageRepositoryFirebase()

  // For image viewing debugging
  // @get:Rule val composeTestRule = createComposeRule()

  // val pattern = (x < width/2 && y < height/2) || (x >= width/2 && y >= height/2)
  val testImageBM = generateTestImage()
  val testImageBA = bitmapToByteArray(testImageBM)

  @Before
  fun setup() {
    Dispatchers.setMain(StandardTestDispatcher())
    runBlocking {
      val auth = FirebaseAuth.getInstance()
      if (auth.currentUser == null) {
        auth.signInAnonymously().await()
      }
    }
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

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

  private fun generateBigImage(): Bitmap {
    // Probably big enough
    val width = (sqrt(repository.MAX_FILE_SIZE.toFloat() / 4) * 1.35).toInt()
    // PNG compression is good at reducing size for patterns
    return generateTestImage(width, width, randomColor = true)
  }

  @Test
  fun uploadImage_canSucceed() = runTest {
    val result = repository.uploadImage(testImageBA)

    if (result.isFailure) Log.e("ImagesTest", "Error: ${result.exceptionOrNull()?.message}")
    assertTrue(result.isSuccess)
    Log.w("ImagesTest", "Uploaded image: ${result.getOrNull()}")
  }

  @Test
  fun downloadImage_getsIdenticalAsUpload() = runTest {
    val uploadResult = repository.uploadImage(testImageBA)
    runBlocking { delay(2000) } // wait before upload finishes
    assertTrue(uploadResult.isSuccess)
    val imageUrl = uploadResult.getOrNull()

    val downloadResult = repository.downloadImage(imageUrl!!)
    assertTrue(downloadResult.isSuccess)
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

    assertEquals(testImageBA.size, downloadedByteArray.size)
    assertTrue(
        testImageBA.contentEquals(downloadedByteArray)) // not assertEquals due to compression
  }

  @Test
  fun isFileTooLarge_worksAsExpected() {
    val smallImage = bitmapToByteArray(generateTestImage(1, 1, randomColor = true))

    val bigImage = bitmapToByteArray(generateBigImage())

    assertTrue(!repository.isFileTooLarge(smallImage))
    assertTrue(repository.isFileTooLarge(bigImage))
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

    assertEquals(bigWidth, newWidth * factor)
    assertEquals(bigHeight, newHeight * factor)
  }

  @Test
  fun resizeImage_untouchedIfSmall() {
    val smallImage = generateTestImage(10, 10)

    val resizedImage = repository.resizeImage(smallImage)

    assertEquals(smallImage.width, resizedImage.width)
    assertEquals(smallImage.height, resizedImage.height)
  }

  @Test
  fun compressImage_loopsUntilSmall() {
    val bigImage = generateBigImage()

    assertTrue(repository.isFileTooLarge(bitmapToByteArray(bigImage)))

    val compressedImage = repository.compressImage(bigImage)

    assertTrue(!repository.isFileTooLarge(compressedImage))
  }

  @Test
  fun reduceFileSize_reducesBigImageUntilSmall() {
    val bigImage = bitmapToByteArray(generateBigImage())

    assertTrue(repository.isFileTooLarge(bigImage))

    val reducedImage = repository.reduceFileSize(bigImage)

    assertTrue(!repository.isFileTooLarge(reducedImage))
  }

  @Test
  fun viewModel_downloadIdenticalAsUpload() = runTest {
    // Cannot mock an interface, so just take the class of a child of the interface
    val fakeRepo = mockkClass(ImageRepositoryProvider.repository::class)
    val uri: Uri = mockk()
    val bytes = byteArrayOf(1, 2, 3, 4)
    val path = "images/test.png"

    every { fakeRepo.resolveUri(uri) } returns bytes
    every { fakeRepo.reduceFileSize(bytes) } returns bytes
    coEvery { fakeRepo.uploadImage(bytes) } returns Result.success(path)
    coEvery { fakeRepo.downloadImage(path) } returns Result.success(bytes)

    val viewModel = ImageViewModel(repository = fakeRepo)

    // Upload and check if success
    viewModel.upload(uri)
    advanceUntilIdle()

    var uiState = viewModel.uiState.value
    assertTrue(uiState is ImageUIState.UploadSuccess)

    // Download and check if valid file
    val resultPath = (uiState as ImageUIState.UploadSuccess).path

    viewModel.download(resultPath)
    advanceUntilIdle()

    uiState = viewModel.uiState.value
    assertTrue(uiState is ImageUIState.DownloadSuccess)

    val resultBytes = (uiState as ImageUIState.DownloadSuccess).imageData
    assertTrue(resultBytes.contentEquals(bytes)) // not assertEquals due to compression
  }

  @Test
  fun viewModel_uploadAndDownloadErrorOnFail() = runTest {
    val fakeRepo = mockkClass(ImageRepositoryProvider.repository::class)
    val uri: Uri = mockk()
    val bytes = byteArrayOf(1, 2, 3, 4)
    val path = "images/test.png"

    val uploadErrorMsg = "Upload failure"
    val downloadErrorMsg = "Download failure"

    every { fakeRepo.resolveUri(uri) } returns bytes
    every { fakeRepo.reduceFileSize(bytes) } returns bytes
    coEvery { fakeRepo.uploadImage(bytes) } returns Result.failure(Exception(uploadErrorMsg))
    coEvery { fakeRepo.downloadImage(path) } returns Result.failure(Exception(downloadErrorMsg))

    val viewModel = ImageViewModel(repository = fakeRepo)

    // Upload and check if failed with correct exception
    viewModel.upload(uri)
    advanceUntilIdle()

    var uiState = viewModel.uiState.value
    assertTrue(uiState is ImageUIState.Error)
    var errorMsg = (uiState as ImageUIState.Error).e.message
    assertTrue(uploadErrorMsg in errorMsg!!)

    // Download and check if failed with correct exception
    viewModel.download(path)
    advanceUntilIdle()

    uiState = viewModel.uiState.value
    assertTrue(uiState is ImageUIState.Error)
    errorMsg = (uiState as ImageUIState.Error).e.message
    assertTrue(downloadErrorMsg in errorMsg!!)
  }
}
