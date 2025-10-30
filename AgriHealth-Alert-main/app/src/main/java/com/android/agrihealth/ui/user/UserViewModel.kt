package com.android.agrihealth.ui.user

import androidx.lifecycle.ViewModel
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.UserRole

// TODO: connect to Firebase Auth
open class UserViewModel : ViewModel() {
  var userRole: UserRole = UserRole.FARMER
  var userId: String = "FARMER_001"

  var user: User? = null
}
