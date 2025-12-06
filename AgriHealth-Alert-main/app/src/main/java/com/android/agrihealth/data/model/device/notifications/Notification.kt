package com.android.agrihealth.data.model.device.notifications

sealed interface Notification {
  val destinationUid: String
  val type: NotificationType

  data class NewReport(override val destinationUid: String, val reportTitle: String) :
      Notification {
    override val type = NotificationType.NEW_REPORT
  }

  data class VetAnswer(override val destinationUid: String, val answer: String) : Notification {
    override val type = NotificationType.VET_ANSWER
  }
}

enum class NotificationType {
  NEW_REPORT,
  VET_ANSWER;

  /** Converts a NotificationType into its string representation. To use when sending to Firebase */
  fun toName(): String = name.lowercase()

  companion object {
    /** Tries to convert a type name into a NotificationType. To use when receiving from Firebase */
    fun fromName(name: String): NotificationType? =
        entries.firstOrNull { it.toName() == name.lowercase() }
  }
}
