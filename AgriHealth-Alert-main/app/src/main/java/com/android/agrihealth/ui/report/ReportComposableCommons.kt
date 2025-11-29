package com.android.agrihealth.ui.report

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.agrihealth.data.model.report.MCQ
import com.android.agrihealth.data.model.report.MCQO
import com.android.agrihealth.data.model.report.OpenQuestion
import com.android.agrihealth.data.model.report.YesOrNoQuestion

object ReportComposableCommonsTestTags {
  const val COLLECTED_SWITCH = "collectedSwitch"
}

@Composable
fun OpenQuestionItem(
    question: OpenQuestion,
    onAnswerChange: (OpenQuestion) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
  Column(modifier = Modifier.padding(vertical = 8.dp)) {
    Text(question.question)
    OutlinedTextField(
        value = question.userAnswer,
        onValueChange = { newAnswer -> onAnswerChange(question.copy(userAnswer = newAnswer)) },
        label = { Text("Open Question") },
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        enabled = enabled)
  }
}

@Composable
fun YesOrNoQuestionItem(
    question: YesOrNoQuestion,
    onAnswerChange: (YesOrNoQuestion) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
  Column(modifier = Modifier.padding(vertical = 8.dp)) {
    Text(question.question, modifier = Modifier.padding(bottom = 4.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
      listOf("Yes", "No").forEachIndexed { idx, text ->
        RadioButton(
            selected = question.answerIndex == idx,
            onClick = { onAnswerChange(question.copy(answerIndex = idx)) },
            enabled = enabled,
            modifier = modifier)
        Text(text)
      }
    }
  }
}

@Composable
fun MCQItem(
    question: MCQ,
    onAnswerChange: (MCQ) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
  Column(modifier = Modifier.padding(vertical = 8.dp)) {
    Text(question.question, modifier = Modifier.padding(bottom = 4.dp))
    question.answers.forEachIndexed { idx, answer ->
      Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(
            selected = question.answerIndex == idx,
            onClick =
                if (enabled) {
                  { onAnswerChange(question.copy(answerIndex = idx)) }
                } else {
                  {}
                },
            enabled = enabled,
            modifier = modifier)
        Text(answer)
      }
    }
  }
}

@Composable
fun MCQOItem(
    question: MCQO,
    onAnswerChange: (MCQO) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
  Column(modifier = Modifier.padding(vertical = 8.dp)) {
    Text(question.question, modifier = Modifier.padding(bottom = 4.dp))
    question.answers.forEachIndexed { idx, answer ->
      Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(
            selected = question.answerIndex == idx,
            onClick =
                if (enabled) {
                  { onAnswerChange(question.copy(answerIndex = idx)) }
                } else {
                  {}
                },
            enabled = enabled,
            modifier = modifier)
        Text(answer)
      }
    }
    OutlinedTextField(
        value = question.userAnswer,
        onValueChange = { onAnswerChange(question.copy(userAnswer = it)) },
        label = { Text("Other") },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        enabled = enabled)
  }
}

@Composable
fun CollectedSwitch(
    checked: Boolean = false,
    onCheckedChange: () -> Unit = {},
    enabled: Boolean = false
) {
  Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.testTag(ReportComposableCommonsTestTags.COLLECTED_SWITCH)) {
        Text("Share anonymous data")
        Spacer(modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = { onCheckedChange }, enabled = enabled)
      }
}
