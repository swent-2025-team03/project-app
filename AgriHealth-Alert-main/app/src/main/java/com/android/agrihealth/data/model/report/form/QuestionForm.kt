package com.android.agrihealth.data.model.report.form

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
    open val question: String,
    open val answers: List<String>,
    open val answerIndex: Int,
    open val userAnswer: String,
    open val questionType: QuestionType
) {
  /** checks if a form has valid values. */
  abstract fun isValid(): Boolean
}

/** Question with a simple text field can type in to answer. */
data class OpenQuestion(override val question: String, override val userAnswer: String = "") :
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
data class MCQ(
    override val question: String,
    override val answers: List<String>,
    override val answerIndex: Int = -1
) :
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
data class YesOrNoQuestion(override val question: String, override val answerIndex: Int = -1) :
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
data class MCQO(
    override val question: String,
    override val answers: List<String>,
    override val answerIndex: Int = -1,
    override val userAnswer: String = ""
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
