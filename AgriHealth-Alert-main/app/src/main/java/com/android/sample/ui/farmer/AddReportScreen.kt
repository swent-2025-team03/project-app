package com.android.sample.ui.farmer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun AddReportScreen(
    onDone: () -> Unit = {},
    onGoBack: () -> Unit = {},
) {
  // Implementation of the Add Report Screen

  Column {
    Text(text = "Add Report Screen")
    Button(onClick = onDone, modifier = Modifier.padding(8.dp)) { Text(text = "Done") }
  }
}
