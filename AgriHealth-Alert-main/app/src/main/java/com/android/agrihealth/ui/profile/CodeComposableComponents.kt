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
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.TOP_BAR
import kotlinx.coroutines.launch

object CodeComposableComponentsTestTags {
  const val CODE_FIELD = "OfficeCodeField"
  const val ADD_CODE_BUTTON = "AddOfficeButton"
  const val GENERATE_BUTTON = "GenerateButton"
  const val GENERATE_FIELD = "GenerateField"
  const val SAVE_BUTTON = "SaveButton"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimCodeScreen(
    codesViewModel: CodesViewModel,
    onGoBack: () -> Unit = {},
) {

  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  val claimMessage by codesViewModel.claimMessage.collectAsState()
  LaunchedEffect(claimMessage) { claimMessage?.let { snackbarHostState.showSnackbar(it) } }

  var code by remember { mutableStateOf("") }

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
                  value = code,
                  onValueChange = { code = it },
                  label = { Text("Enter Office Code") },
                  modifier =
                      Modifier.fillMaxWidth().testTag(CodeComposableComponentsTestTags.CODE_FIELD))

              Spacer(modifier = Modifier.height(8.dp))

              Button(
                  onClick = {
                    if (code.isBlank()) {
                      scope.launch { snackbarHostState.showSnackbar("Please enter a code.") }
                    } else {
                      codesViewModel.claimCode(code)
                    }
                  },
                  modifier =
                      Modifier.align(Alignment.CenterHorizontally)
                          .testTag(CodeComposableComponentsTestTags.ADD_CODE_BUTTON)) {
                    Text("claim Code")
                  }
              Spacer(modifier = Modifier.weight(1f))
              Button(
                  onClick = onGoBack,
                  modifier =
                      Modifier.fillMaxWidth()
                          .testTag(CodeComposableComponentsTestTags.SAVE_BUTTON)) {
                    Text("Save Changes")
                  }
            }
      }
}

@Composable
fun GenerateCode(
    codesViewModel: CodesViewModel,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
  val code by codesViewModel.generatedCode.collectAsState()
  Button(
      onClick = { codesViewModel.generateCode() },
      modifier = modifier.testTag(CodeComposableComponentsTestTags.GENERATE_BUTTON)) {
        Text("Generate new Connection Code")
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
              modifier =
                  modifier
                      .padding(end = 8.dp)
                      .testTag(CodeComposableComponentsTestTags.GENERATE_FIELD))
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
