package com.android.agrihealth.ui.farmer

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AddReportViewModelTest {

  private lateinit var viewModel: AddReportViewModel

  @Before
  fun setup() {
    viewModel = AddReportViewModel()
  }

  @Test
  fun testSetTitle() {
    viewModel.setTitle("Test Title")
    assertEquals("Test Title", viewModel.uiState.value.title)
  }

  @Test
  fun testSetDescription() {
    viewModel.setDescription("Description here")
    assertEquals("Description here", viewModel.uiState.value.description)
  }

  @Test
  fun testSetVet() {
    viewModel.setVet("Best Vet Ever!")
    assertEquals("Best Vet Ever!", viewModel.uiState.value.chosenVet)
  }

  /*
  // TODO When image working, add a similar test
  @Test
  fun testSetImageBitmap() {
    val bitmap: Bitmap? = null // Mock or instrumented bitmap if needed
    viewModel.setImageBitmap(bitmap)
    assertEquals(bitmap, viewModel.uiState.value.imageBitmap)
  }
   */
}