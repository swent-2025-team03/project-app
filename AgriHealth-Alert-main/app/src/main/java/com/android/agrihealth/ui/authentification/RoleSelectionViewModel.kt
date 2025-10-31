package com.android.agrihealth.ui.authentification

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.authentification.AuthRepository
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
import com.android.agrihealth.data.model.authentification.UserRepository
import com.android.agrihealth.data.model.authentification.UserRepositoryProvider
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.data.model.user.Vet
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class RoleSelectionViewModel(
    private var authRepository: AuthRepository = AuthRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : ViewModel() {

  fun createUser(role: UserRole) {
    val fireUser = Firebase.auth.currentUser!!
    val user =
        when (role) {
          UserRole.VET -> Vet(fireUser.uid, fireUser.displayName ?: "", "", "", null)
          UserRole.FARMER ->
              Farmer(fireUser.uid, fireUser.displayName ?: "", "", "", null, defaultVet = null)
        }
    viewModelScope.launch { userRepository.addUser(user) }
  }

  fun signOut(credentialManager: CredentialManager) {
    viewModelScope.launch {
      authRepository.signOut()
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
  }
}
