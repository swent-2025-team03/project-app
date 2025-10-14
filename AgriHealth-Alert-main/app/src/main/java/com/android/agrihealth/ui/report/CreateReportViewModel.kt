package com.android.agrihealth.ui.report

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.authentification.AuthRepository
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
import com.android.agrihealth.data.model.authentification.User
import com.android.agrihealth.data.model.authentification.UserRepository
import com.android.agrihealth.data.model.authentification.UserRepositoryProvider
import com.android.agrihealth.data.model.authentification.UserRole
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class CreateReportUiState(
    val title: String = "",
    val description: String = "",
    val chosenVet: String = "",   // TODO: Shouldn't be a string! Temporary measure
    val imageBitmap: Bitmap? = null    // TODO: Currently just an image but should hold mutliple images/videos later
)

class CreateReportViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CreateReportUiState())
    val uiState: StateFlow<CreateReportUiState> = _uiState

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
