package com.android.agrihealth.ui.office

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.test.platform.app.InstrumentationRegistry
import com.android.agrihealth.data.model.images.ImageViewModel
import com.android.agrihealth.testhelpers.TestTimeout.LONG_TIMEOUT
import com.android.agrihealth.testhelpers.TestUser.office1
import com.android.agrihealth.testhelpers.fakes.FakeImageRepository
import com.android.agrihealth.testhelpers.fakes.FakeOfficeRepository
import com.android.agrihealth.testhelpers.templates.UITest
import com.android.agrihealth.ui.common.layout.NavigationTestTags
import com.android.agrihealth.ui.profile.PhotoComponentsTestTags
import com.android.agrihealth.utils.TestAssetUtils.getUriFrom
import org.junit.Test

private const val PLACEHOLDER_OFFICE_PHOTO = "report_image_cat.jpg"
private const val FAKE_PHOTO_PATH = "some/fake/path/to/photo"

class FakeViewOfficeViewModel(initial: ViewOfficeUiState) :
    ViewOfficeViewModel(targetOfficeId = "fake", officeRepository = FakeOfficeRepository()) {

  private val _state: MutableState<ViewOfficeUiState> = mutableStateOf(initial)

  override val uiState: MutableState<ViewOfficeUiState>
    get() = _state

  override fun load() {}
}

class ViewOfficeScreenTest : UITest() {
  private fun setScreen(
      fakeOfficeVm: FakeViewOfficeViewModel,
      imageViewModel: ImageViewModel = ImageViewModel(FakeImageRepository())
  ) {
    setContent {
      ViewOfficeScreen(viewModel = fakeOfficeVm, onBack = {}, imageViewModel = imageViewModel)
    }
  }

  private fun setBasicTestScreen(withPhoto: Boolean = true): TestDependencies {
    val actualOffice = office1.copy(photoUrl = if (withPhoto) FAKE_PHOTO_PATH else null)
    val fakeOfficeVm = FakeViewOfficeViewModel(ViewOfficeUiState.Success(actualOffice))

    val fakeImageRepository = FakeImageRepository()
    val imageViewModel = ImageViewModel(fakeImageRepository)

    setScreen(fakeOfficeVm, imageViewModel)

    return TestDependencies(fakeOfficeVm, imageViewModel, fakeImageRepository)
  }

  private fun getImageBytesFromUri(uri: Uri): ByteArray {
    val context = InstrumentationRegistry.getInstrumentation().context
    return context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
  }

  private fun addPlaceholderPhoto(imageRepo: FakeImageRepository) {
    val uri = getUriFrom(PLACEHOLDER_OFFICE_PHOTO)
    val bytes = getImageBytesFromUri(uri)
    imageRepo.forceUploadImage(bytes)
  }

  private data class TestDependencies(
      val viewModel: FakeViewOfficeViewModel,
      val imageViewModel: ImageViewModel,
      val imageRepository: FakeImageRepository
  )

  @Test
  override fun displayAllComponents() {
    val dependencies = setBasicTestScreen(withPhoto = true)
    dependencies.imageRepository.freezeRepoConnection()

    with(ViewOfficeScreenTestTags) {
      nodesAreDisplayed(
          OFFICE_INFO_COLUMN,
          ADDRESS_FIELD,
          VET_LIST,
          NavigationTestTags.TOP_BAR_TITLE,
          NavigationTestTags.GO_BACK_BUTTON,
          PhotoComponentsTestTags.PHOTO_LOADING_ANIMATION)
      textContains(NAME_FIELD, office1.name)
      textContains(DESCRIPTION_FIELD, office1.description)
    }
  }

  @Test
  fun loadingState_showsProgressIndicator() {
    val vm = FakeViewOfficeViewModel(ViewOfficeUiState.Loading)
    setScreen(vm)
    nodeIsDisplayed(ViewOfficeScreenTestTags.LOADING_INDICATOR)
  }

  @Test
  fun errorState_displaysErrorText() {
    val vm = FakeViewOfficeViewModel(ViewOfficeUiState.Error("Test error"))
    setScreen(vm)
    nodeIsDisplayed(ViewOfficeScreenTestTags.ERROR_TEXT)
  }

  @Test
  fun officePhoto_showsDefaultIcon_whenDownloadFails() {
    val dependencies = setBasicTestScreen()
    dependencies.imageRepository.makeRepoThrowError()
    dependencies.imageRepository.unfreezeRepoConnection()

    with(PhotoComponentsTestTags) {
      node(PHOTO_RENDER).assertDoesNotExist()
      node(PHOTO_ERROR_TEXT).assertDoesNotExist()
      nodeIsDisplayed(DEFAULT_ICON)
    }
  }

  @Test
  fun officePhoto_showsDefaultIcon_whenNoPhotoUrl() {
    setBasicTestScreen(withPhoto = false)

    with(PhotoComponentsTestTags) {
      nodesNotDisplayed(PHOTO_RENDER, PHOTO_LOADING_ANIMATION, PHOTO_ERROR_TEXT)
      nodeIsDisplayed(DEFAULT_ICON)
    }
  }

  @Test
  fun officePhoto_transitionsFromLoadingToSuccess() {
    val dependencies = setBasicTestScreen()
    dependencies.imageRepository.freezeRepoConnection()
    addPlaceholderPhoto(dependencies.imageRepository)

    with(PhotoComponentsTestTags) {
      nodeIsDisplayed(PHOTO_LOADING_ANIMATION, LONG_TIMEOUT)

      dependencies.imageRepository.unfreezeRepoConnection()

      nodeIsDisplayed(PHOTO_RENDER, LONG_TIMEOUT)
      nodeNotDisplayed(PHOTO_LOADING_ANIMATION)
    }
  }
}
