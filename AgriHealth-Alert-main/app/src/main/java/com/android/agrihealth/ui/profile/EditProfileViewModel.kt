package com.android.agrihealth.ui.profile

import androidx.lifecycle.ViewModel
import com.android.agrihealth.data.model.images.ImageViewModel
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.office.OfficeRepository
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.ui.utils.PhotoUi

// TODO Refactor this screen to have an uiState variable and move code from screen to this viewModel
class EditProfileViewModel(
    private val officeRepository: OfficeRepository,
    private val imageViewModel: ImageViewModel
) : ViewModel() {

  /** Decides which photo to display depending on the
   * state of the UI (i.e if a photo has been removed, picked, ...) */
  fun choosePhotoToDisplay(
    remotePhotoURL: String?,
    localPhotoBytes: ByteArray?,
    removeRemotePhoto: Boolean
  ): PhotoUi = when {
    localPhotoBytes != null -> PhotoUi.Local(localPhotoBytes)
    removeRemotePhoto -> PhotoUi.Empty
    remotePhotoURL != null -> PhotoUi.Remote(remotePhotoURL)
    else -> PhotoUi.Empty
  }

  suspend fun saveProfileChanges(
      user: User,
      firstname: String,
      lastname: String,
      pickedLocation: Location?,
      selectedDefaultOffice: String?,
      description: String,
      collected: Boolean,
      photoByteArray: ByteArray?,
      removeRemotePhoto: Boolean,
  ): User? {
    val updatedDescription = description.ifBlank { null }

    if (user is Vet && user.officeId != null) {
      val officeResult = officeRepository.getOffice(user.officeId)
      val office = officeResult.getOrNull()

      if (office != null && pickedLocation != null) {
        val updatedOffice = office.copy(address = pickedLocation)
        officeRepository.updateOffice(updatedOffice)
      }
    }

    val finalPhotoUrl: String? =
        when {
          photoByteArray != null -> { // user picked a new photo
            try {
              imageViewModel.uploadAndWait(photoByteArray)
            } catch (e: Throwable) {
              // TODO: Handle error
              null
            }
          }
          removeRemotePhoto -> null // User wants to remove current remote photo
          else -> user.photoURL // User did nothing
        }

    return when (user) {
      is Farmer ->
          user.copy(
              firstname = firstname,
              lastname = lastname,
              address = pickedLocation,
              defaultOffice = selectedDefaultOffice,
              description = updatedDescription,
              collected = collected,
              photoURL = finalPhotoUrl)
      is Vet ->
          user.copy(
              firstname = firstname,
              lastname = lastname,
              address = pickedLocation,
              description = updatedDescription,
              collected = collected,
              photoURL = finalPhotoUrl)
    }
  }
}
