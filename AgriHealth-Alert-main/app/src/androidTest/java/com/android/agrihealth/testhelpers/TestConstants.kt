package com.android.agrihealth.testhelpers

import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.testhelpers.TestUser.FARMER1
import com.android.agrihealth.testhelpers.TestUser.FARMER2
import com.android.agrihealth.testhelpers.TestUser.OFFICE1
import com.android.agrihealth.testhelpers.TestUser.OFFICE2
import com.android.agrihealth.testhelpers.TestUser.VET1
import java.time.LocalTime

object TestTimeout {
  const val SHORT_TIMEOUT = 2_000L
  const val DEFAULT_TIMEOUT = 3_000L
  const val LONG_TIMEOUT = 5_000L
  const val SUPER_LONG_TIMEOUT = 10_000L
}

object TestUser {
  val VET1 = Vet("ghi789", "Nazuna", "Amemiya", "email3@kms.josh", null, officeId = "off_1")
  val VET2 = Vet("mock_vet_id", "john", "john", "john@john.john", null, officeId = "off_2")

  val OFFICE1 =
      Office(
          id = VET1.officeId!!,
          name = "Agri Vet Clinic",
          address = Location(1.2, 3.4, "swag town"),
          description = "Providing quality veterinary services for farm animals.",
          vets = listOf(VET1.uid, "vet1b"),
          ownerId = VET1.uid,
          photoUrl = "/path/to/img.jpg")

  val OFFICE2 =
      Office(
          id = VET2.officeId!!,
          name = "swag central",
          address = Location(42.0, 6.7, "yverdon-les-bains"),
          description = "i just wanna be done with this task",
          vets = listOf(VET2.uid),
          ownerId = VET2.uid)

  val FARMER1 =
      Farmer(
          uid = "abc123",
          firstname = "Rushia",
          lastname = "Uruha",
          email = "email1@thing.com",
          address = Location(latitude = 46.5191, longitude = 6.5668, "place"),
          linkedOffices = listOf(OFFICE1.id),
          defaultOffice = OFFICE1.id,
          description = "not pettan")

  val FARMER2 =
      Farmer("def456", "mike", "neko", "email2@aaaaa.balls", null, listOf(OFFICE2.id), OFFICE2.id)
  val FARMER3 =
      Farmer(
          "jklABC",
          "John",
          "Fake",
          "fakeUser.glorp",
          null,
          listOf(OFFICE1.id, OFFICE2.id),
          OFFICE1.id)
}

object TestPassword {
  const val PASSWORD1 = "Password123"
  const val PASSWORD2 = "iamaweakpassword"
  const val PASSWORD3 = "12345678"
  const val WEAK_PASSWORD = "123"
  const val STRONG_PASSWORD = "definitelyAStrongEnoughPassword123/()"
}

object TestReport {
  val REPORT1 =
      Report(
          "rep_id1",
          "Report title 1",
          "Description 1",
          emptyList(),
          null,
          FARMER1.uid,
          OFFICE1.id,
          ReportStatus.PENDING,
          null,
          Location(46.9481, 7.4474, "Place name 1"),
          duration = LocalTime.of(0, 10),
          assignedVet = VET1.uid)
  val REPORT2 =
      Report(
          "rep_id2",
          "Report title 2",
          "Description aaaa 2",
          emptyList(),
          null,
          FARMER2.uid,
          OFFICE2.id,
          ReportStatus.IN_PROGRESS,
          "Vet answer",
          Location(46.9481, 7.4484))
  val REPORT3 =
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
  val REPORT4 =
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
