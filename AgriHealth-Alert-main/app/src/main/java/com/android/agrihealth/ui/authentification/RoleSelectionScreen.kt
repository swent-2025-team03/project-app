package com.android.agrihealth.ui.authentification

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.data.model.user.UserRole
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

object RoleSelectionScreenTestTags {
  const val WELCOME = "Welcome"
  const val FARMER = "FarmerButton"
  const val VET = "VetButton"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionScreen(
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    vm: RoleSelectionViewModel = viewModel(),
    onBack: () -> Unit = {},
    onButtonPressed: () -> Unit = {},
) {
  val userGreeting = remember {
    if (Firebase.auth.currentUser!!.displayName != null) {
      "Welcome, ${Firebase.auth.currentUser!!.displayName}!"
    } else "Welcome!"
  }
  Scaffold(
      modifier = Modifier.fillMaxSize(),
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
                  modifier = Modifier.testTag(SignUpScreenTestTags.BACK_BUTTON)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                  }
            })
      }) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Spacer(Modifier.height(250.dp))

              Text(
                  text = userGreeting,
                  fontSize = 40.sp,
                  fontWeight = FontWeight.Medium,
                  color = Color.Black,
                  modifier = Modifier.testTag(RoleSelectionScreenTestTags.WELCOME))
              Spacer(Modifier.height(10.dp))
              Text(
                  text = "Please choose a role.",
                  fontSize = 25.sp,
                  color = Color.Black,
                  modifier = Modifier.testTag(SignInScreenTestTags.LOGIN_TITLE))
              Spacer(Modifier.height(50.dp))
              Button(
                  onClick = {
                    vm.createUser(UserRole.FARMER)
                    onButtonPressed.invoke()
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDDF4E7)),
                  modifier = Modifier.testTag(RoleSelectionScreenTestTags.FARMER)) {
                    Text("I'm a Farmer!", color = Color.Black)
                  }
              Spacer(Modifier.height(40.dp))
              Button(
                  onClick = {
                    vm.createUser(UserRole.VET)
                    onButtonPressed.invoke()
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDDF4E7)),
                  modifier = Modifier.testTag(RoleSelectionScreenTestTags.VET)) {
                    Text("I'm a Veterinarian!", color = Color.Black)
                  }
            }
      }
}

@Preview
@Composable
fun ScreenPreview() {
  RoleSelectionScreen()
}
