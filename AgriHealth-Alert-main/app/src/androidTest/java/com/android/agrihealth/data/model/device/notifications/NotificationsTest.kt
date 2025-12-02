package com.android.agrihealth.data.model.device.notifications

import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsTest
import com.android.agrihealth.testutil.FakeNotificationSender
import com.android.agrihealth.testutil.FakeNotificationTokenResolver
import com.android.agrihealth.testutil.FakeUserRepository
import com.google.firebase.messaging.RemoteMessage
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationsTest : FirebaseEmulatorsTest() {
  private val fakeToken = "1234"
  override val userRepository = FakeUserRepository()
  private val tokenResolver = FakeNotificationTokenResolver(fakeToken)
  private val sender = FakeNotificationSender(userRepository)

  val messagingService = FirebaseMessagingService(tokenResolver, sender)

  @Before
  override fun setUp() = runTest {
    userRepository.addUser(user1.copy(deviceTokensFCM = setOf(fakeToken)))
  }

  @Test
  fun getToken_succeedsWithTokenProvider() = runTest {
    messagingService.getToken { token -> assertEquals(fakeToken, token) }
  }

  @Test
  fun getToken_failsWithBadTokenProvider() = runTest {
    val badTokenResolver = FakeNotificationTokenResolver(null)
    val badMessagingService = FirebaseMessagingService(badTokenResolver)

    badMessagingService.getToken { token -> assertEquals(null, token) }
  }

  @Test
  fun uploadNotification_succeedsWithAllTypes() = runTest {
    val notificationNewReport =
        Notification.NewReport(destinationUid = user1.uid, reportTitle = "drop drop it fire")

    val notificationVetAnswer =
        Notification.VetAnswer(destinationUid = user1.uid, answer = "rope neck asap")

    messagingService.uploadNotification(notificationNewReport) { assertTrue(it) }
    messagingService.uploadNotification(notificationVetAnswer) { assertTrue(it) }
  }

  @Test
  fun uploadNotification_failsWithUnknownUser() = runTest {
    val notif =
        Notification.NewReport(
            destinationUid = user2.uid, reportTitle = "o o e o reaching high reaching higher")

    messagingService.uploadNotification(notif) { assertFalse(it) }
  }

  @Test
  fun uploadNotification_failsWhenUserHasNoTokens() = runTest {
    userRepository.addUser(user2.copy(deviceTokensFCM = setOf()))

    val notif =
        Notification.NewReport(destinationUid = user2.uid, reportTitle = "every night every day")

    messagingService.uploadNotification(notif) { assertFalse(it) }
  }

  @Test
  // Cannot use showNotification directly because it's a service and it crashes the app
  fun onMessageReceived_callsShowNotificationWithCorrectInfo() = runTest {
    val spy = spyk(FirebaseMessagingService(tokenResolver, sender))
    val slot = slot<Notification>()

    every { spy.showNotification(capture(slot)) } just Runs

    val notificationNewReport =
        Notification.NewReport(destinationUid = user1.uid, reportTitle = "makes me sick")
    val notificationVetAnswer =
        Notification.VetAnswer(destinationUid = user1.uid, answer = "when you're acting like that")

    val messageNR = dataToRemoteMessage(notificationNewReport.toDataMap())
    val messageVA = dataToRemoteMessage(notificationVetAnswer.toDataMap())

    spy.onMessageReceived(messageNR)
    val capturedNR = slot.captured
    slot.clear()
    spy.onMessageReceived(messageVA)
    val capturedVA = slot.captured

    assertEquals(notificationNewReport, capturedNR)
    assertEquals(notificationVetAnswer, capturedVA)

    clearMocks(spy)
  }

  @Test
  fun onMessageReceived_handlesBadMessageProperly() = runTest {
    val spy = spyk(FirebaseMessagingService(tokenResolver, sender))

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
