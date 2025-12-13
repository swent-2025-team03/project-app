package com.android.agrihealth.testhelpers

import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.Vet

object TestTimeout {
  const val SHORT_TIMEOUT = 2_000L
  const val DEFAULT_TIMEOUT = 3_000L
  const val LONG_TIMEOUT = 5_000L
  const val SUPER_LONG_TIMEOUT = 10_000L
}

object TestUser {
  val farmer1 =
    Farmer(
      "abc123",
      "Rushia",
      "Uruha",
      "email1@thing.com",
      Location(latitude = 46.5191, longitude = 6.5668),
      listOf("Best Office Ever!", "Meh Office."),
      "Test?")

  val farmer2 = Farmer("def456", "mike", "neko", "email2@aaaaa.balls", null, emptyList(), null)
  val farmer3 = Farmer("jklABC", "John", "Fake", "fakeUser.glorp", null, emptyList(), null)

  val vet1 = Vet("ghi789", "Nazuna", "Amemiya", "email3@kms.josh", null, officeId = "off1")

  val office1 = Office("off1", "o", ownerId = vet1.uid)
}

object TestPassword {
  val password1 = "Password123"
  val password2 = "iamaweakpassword"
  val password3 = "12345678"
  val password4 = "weak"
}