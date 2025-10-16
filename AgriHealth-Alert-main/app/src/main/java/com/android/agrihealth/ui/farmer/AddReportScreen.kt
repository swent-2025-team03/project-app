package com.android.agrihealth.ui.farmer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.android.agrihealth.ui.navigation.NavigationTestTags
import kotlinx.coroutines.launch


/**
 *  Tags for the various components. For testing purposes
 */
object AddReportScreenTestTags {
    const val TITLE_FIELD = "titleField"
    const val DESCRIPTION_FIELD = "descriptionField"
    const val IMAGE_BUTTON = "imageButton"
    const val VET_DROPDOWN = "vetDropDown"
    const val CREATE_BUTTON = "createButton"
    const val SCREEN = "AddReportScreen"
}

/**
 *  Texts for the report creation feedback. For testing purposes
 */
object AddReportFeedbackTexts {
    const val SUCCESS = "Report created successfully!"
    const val FAILURE = "Please fill in all required fields..."
}


private val unfocusedFieldColor = Color(0xFFF0F7F1)
private val focusedFieldColor = Color(0xFFF0F7F1)
private val createReportButton = Color(0xFF96B7B1)


/**
 *  Displays the report creation screen for farmers
 *
 *
 * @param onBack A callback invoked when the back button in the top bar is pressed.
 * @param onCreateReport A callback invoked when a report is successfully created.
 * @param createReportViewModel The [AddReportViewModel] instance responsible for managing
 * report creation logic and UI state.
 *
 * @see AddReportViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReportScreen(
    onBack: () -> Unit = {},
    onCreateReport: () -> Unit = {},
    createReportViewModel: AddReportViewModel = viewModel()
) {


    val uiState by createReportViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // For the dropdown menu
    val vetOptions = listOf("Best Vet Ever!", "Meh Vet", "Great Vet")   // TODO: Dummy list must change later
    var expanded by remember { mutableStateOf(false) }  // For menu expanded/collapsed tracking
    val selectedOption = uiState.chosenVet

    // For the confirmation snackbar (i.e alter window)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            // Top bar with back arrow and title/status
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Create New Report",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f).testTag(NavigationTestTags.TOP_BAR_TITLE)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.testTag(NavigationTestTags.GO_BACK_BUTTON)
                        )
                    }
                })
        }) { padding ->

        // Main scrollable content
        Column(
            modifier =
                Modifier.padding(padding)
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        )
        {
            Field(
                uiState.title,
                { createReportViewModel.setTitle(it) },
                "Title",
                Modifier,
                AddReportScreenTestTags.TITLE_FIELD
            )
            Field(
                value = uiState.description,
                { createReportViewModel.setDescription(it) },
                "Description",
                Modifier,
                AddReportScreenTestTags.DESCRIPTION_FIELD
            )



            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedOption,
                    onValueChange = { }, // No direct text editing
                    readOnly = true,
                    label = { Text("Choose an Option") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor() // Needed for M3 dropdown alignment
                        .fillMaxWidth()
                        .testTag(AddReportScreenTestTags.VET_DROPDOWN)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    vetOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                createReportViewModel.setVet(option)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            Button(
                onClick = {
                    val created = createReportViewModel.createReport()
                    scope.launch {
                        if (created) {
                            snackbarHostState.showSnackbar(AddReportFeedbackTexts.SUCCESS)
                            onCreateReport()
                        } else {
                            snackbarHostState.showSnackbar(AddReportFeedbackTexts.FAILURE)
                        }
                    }
                },
                modifier =
                    Modifier.fillMaxWidth()
                        .height(56.dp)
                        .testTag(AddReportScreenTestTags.CREATE_BUTTON),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = createReportButton)
            ) {
                Text("Create Report", fontSize = 24.sp)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Field(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    testTag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp).testTag(testTag),
        colors =
            OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = unfocusedFieldColor,
                focusedContainerColor = focusedFieldColor,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent))
}


/**
 * Preview of the ReportViewScreen for both farmer and vet roles. Allows testing of layout and
 * colors directly in Android Studio.
 */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AddReportScreenPreview() {
    MaterialTheme { AddReportScreen() }
}