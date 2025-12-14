package com.android.agrihealth.ui.authentification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.data.model.user.UserViewModel
import com.android.agrihealth.ui.common.layout.NavigationTestTags
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

object RoleSelectionScreenTestTags {
  const val WELCOME = "Welcome"
  const val FARMER = "FarmerButton"
  const val VET = "VetButton"
}

/**
 * Minimalist composable function to display a screen to new google users, this should only display
 * when the user logs in with a new google account for the first time. The point of this screen is
 * to let the user choose a role as the other user information will be provided through the profile
 * screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionScreen(
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    vm: RoleSelectionViewModel = viewModel(),
    onBack: () -> Unit = {},
    onButtonPressed: () -> Unit = {},
    userViewModel: UserViewModel = viewModel()
) {
  val userGreeting = remember {
    if (Firebase.auth.currentUser!!.displayName != null) {
      "Welcome, ${Firebase.auth.currentUser!!.displayName}!"
    } else "Welcome!"
  }

  Scaffold(
      topBar = {
        // Top bar with back arrow and title/status
        TopAppBar(
            title = {},
            navigationIcon = {
              IconButton(
                  onClick = {
                    vm.signOut(credentialManager)
                    onBack.invoke()
                  },
                  modifier = Modifier.testTag(NavigationTestTags.GO_BACK_BUTTON)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                  }
            })
      }) { padding ->
        Column(
            modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)) {
              Text(
                  text = userGreeting,
                  style = MaterialTheme.typography.displaySmall,
                  overflow = TextOverflow.Visible,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.testTag(RoleSelectionScreenTestTags.WELCOME).fillMaxWidth())
              Text(text = "Please choose a role.", style = MaterialTheme.typography.headlineMedium)
              Button(
                  onClick = {
                    val firebaseUser = Firebase.auth.currentUser
                    if (firebaseUser != null) {
                      val newUser =
                          com.android.agrihealth.data.model.user.Farmer(
                              uid = firebaseUser.uid,
                              firstname = firebaseUser.displayName ?: "",
                              lastname = "",
                              email = firebaseUser.email ?: "",
                              address = null,
                              linkedOffices = emptyList(),
                              defaultOffice = null)
                      userViewModel.setUser(newUser) // synchronous in-memory set
                    }

                    vm.createUser(UserRole.FARMER)
                    onButtonPressed.invoke()
                  },
                  modifier = Modifier.testTag(RoleSelectionScreenTestTags.FARMER)) {
                    Text("I'm a Farmer!")
                  }
              Button(
                  onClick = {
                    val firebaseUser = Firebase.auth.currentUser
                    if (firebaseUser != null) {
                      val newUser =
                          com.android.agrihealth.data.model.user.Vet(
                              uid = firebaseUser.uid,
                              firstname = firebaseUser.displayName ?: "",
                              lastname = "",
                              email = firebaseUser.email ?: "",
                              address = null)
                      userViewModel.setUser(newUser) // synchronous in-memory set
                    }

                    vm.createUser(UserRole.VET)
                    onButtonPressed.invoke()
                  },
                  modifier = Modifier.testTag(RoleSelectionScreenTestTags.VET)) {
                    Text("I'm a Veterinarian!")
                  }
            }
      }
}

@Preview
@Composable
fun RoleSelectionScreenPreview() {
  RoleSelectionScreen()
}
