package com.android.agrihealth.data.model.report.form

/** Generates a list of questions to ask in a report, depending on the animal species */
object HealthQuestionFactory {
  fun questionsForSpecies(species: Species): List<QuestionForm> {
    return when (species) {
      Species.POULTRY -> poultryQuestions()
      Species.OVINE -> ovineQuestions()
    }
  }

  /** Returns a predefined list of questions to assess the animal's health. */
  fun animalHealthQuestions(): List<QuestionForm> {
    return listOf(

        // --- General Information ---

        MCQ(
            question = "How many animals are affected?",
            answers = listOf("1", "2–5", "More than 5")),
        MCQ(
            question = "How long ago did the symptoms appear?",
            answers = listOf("Less than 12 hours", "1–2 days", "More than 3 days")),
        MCQ(
            question = "Has the animal recently been vaccinated or treated?",
            answers = listOf("Yes", "No", "I don't know")),

        // --- Observed Symptoms ---

        MCQO(
            question =
                "Does the animal show signs of fever (lethargy, loss of appetite, elevated temperature)?",
            answers = listOf("Yes", "No", "I don't know")),
        YesOrNoQuestion(question = "Does the animal have diarrhea?"),
        YesOrNoQuestion(question = "Does the animal have nasal or eye discharge?"),
        YesOrNoQuestion(
            question =
                "Does the animal have visible lesions (wounds, swelling, redness, scabs, etc.)?"),
        MCQO(
            question = "If yes, where are the lesions located?",
            answers = listOf("Skin", "Legs", "Udder", "Head / Mouth")),
        YesOrNoQuestion(question = "Is the animal limping or having difficulty moving?"),
        YesOrNoQuestion(question = "Has the animal coughed or shown respiratory distress signs?"),

        // --- Behavior & Feeding ---

        MCQ(
            question = "Is the animal eating normally?",
            answers = listOf("Yes", "Less than usual", "Not at all")),
        MCQ(
            question = "Is the animal drinking normally?",
            answers = listOf("Yes", "Less than usual", "Not at all")),
        MCQ(
            question = "Does the animal show abnormal behavior?",
            answers = listOf("Lethargic", "Isolated", "Aggressive", "Apathetic", "No")))
  }

  fun poultryQuestions(): List<QuestionForm> {
    return listOf(
        MCQ(
            question = "What percentage of animals are affected (estimate)?",
            answers = listOf("Less than 10%", "10% – 30%", "30% – 60%", "More than 60%")),
        YesOrNoQuestion(question = "Is there a decrease in egg production?"),
        MCQ(
            question = "Have you observed changes in behavior?",
            answers = listOf("Lethargy", "Isolation", "Ruffled feathers", "No abnormalities")),
        MCQ(
            question =
                "Do the animals show respiratory signs (coughing, sneezing, noisy breathing)?",
            answers =
                listOf(
                    "Coughing",
                    "Sneezing",
                    "Noisy breathing",
                    "No respiratory signs",
                    "I don't know")),
        YesOrNoQuestion(question = "Is there diarrhea or abnormal droppings?"),
        YesOrNoQuestion(question = "Has there been sudden death of one or more individuals?"))
  }

  fun ovineQuestions(): List<QuestionForm> {
    return listOf(
        YesOrNoQuestion(question = "Are there wool losses or crusts on the skin?"),
        YesOrNoQuestion(question = "Do animals show coughing or difficulty breathing?"),
        YesOrNoQuestion(question = "Are there cases of lameness in the flock?"),
        YesOrNoQuestion(question = "Is there an unusual rate of mortality or abortion?"),
        YesOrNoQuestion(question = "Have new animals been introduced recently?"))
  }
}
