package com.android.agrihealth.data.model.report

import com.android.agrihealth.data.model.report.form.HealthQuestionFactory
import com.android.agrihealth.data.model.report.form.Species
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
}
