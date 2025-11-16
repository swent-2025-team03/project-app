package com.android.agrihealth.data.model.report

import org.junit.Assert.assertEquals
import org.junit.Test

class HealthQuestionFactoryTest {

    @Test
    fun routing_poultry_returns_only_poultry() {
        val expected = HealthQuestionFactory.poultryQuestions().size
        val result = HealthQuestionFactory.questionsForSpecies(Species.POULTRY)

        assertEquals(expected, result.size)
    }

    @Test
    fun routing_ovine_returns_only_ovine() {
        val expected = HealthQuestionFactory.ovineQuestions().size
        val result = HealthQuestionFactory.questionsForSpecies(Species.OVINE)

        assertEquals(expected, result.size)
    }

    // TODO:
//  - This test file only verifies that `questionsForSpecies()` returns
//    the species-specific questions (NOT general questions).
//    This is correct because the Factory is responsible only for providing
//    species-specific lists.
//
//  - The "general + specific" combination MUST be tested in the ViewModel.
//    The ViewModel calls:
//
//        val general = HealthQuestionFactory.animalHealthQuestions()
//        val specific = HealthQuestionFactory.questionsForSpecies(species)
//        questionForms = general + specific
//
//  - Therefore, create a separate ViewModel test that verifies:
//
//        expectedGeneral + expectedSpecific == uiState.questionForms.size
//
//  Example ViewModel test:
//
//        vm.onSpeciesSelected(Species.POULTRY)
//        assertEquals(generalCount + poultryCount, vm.uiState.value.questionForms.size)
//
//  - Do NOT test the combination logic here in the factory test, because
//    the factory is not responsible for merging general + specific questions.

}
