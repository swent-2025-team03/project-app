@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.agrihealth.ui.authentification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.testutil.FakeAuthRepository
import com.android.agrihealth.testutil.FakeUserViewModel
import com.android.agrihealth.ui.loading.LoadingOverlay
import com.android.agrihealth.ui.user.UserViewModel
import com.android.agrihealth.ui.user.UserViewModelContract

object SignUpScreenTestTags {
  const val SCREEN = "SignUpScreen"
  const val BACK_BUTTON = "BackButton"
  const val TITLE = "SignUpTitle"
  const val FIRSTNAME_FIELD = "NameField"
  const val LASTNAME_FIELD = "SurnameField"
  const val EMAIL_FIELD = "EmailField"
  const val PASSWORD_FIELD = "PasswordField"
  const val CONFIRM_PASSWORD_FIELD = "ConfirmPasswordField"
  const val SAVE_BUTTON = "SaveButton"
  const val FARMER_PILL = "FarmerPill"
  const val VET_PILL = "VetPill"
  const val SNACKBAR = "Snackbar"
}

@Composable
fun SignUpScreen(
    onBack: () -> Unit = {},
    onSignedUp: () -> Unit = {},
    signUpViewModel: SignUpViewModel = viewModel(),
    userViewModel: UserViewModelContract = viewModel<UserViewModel>()
) {
  val signUpUIState by signUpViewModel.uiState.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }
  val errorMsg = signUpUIState.errorMsg

  LaunchedEffect(errorMsg) {
    if (errorMsg != null) {
      snackbarHostState.showSnackbar(errorMsg)
      signUpViewModel.clearErrorMsg()
    }
  }

  LaunchedEffect(signUpUIState.uid) {
    signUpUIState.uid?.let { uid ->
      val newUser = signUpViewModel.createLocalUser(uid)

      if (newUser != null) {
        userViewModel.setUser(newUser)
      }
      // Navigate away only after updating in-memory user
      onSignedUp()
    }
  }

  Scaffold(
      snackbarHost = {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.testTag(SignUpScreenTestTags.SNACKBAR).imePadding())
      },
      topBar = {
        TopAppBar(
            title = {},
            navigationIcon = {
              IconButton(
                  onClick = onBack, modifier = Modifier.testTag(SignUpScreenTestTags.BACK_BUTTON)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                  }
            })
      }) { padding ->
        LoadingOverlay(isLoading = signUpUIState.isLoading) {
          Column(
              Modifier.background(MaterialTheme.colorScheme.surface)
                  .fillMaxSize()
                  .padding(padding)
                  .padding(horizontal = 24.dp)
                  .testTag(SignUpScreenTestTags.SCREEN)
                  .verticalScroll(rememberScrollState()),
              horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Create An Account",
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.testTag(SignUpScreenTestTags.TITLE))
                Spacer(Modifier.height(24.dp))

                Field(
                    signUpUIState.firstname,
                    { signUpViewModel.setName(it) },
                    "Name",
                    modifier = Modifier.testTag(SignUpScreenTestTags.FIRSTNAME_FIELD))
                Field(
                    signUpUIState.lastname,
                    { signUpViewModel.setSurname(it) },
                    "Surname",
                    modifier = Modifier.testTag(SignUpScreenTestTags.LASTNAME_FIELD))
                Field(
                    signUpUIState.email,
                    { signUpViewModel.setEmail(it) },
                    "Email",
                    modifier = Modifier.testTag(SignUpScreenTestTags.EMAIL_FIELD),
                    signUpUIState.hasFailed && signUpUIState.emailIsMalformed())
                Field(
                    signUpUIState.password,
                    { signUpViewModel.setPassword(it) },
                    "Password",
                    modifier = Modifier.testTag(SignUpScreenTestTags.PASSWORD_FIELD),
                    signUpUIState.hasFailed && signUpUIState.passwordIsWeak())
                Field(
                    signUpUIState.cnfPassword,
                    { signUpViewModel.setCnfPassword(it) },
                    "Confirm Password",
                    modifier = Modifier.testTag(SignUpScreenTestTags.CONFIRM_PASSWORD_FIELD),
                    signUpUIState.hasFailed &&
                        (signUpUIState.cnfPassword != signUpUIState.password ||
                            signUpUIState.passwordIsWeak()))

                Spacer(Modifier.height(16.dp))
                Text("Are you a vet or a farmer ?", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))

                RoleSelector(
                    selected = signUpUIState.role, onSelected = { signUpViewModel.onSelected(it) })

                Spacer(Modifier.height(28.dp))
                Button(
                    onClick = { signUpViewModel.signUp() },
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(56.dp)
                            .testTag(SignUpScreenTestTags.SAVE_BUTTON),
                    shape = RoundedCornerShape(20.dp),
                ) {
                  Text("Save", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.height(24.dp))
              }
        }
      }
}

@Composable
private fun RoleSelector(selected: UserRole?, onSelected: (UserRole) -> Unit) {
  Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
    SelectablePill(
        text = "Farmer",
        selected = selected == UserRole.FARMER,
        onClick = { onSelected(UserRole.FARMER) },
        modifier = Modifier.testTag(SignUpScreenTestTags.FARMER_PILL))
    SelectablePill(
        text = "Vet",
        selected = selected == UserRole.VET,
        onClick = { onSelected(UserRole.VET) },
        modifier = Modifier.testTag(SignUpScreenTestTags.VET_PILL))
  }
}

@Composable
private fun SelectablePill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  val bg =
      if (selected) {
        MaterialTheme.colorScheme.primaryContainer
      } else MaterialTheme.colorScheme.tertiaryContainer
  Surface(
      onClick = onClick,
      shape = RoundedCornerShape(24.dp),
      color = bg,
      modifier = modifier.width(140.dp).height(56.dp)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(text, style = MaterialTheme.typography.titleMedium)
        }
      }
}

@Composable
private fun Field(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      placeholder = { Text(placeholder) },
      singleLine = true,
      modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
      isError = isError,
  )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignUpScreenPreview() {
  val authRepo = FakeAuthRepository()
  val vm = object : SignUpViewModel(authRepo) {}

  AgriHealthAppTheme { SignUpScreen(signUpViewModel = vm, userViewModel = FakeUserViewModel()) }
}
