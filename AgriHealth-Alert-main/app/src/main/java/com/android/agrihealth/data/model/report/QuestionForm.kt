package com.android.agrihealth.data.model.report

/**
 * Represents a General Question with answer form.
 *
 * @param question the question the user needs to answer.
 * @param answers the possible answers given to the user, not relevant for open questions.
 * @param answerIndex the index at which the value chosen by the user is in the answers list, not
 *   relevant for open questions.
 * @param userAnswer the answer typed by the user, not relevant for MCQ and yes or no questions.
 * @param questionType the type of the answer, used to distinguish between different types of answer
 *   when storing data in firebase or rendering in UI.
 */
sealed class QuestionForm(
    val question: String,
    val answers: List<String>,
    val answerIndex: Int,
    val userAnswer: String,
    val questionType: QuestionType
) {
  override fun equals(other: Any?): Boolean {
    val otherForm = other as? QuestionForm ?: return false
    return (otherForm.question == question &&
        otherForm.questionType == questionType &&
        otherForm.userAnswer == userAnswer &&
        otherForm.answers == answers &&
        otherForm.answerIndex == answerIndex)
  }

  override fun hashCode(): Int {
    var result = answerIndex
    result = 31 * result + question.hashCode()
    result = 31 * result + answers.hashCode()
    result = 31 * result + userAnswer.hashCode()
    result = 31 * result + questionType.hashCode()
    return result
  }

  /** checks if a form has valid values. */
  abstract fun isValid(): Boolean
}

/** Question with a simple text field can type in to answer. */
class OpenQuestion(question: String, userAnswer: String = "") :
    QuestionForm(
        question = question,
        answers = emptyList(),
        answerIndex = -1,
        userAnswer = userAnswer,
        questionType = QuestionType.OPEN) {
  override fun isValid(): Boolean {
    return userAnswer.isNotBlank()
  }
}

/** Multiple choice question. */
class MCQ(question: String, answers: List<String>, answerIndex: Int = -1) :
    QuestionForm(
        question = question,
        answers = answers,
        answerIndex = answerIndex,
        userAnswer = "",
        questionType = QuestionType.MCQ) {
  override fun isValid(): Boolean {
    return answerIndex >= 0 && answerIndex < answers.size
  }
}

/** Special case of an MCQ where the answer is only yes or no. */
class YesOrNoQuestion(question: String, answerIndex: Int = -1) :
    QuestionForm(
        question = question,
        answers = listOf("Yes", "No"),
        answerIndex = answerIndex,
        userAnswer = "",
        questionType = QuestionType.YESORNO) {
  override fun isValid(): Boolean {
    return answerIndex >= 0 && answerIndex < 2
  }
}

/** MCQ with an added "other" option where the user can type his answer. */
class MCQO(
    question: String,
    answers: List<String>,
    answerIndex: Int = -1,
    userAnswer: String = ""
) :
    QuestionForm(
        question = question,
        answers = answers,
        answerIndex = answerIndex,
        userAnswer = userAnswer,
        questionType = QuestionType.MCQO) {
  override fun isValid(): Boolean {
    return answerIndex >= 0 && answerIndex <= answers.size
  }
}

enum class QuestionType {
  OPEN,
  YESORNO,
  MCQ,
  MCQO
}
