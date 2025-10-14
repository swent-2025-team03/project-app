package com.android.agrihealth.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.android.agrihealth.ui.authentification.SignUpScreenTestTags


/**
*  Displays the report creation screen for farmers
*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReportScreen(
    onBack: () -> Unit = {},
    onCreateReport: () -> Unit = {},
    createReportViewModel: CreateReportViewModel = viewModel()
) {


    val uiState by createReportViewModel.uiState.collectAsState()

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
                value = uiState.title,
                { createReportViewModel.setTitle(it) },
                "Title"
            )
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
                Text("Save", fontSize = 24.sp)
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


/**
 * Preview of the ReportViewScreen for both farmer and vet roles. Allows testing of layout and
 * colors directly in Android Studio.
 */


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CreateReportScreenPreview() {
    MaterialTheme { CreateReportScreen() }
}