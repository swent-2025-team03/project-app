@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.agrihealth.ui.authentification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.data.model.authentification.UserRole

private val FieldBg = Color(0xFFF0F6F1)

object SignUpScreenTestTags {
  const val BACK_BUTTON = "BackButton"
  const val TITLE = "SignUpTitle"
  const val NAME_FIELD = "NameField"
  const val SURNAME_FIELD = "SurnameField"
  const val EMAIL_FIELD = "EmailField"
  const val PASSWORD_FIELD = "PasswordField"
  const val CONFIRM_PASSWORD_FIELD = "ConfirmPasswordField"
  const val SAVE_BUTTON = "SaveButton"
  const val FARMER_PILL = "FarmerPill"
  const val VET_PILL = "VetPill"
}

@Composable
fun SignUpScreen(
    onBack: () -> Unit = {},
    onSignedUp: () -> Unit = {},
    signUpViewModel: SignUpViewModel = viewModel()
) {
  val signUpUIState by signUpViewModel.uiState.collectAsState()

  LaunchedEffect(signUpUIState.user) { signUpUIState.user?.let { onSignedUp() } }

  Scaffold(
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
        Column(
            Modifier.background(FieldBg).fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Spacer(Modifier.height(24.dp))
              Text(
                  "Create An Account",
                  fontSize = 36.sp,
                  fontWeight = FontWeight.SemiBold,
                  modifier = Modifier.testTag(SignUpScreenTestTags.TITLE))
              Spacer(Modifier.height(24.dp))

              Field(
                  signUpUIState.name,
                  { signUpViewModel.setName(it) },
                  "Name",
                  modifier = Modifier.testTag(SignUpScreenTestTags.NAME_FIELD))
              Field(
                  signUpUIState.surname,
                  { signUpViewModel.setSurname(it) },
                  "Surname",
                  modifier = Modifier.testTag(SignUpScreenTestTags.SURNAME_FIELD))
              Field(
                  signUpUIState.email,
                  { signUpViewModel.setEmail(it) },
                  "Email",
                  modifier = Modifier.testTag(SignUpScreenTestTags.EMAIL_FIELD))
              Field(
                  signUpUIState.password,
                  { signUpViewModel.setPassword(it) },
                  "Password",
                  modifier = Modifier.testTag(SignUpScreenTestTags.PASSWORD_FIELD))
              Field(
                  signUpUIState.cnfPassword,
                  { signUpViewModel.setCnfPassword(it) },
                  "Confirm Password",
                  modifier = Modifier.testTag(SignUpScreenTestTags.CONFIRM_PASSWORD_FIELD))

              Spacer(Modifier.height(16.dp))
              Text(
                  "Are you a vet or a farmer ?", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
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
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF96B7B1))) {
                    Text("Save", fontSize = 24.sp)
                  }
              Spacer(Modifier.height(24.dp))
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
        selected = selected == UserRole.VETERINARIAN,
        onClick = { onSelected(UserRole.VETERINARIAN) },
        modifier = Modifier.testTag(SignUpScreenTestTags.VET_PILL))
  }
}

private val UnselectedColor = Color(0xFFE5E5E5)
private val SelectedColor = Color(0xFF96B7B1) // vert

@Composable
private fun SelectablePill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  val bg = if (selected) SelectedColor else UnselectedColor // vert / gris
  Surface(
      onClick = onClick,
      shape = RoundedCornerShape(24.dp),
      color = bg,
      modifier = modifier.width(140.dp).height(56.dp)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(text, style = MaterialTheme.typography.titleMedium, color = Color.Black)
        }
      }
}

private val unfocusedFieldColor = Color(0xFFF0F7F1)
private val focusedFieldColor = Color(0xFFF0F7F1)

@Composable
private fun Field(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      placeholder = { Text(placeholder) },
      singleLine = true,
      shape = RoundedCornerShape(28.dp),
      modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
      colors =
          OutlinedTextFieldDefaults.colors(
              unfocusedContainerColor = unfocusedFieldColor,
              focusedContainerColor = focusedFieldColor,
              unfocusedBorderColor = Color.Transparent,
              focusedBorderColor = Color.Transparent))
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignUpScreenPreview() {
  MaterialTheme { SignUpScreen() }
}
