Loading Questions When a Species Is Selected

Each species has:
- **General questions**
- **Species-specific questions**

Both must be loaded together when the user selects a species.

```kotlin
fun onSpeciesSelected(species: Species) {
    val general = HealthQuestionFactory.generalAnimalQuestions()
    val specific = HealthQuestionFactory.questionsForSpecies(species)

    _uiState.update {
        it.copy(
            selectedSpecies = species,
            questionForms = general + specific
            // GENERAL QUESTIONS + SPECIES-SPECIFIC QUESTIONS
        )
    }
}

The screen should include a Species selector at the top
(e.g., radio buttons or a dropdown). So when the user picks a species, 
it  triggers the loading of the correct question list.


You do:
Let the user choose a Species.
Call viewModel.onSpeciesSelected(species).
