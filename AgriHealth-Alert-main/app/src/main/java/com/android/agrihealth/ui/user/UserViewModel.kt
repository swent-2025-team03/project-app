package com.android.agrihealth.ui.user

import androidx.lifecycle.ViewModel
import com.android.agrihealth.data.model.UserRole
import com.android.agrihealth.data.model.authentification.User

// TODO: connect to Firebase Auth
class UserViewModel : ViewModel() {
  var userRole: UserRole = UserRole.FARMER
  var userId: String = "FARMER_001"

  var user: User? = null

}
