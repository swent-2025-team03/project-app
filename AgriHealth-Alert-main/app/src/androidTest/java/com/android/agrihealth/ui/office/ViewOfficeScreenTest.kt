package com.android.agrihealth.ui.office

import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.android.agrihealth.data.model.images.ImageViewModel
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.testutil.FakeImageRepository
import com.android.agrihealth.testutil.FakeOfficeRepository
import com.android.agrihealth.testutil.TestConstants
import com.android.agrihealth.testutil.TestConstants.LONG_TIMEOUT
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.profile.PhotoComponentsTestTags
import com.android.agrihealth.utils.TestAssetUtils.getUriFrom
import org.junit.Rule
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

class ViewOfficeScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun setScreen(
      fakeOfficeVm: FakeViewOfficeViewModel,
      imageViewModel: ImageViewModel = ImageViewModel(FakeImageRepository())
  ) {
    composeTestRule.setContent {
      MaterialTheme {
        ViewOfficeScreen(viewModel = fakeOfficeVm, onBack = {}, imageViewModel = imageViewModel)
      }
    }
  }

  private fun setBasicTestScreen(withPhoto: Boolean = true): TestDependencies {
    val office =
        Office(
            id = "o1",
            name = "Agri Vet Clinic",
            address = Location(0.0, 0.0, "123 Farm Road, Countryside"),
            description = "Providing quality veterinary services for farm animals.",
            vets = listOf("vet1", "vet2"),
            ownerId = "owner1",
            photoUrl = if (withPhoto) FAKE_PHOTO_PATH else null)

    val fakeOfficeVm = FakeViewOfficeViewModel(ViewOfficeUiState.Success(office))

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
  fun topBar_displaysCorrectly() {
    val vm = FakeViewOfficeViewModel(ViewOfficeUiState.Loading)
    setScreen(
        vm,
    )

    composeTestRule.onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE).assertExists()
    composeTestRule.onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON).assertExists()
  }

  @Test
  fun loadingState_showsProgressIndicator() {
    val vm = FakeViewOfficeViewModel(ViewOfficeUiState.Loading)
    setScreen(vm)

    composeTestRule
        .onNodeWithTag(ViewOfficeScreenTestTags.LOADING_INDICATOR)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun errorState_displaysErrorText() {
    val vm = FakeViewOfficeViewModel(ViewOfficeUiState.Error("Test error"))
    setScreen(vm)

    composeTestRule
        .onNodeWithTag(ViewOfficeScreenTestTags.ERROR_TEXT)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun successState_displaysContentColumn() {
    setBasicTestScreen()

    composeTestRule
        .onNodeWithTag(ViewOfficeScreenTestTags.OFFICE_INFO_COLUMN)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun nameField_displaysCorrectValue() {
    setBasicTestScreen()

    composeTestRule
        .onNodeWithTag(ViewOfficeScreenTestTags.NAME_FIELD)
        .assertTextContains("Agri Vet Clinic")
  }

  @Test
  fun descriptionField_showsWhenDescriptionPresent() {
    setBasicTestScreen()

    composeTestRule
        .onNodeWithTag(ViewOfficeScreenTestTags.DESCRIPTION_FIELD)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun addressField_showsWhenAddressExists() {
    setBasicTestScreen()
    composeTestRule
        .onNodeWithTag(ViewOfficeScreenTestTags.ADDRESS_FIELD)
        .assertExists()
        .assertIsDisplayed()
        .assertTextContains("123 Farm Road, Countryside")
  }

  @Test
  fun vetList_showsWhenVetsPresent() {
    setBasicTestScreen()

    composeTestRule
        .onNodeWithTag(ViewOfficeScreenTestTags.VET_LIST)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun officePhoto_showsLoadingIndicator_whenImageIsLoading() {
    val dependencies = setBasicTestScreen()
    dependencies.imageRepository.freezeRepoConnection()

    composeTestRule
        .onNodeWithTag(PhotoComponentsTestTags.PHOTO_LOADING_ANIMATION)
        .assertIsDisplayed()
  }

  @Test
  fun officePhoto_showsImage_whenDownloadSucceeds() {
    val dependencies = setBasicTestScreen()
    dependencies.imageRepository.freezeRepoConnection()

    addPlaceholderPhoto(dependencies.imageRepository)
    dependencies.imageRepository.unfreezeRepoConnection()

    composeTestRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeTestRule.onNodeWithTag(PhotoComponentsTestTags.PHOTO_RENDER).isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(PhotoComponentsTestTags.PHOTO_LOADING_ANIMATION)
        .assertIsNotDisplayed()
  }

  @Test
  fun officePhoto_showsDefaultIcon_whenDownloadFails() {
    val dependencies = setBasicTestScreen()
    dependencies.imageRepository.makeRepoThrowError()
    dependencies.imageRepository.unfreezeRepoConnection()

    composeTestRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeTestRule
          .onAllNodesWithTag(PhotoComponentsTestTags.PHOTO_RENDER)
          .fetchSemanticsNodes()
          .isEmpty()
    }

    composeTestRule.onNodeWithTag(PhotoComponentsTestTags.PHOTO_RENDER).assertDoesNotExist()

    composeTestRule.onNodeWithTag(PhotoComponentsTestTags.PHOTO_ERROR_TEXT).assertDoesNotExist()

    composeTestRule.onNodeWithTag(PhotoComponentsTestTags.DEFAULT_ICON).isDisplayed()
  }

  @Test
  fun officePhoto_showsDefaultIcon_whenNoPhotoUrl() {
    setBasicTestScreen(withPhoto = false)

    composeTestRule.onNodeWithTag(PhotoComponentsTestTags.PHOTO_RENDER).assertIsNotDisplayed()

    composeTestRule
        .onNodeWithTag(PhotoComponentsTestTags.PHOTO_LOADING_ANIMATION)
        .assertIsNotDisplayed()

    composeTestRule.onNodeWithTag(PhotoComponentsTestTags.PHOTO_ERROR_TEXT).assertIsNotDisplayed()

    composeTestRule.onNodeWithTag(PhotoComponentsTestTags.DEFAULT_ICON).isDisplayed()
  }

  @Test
  fun officePhoto_transitionsFromLoadingToSuccess() {
    val dependencies = setBasicTestScreen()
    dependencies.imageRepository.freezeRepoConnection()
    addPlaceholderPhoto(dependencies.imageRepository)

    composeTestRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeTestRule.onNodeWithTag(PhotoComponentsTestTags.PHOTO_LOADING_ANIMATION).isDisplayed()
    }

    dependencies.imageRepository.unfreezeRepoConnection()

    composeTestRule.waitUntil(TestConstants.LONG_TIMEOUT) {
      composeTestRule.onNodeWithTag(PhotoComponentsTestTags.PHOTO_RENDER).isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(PhotoComponentsTestTags.PHOTO_LOADING_ANIMATION)
        .assertIsNotDisplayed()
  }
}
