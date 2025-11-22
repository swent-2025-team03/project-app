package com.android.agrihealth.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.android.agrihealth.ui.navigation.NavigationTestTags.GO_BACK_BUTTON
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.CODE_BUTTON_VET
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.GENERATED_CODE_TEXT
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.TOP_BAR
import kotlinx.coroutines.launch

object ClaimCodeScreenTestTags {
  const val CODE_FIELD = "OfficeCodeField"
  const val ADD_CODE_BUTTON = "AddOfficeButton"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimCodeScreen(
    profileViewModel: ProfileViewModel,
    onGoBack: () -> Unit = {},
) {

  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  val vetClaimMessage by profileViewModel.vetClaimMessage.collectAsState()
  LaunchedEffect(vetClaimMessage) { vetClaimMessage?.let { snackbarHostState.showSnackbar(it) } }

  var vetCode by remember { mutableStateOf("") }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Claim a code", style = MaterialTheme.typography.titleMedium) },
            navigationIcon = {
              IconButton(onClick = onGoBack, modifier = Modifier.testTag(GO_BACK_BUTTON)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
              }
            },
            modifier = Modifier.testTag(TOP_BAR))
      },
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { innerPadding ->
        Column(
            modifier =
                Modifier.padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
              HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

              Spacer(modifier = Modifier.height(16.dp))

              OutlinedTextField(
                  value = vetCode,
                  onValueChange = { vetCode = it },
                  label = { Text("Enter Office Code") },
                  modifier = Modifier.fillMaxWidth().testTag(ClaimCodeScreenTestTags.CODE_FIELD))

              Spacer(modifier = Modifier.height(8.dp))

              Button(
                  onClick = {
                    if (vetCode.isBlank()) {
                      scope.launch { snackbarHostState.showSnackbar("Please enter a code.") }
                    } else {
                      profileViewModel.claimVetCode(vetCode)
                    }
                  },
                  modifier =
                      Modifier.align(Alignment.CenterHorizontally)
                          .testTag(ClaimCodeScreenTestTags.ADD_CODE_BUTTON)) {
                    Text("claim Code")
                  }
              Spacer(modifier = Modifier.weight(1f))
              Button(
                  onClick = onGoBack,
                  modifier =
                      Modifier.fillMaxWidth().testTag(EditProfileScreenTestTags.SAVE_BUTTON)) {
                    Text("Save Changes")
                  }
            }
      }
}

@Composable
fun GenerateCode(
    profileViewModel: ProfileViewModel,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
  val code by profileViewModel.generatedCode.collectAsState()
  Button(
      onClick = { profileViewModel.generateVetCode() },
      modifier = modifier.testTag(CODE_BUTTON_VET)) {
        Text("Generate new Farmer's Code")
      }

  if (code != null) {
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center) {
          Text(
              "Generated Code: $code",
              style = MaterialTheme.typography.bodyLarge,
              modifier = modifier.padding(end = 8.dp).testTag(GENERATED_CODE_TEXT))
          IconButton(
              onClick = {
                clipboard.setText(AnnotatedString(code!!))
                scope.launch { snackbarHostState.showSnackbar("Code copied!") }
              }) {
                Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Copy Code")
              }
        }
  }
}
