package com.android.agrihealth.ui.profile

import androidx.lifecycle.ViewModel
import com.android.agrihealth.data.model.images.ImageViewModel
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.office.OfficeRepository
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.Vet

class EditProfileViewModel(
  private val officeRepository: OfficeRepository,
  private val imageViewModel: ImageViewModel
) : ViewModel() {

  suspend fun saveProfileChanges(
      user: User,
      firstname: String,
      lastname: String,
      pickedLocation: Location?,
      selectedDefaultOffice: String?,
      description: String,
      collected: Boolean,
      photoByteArray: ByteArray?
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

    var uploadedPhotoURL: String? = null
    if (photoByteArray != null) {
      try {
        uploadedPhotoURL = imageViewModel.uploadAndWait(photoByteArray)
      } catch (e: Throwable) {
        // TODO Implement error handling
      }
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
              photoURL = uploadedPhotoURL)
      is Vet ->
          user.copy(
              firstname = firstname,
              lastname = lastname,
              address = pickedLocation,
              description = updatedDescription,
              collected = collected,
              photoURL = uploadedPhotoURL)
    }
  }
}
