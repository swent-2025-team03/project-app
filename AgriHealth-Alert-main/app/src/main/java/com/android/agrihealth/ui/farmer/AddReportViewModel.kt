package com.android.agrihealth.ui.farmer

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


data class AddReportUiState(
    val title: String = "",
    val description: String = "",
    val chosenVet: String = "",   // TODO: Shouldn't be a string! Temporary measure
    val imageBitmap: Bitmap? = null    // TODO: Currently just an image but should hold mutliple images/videos later
)

class AddReportViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AddReportUiState())
    val uiState: StateFlow<AddReportUiState> = _uiState

    fun setTitle(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle)
    }

    fun setDescription(newDescription: String) {
        _uiState.value = _uiState.value.copy(description = newDescription)
    }

    fun setImageBitmap(newImageBitmap: Bitmap?) {
        _uiState.value = _uiState.value.copy(imageBitmap = newImageBitmap)
    }
    fun setVet(option: String) {
        _uiState.value = _uiState.value.copy(chosenVet = option)
    }


    fun createReport() {
        // TODO: complete the action of the create report button
    }
}
