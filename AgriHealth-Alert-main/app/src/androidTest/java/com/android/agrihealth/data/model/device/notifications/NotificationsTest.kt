package com.android.agrihealth.data.model.device.notifications

import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.data.model.user.copyCommon
import com.android.agrihealth.testhelpers.fakes.FakeNotificationSender
import com.android.agrihealth.testhelpers.fakes.FakeNotificationTokenResolver
import com.android.agrihealth.testhelpers.fakes.FakeUserRepository
import com.google.firebase.messaging.RemoteMessage
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.collections.iterator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationsTest {
  private val fakeToken = "1234"
  private val userRepository = FakeUserRepository()
  private val tokenResolver = FakeNotificationTokenResolver(fakeToken)
  private val sender = FakeNotificationSender(userRepository)

  val messagingService = NotificationHandlerFirebase(tokenResolver, sender)

  private val user1 =
      Farmer(
          "uid1",
          "dreamy",
          "bull",
          "ambatu@farm.com",
          null,
          defaultOffice = null,
          deviceTokensFCM = setOf(fakeToken))
  private val user2 = Vet("uid2", "just", "jo", "uhoh@turbulence.com", null)

  @Before
  fun setUp() = runTest {
    userRepository.addUser(user1.copyCommon(deviceTokensFCM = setOf(fakeToken)))
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun getToken_succeedsWithTokenProvider() = runTest {
    var newToken: String? = null
    val latch = CountDownLatch(1)

    messagingService.getToken { token ->
      newToken = token
      latch.countDown()
    }

    assertTrue(latch.await(2, TimeUnit.SECONDS)) // wait for lambda to execute

    assertEquals(fakeToken, newToken)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun getToken_failsWithBadTokenProvider() = runTest {
    val badTokenResolver = FakeNotificationTokenResolver(null)
    val badMessagingService = NotificationHandlerFirebase(badTokenResolver)

    var newToken: String? = ""
    val latch = CountDownLatch(1)

    badMessagingService.getToken { token ->
      newToken = token
      latch.countDown()
    }

    assertTrue(latch.await(2, TimeUnit.SECONDS))

    assertEquals(null, newToken)
  }

  @Test
  fun uploadNotification_succeedsWithAllTypes() = runTest {
    val notificationNewReport =
        Notification.NewReport(destinationUid = user1.uid, description = "drop drop it fire")

    val notificationVetAnswer =
        Notification.VetAnswer(destinationUid = user1.uid, description = "rope neck asap")

    val notificationJoinOffice =
        Notification.JoinOffice(destinationUid = user1.uid, description = "sorahe mau")

    val notificationConnectOffice =
        Notification.ConnectOffice(destinationUid = user1.uid, description = "sekaino kanata")

    messagingService.uploadNotification(notificationNewReport) { assertTrue(it) }
    messagingService.uploadNotification(notificationVetAnswer) { assertTrue(it) }
    messagingService.uploadNotification(notificationJoinOffice) { assertTrue(it) }
    messagingService.uploadNotification(notificationConnectOffice) { assertTrue(it) }
  }

  @Test
  fun uploadNotification_failsWithUnknownUser() = runTest {
    val notif =
        Notification.NewReport(
            destinationUid = user2.uid, description = "o o e o reaching high reaching higher")

    messagingService.uploadNotification(notif) { assertFalse(it) }
  }

  @Test
  fun uploadNotification_failsWithEmptyUid() = runTest {
    val notifNullUid = Notification.NewReport(destinationUid = "", description = "Empty UID test")
    messagingService.uploadNotification(notifNullUid) { success ->
      assertFalse("Notification with empty UID should fail", success)
    }
  }

  @Test
  fun uploadNotification_failsWhenUserHasNoTokens() = runTest {
    userRepository.addUser(user2.copyCommon(deviceTokensFCM = setOf()))

    val notif =
        Notification.NewReport(destinationUid = user2.uid, description = "every night every day")

    messagingService.uploadNotification(notif) { assertFalse(it) }
  }

  @Test
  // Cannot use showNotification directly because it's a service and it crashes the app
  fun onMessageReceived_callsShowNotificationWithCorrectInfo() = runTest {
    val spy = spyk(NotificationHandlerFirebase(tokenResolver, sender))
    val slot = slot<Notification>()

    every { spy.showNotification(capture(slot)) } just Runs

    val notificationNewReport =
        Notification.NewReport(destinationUid = user1.uid, description = "makes me sick")
    val notificationVetAnswer =
        Notification.VetAnswer(
            destinationUid = user1.uid, description = "when you're acting like that")
    val notificationJoinOffice =
        Notification.JoinOffice(destinationUid = user1.uid, description = "yamiwo terasu")
    val notificationConnectOffice =
        Notification.ConnectOffice(destinationUid = user1.uid, description = "kaisei")

    val messageNR = dataToRemoteMessage(notificationNewReport.toDataMap())
    val messageVA = dataToRemoteMessage(notificationVetAnswer.toDataMap())
    val messageJO = dataToRemoteMessage(notificationJoinOffice.toDataMap())
    val messageCO = dataToRemoteMessage(notificationConnectOffice.toDataMap())

    spy.onMessageReceived(messageNR)
    val capturedNR = slot.captured
    slot.clear()
    spy.onMessageReceived(messageVA)
    val capturedVA = slot.captured
    slot.clear()
    spy.onMessageReceived(messageJO)
    val capturedJO = slot.captured
    slot.clear()
    spy.onMessageReceived(messageCO)
    val capturedCO = slot.captured

    assertEquals(notificationNewReport, capturedNR)
    assertEquals(notificationVetAnswer, capturedVA)
    assertEquals(notificationJoinOffice, capturedJO)
    assertEquals(notificationConnectOffice, capturedCO)

    clearMocks(spy)
  }

  @Test
  fun onMessageReceived_handlesBadMessageProperly() = runTest {
    val spy = spyk(NotificationHandlerFirebase(tokenResolver, sender))

    every { spy.showNotification(any()) } just Runs

    val badMessage =
        dataToRemoteMessage(
            mapOf("got me going like" to "vroom vroom", "no ice" to "big chains gold watch"))

    spy.onMessageReceived(badMessage)

    verify(exactly = 0) { spy.showNotification(any()) }

    clearMocks(spy)
  }

  private fun dataToRemoteMessage(data: Map<String, String>): RemoteMessage {
    return RemoteMessage.Builder("yeah_yeah_put_your_hands@fcm.googleapis.com")
        .apply {
          for ((key, value) in data) {
            addData(key, value)
          }
        }
        .build()
  }
}
