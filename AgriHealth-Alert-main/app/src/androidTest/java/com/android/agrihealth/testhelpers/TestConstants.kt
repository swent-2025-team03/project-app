package com.android.agrihealth.testhelpers

import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.testhelpers.TestUser.farmer1
import com.android.agrihealth.testhelpers.TestUser.office1
import com.android.agrihealth.testhelpers.TestUser.vet1
import java.time.LocalTime

object TestTimeout {
  const val SHORT_TIMEOUT = 2_000L
  const val DEFAULT_TIMEOUT = 3_000L
  const val LONG_TIMEOUT = 5_000L
  const val SUPER_LONG_TIMEOUT = 10_000L
}

object TestUser {
  val farmer1 =
      Farmer(
          uid = "abc123",
          firstname = "Rushia",
          lastname = "Uruha",
          email = "email1@thing.com",
          address = Location(latitude = 46.5191, longitude = 6.5668),
          linkedOffices = listOf("Best Office Ever!", "Meh Office."),
          defaultOffice = "Test?",
          description = "not pettan")

  val farmer2 = Farmer("def456", "mike", "neko", "email2@aaaaa.balls", null, emptyList(), null)
  val farmer3 = Farmer("jklABC", "John", "Fake", "fakeUser.glorp", null, emptyList(), null)

  val vet1 = Vet("ghi789", "Nazuna", "Amemiya", "email3@kms.josh", null, officeId = "off_1")
  val vet2 = Vet("mock_vet_id", "john", "john", "john@john.john", null)

  val office1 =
      Office(
          id = vet1.officeId!!,
          name = "Agri Vet Clinic",
          address = Location(1.2, 3.4, "swag town"),
          description = "Providing quality veterinary services for farm animals.",
          vets = listOf(vet1.uid, "vet2"),
          ownerId = vet1.uid,
          photoUrl = "/path/to/img.jpg")
}

object TestPassword {
  val password1 = "Password123"
  val password2 = "iamaweakpassword"
  val password3 = "12345678"
  val weakestPassword = "123"
  val strongestPassword = "definitelyAStrongEnoughPassword123/()"
}

object TestReport {
  val report1 =
      Report(
          "rep_id1",
          "Report title 1",
          "Description 1",
          emptyList(),
          null,
          farmer1.uid,
          office1.id,
          ReportStatus.PENDING,
          null,
          Location(46.9481, 7.4474, "Place name 1"),
          duration = LocalTime.of(0, 10),
          assignedVet = vet1.uid)
  val report2 =
      Report(
          "rep_id2",
          "Report title 2",
          "Description aaaa 2",
          emptyList(),
          null,
          "farmerId2",
          "officeId2",
          ReportStatus.IN_PROGRESS,
          "Vet answer",
          Location(46.9481, 7.4484))
  val report3 =
      Report(
          "rep_id3",
          "Report title 3",
          "Description 3",
          emptyList(),
          null,
          "farmerId3",
          "officeId1",
          ReportStatus.RESOLVED,
          null,
          Location(46.9481, 7.4464, "Place name 3"))
  val report4 =
      Report(
          "rep_id4",
          "Report title 4",
          "Description aaaa 4",
          emptyList(),
          null,
          "farmerId4",
          "officeId4",
          ReportStatus.SPAM,
          "Vet answer 4",
          Location(46.9491, 7.4474))
}
