package com.android.agrihealth.ui.report

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.agrihealth.data.model.report.MCQ
import com.android.agrihealth.data.model.report.MCQO
import com.android.agrihealth.data.model.report.OpenQuestion
import com.android.agrihealth.data.model.report.YesOrNoQuestion

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
            onValueChange = { newAnswer ->
                onAnswerChange(OpenQuestion(question.question, newAnswer))
            },
            label = { Text("Open Question") },
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = enabled
        )
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
    Row (verticalAlignment = Alignment.CenterVertically) {
        listOf("Yes", "No").forEachIndexed { idx, text ->
            RadioButton(
                selected = question.answerIndex == idx,
                onClick = { onAnswerChange(YesOrNoQuestion(question.question, idx)) },
                enabled = enabled,
                modifier = modifier
            )
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
    Column (modifier = Modifier.padding(vertical = 8.dp)) {
        Text(question.question, modifier = Modifier.padding(bottom = 4.dp))
        question.answers.forEachIndexed { idx, answer ->
            Row (verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = question.answerIndex == idx,
                    onClick = if (enabled) {
                        { onAnswerChange(MCQ(question.question, question.answers, idx)) }
                    } else {
                        {}
                    },
                    enabled = enabled,
                    modifier = modifier
                )
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
    Column (modifier = Modifier.padding(vertical = 8.dp)) {
        Text(question.question, modifier = Modifier.padding(bottom = 4.dp))
        question.answers.forEachIndexed { idx, answer ->
            Row (verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = question.answerIndex == idx,
                    onClick = if (enabled) {
                        { onAnswerChange(MCQO(question.question, question.answers, idx, question.userAnswer)) }
                    } else {
                        {}
                    },
                    enabled = enabled,
                    modifier = modifier
                )
                Text(answer)
            }
        }
        OutlinedTextField(
            value = question.userAnswer,
            onValueChange = { onAnswerChange(MCQO(question.question, question.answers, question.answerIndex, it))
            },
            label = { Text("Other") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            enabled = enabled
        )
    }
}
