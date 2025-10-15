package com.android.agrihealth.ui.report

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.sp
import com.android.agrihealth.ui.authentification.SignUpScreenTestTags
import com.android.agrihealth.ui.farmer.AddReportViewModel


// For the automatic tests
object AddReportScreenTestTags {
    const val TITLE_FIELD = "titleField"
    const val DESCRIPTION_FIELD = "descriptionField"
    const val IMAGE_BUTTON = "imageButton"
    const val VET_DROPDOWN = "vetDropDown"
    const val CREATE_BUTTON = "createButton"
}


/**
 *  Displays the report creation screen for farmers
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


    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        // Decode Uri to Bitmap and store in ViewModel
        uri?.let {
            val bitmap = context.contentResolver.openInputStream(it)?.use { input ->
                BitmapFactory.decodeStream(input)
            }
            createReportViewModel.setImageBitmap(bitmap)
        }
    }

    Scaffold(
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
                            modifier = Modifier.weight(1f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onBack }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                })
        }) { padding ->

        // Main scrollable content
        Column(
            modifier =
                Modifier.padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        )
        {
            Spacer(Modifier.height(96.dp))
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
            // Add Picture button
            Button(
                onClick = {
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.fillMaxWidth().testTag(AddReportScreenTestTags.IMAGE_BUTTON)
            ) {
                Text("Add Picture")
            }

            // TODO: Add this prettier version of the button
            /*
            Button(
                onClick = {
                    pickMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3D9C1))
            ) {
                Icon(
                    imageVector = null,
                    contentDescription = "Add Picture",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Add Picture", fontSize = 18.sp)
            }
             */

            // Show the bitmap if present
            uiState.imageBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = { createReportViewModel.createReport() },   // TODO: Should we use "onCreateReport"?
                modifier =
                    Modifier.fillMaxWidth()
                        .height(56.dp)
                        .testTag(SignUpScreenTestTags.SAVE_BUTTON),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF96B7B1))
            ) {
                Text("Create Report", fontSize = 24.sp)
            }
            Spacer(Modifier.height(24.dp))
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
    modifier: Modifier = Modifier,
    testTag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp).testTag(AddReportScreenTestTags.TITLE_FIELD),
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