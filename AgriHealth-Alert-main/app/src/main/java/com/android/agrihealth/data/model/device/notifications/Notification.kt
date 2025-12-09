package com.android.agrihealth.data.model.device.notifications

sealed interface Notification {
  val destinationUid: String
  val type: NotificationType
  val description: String

  data class NewReport(override val destinationUid: String, override val description: String) :
      Notification {
    override val type = NotificationType.NEW_REPORT
  }

  data class VetAnswer(override val destinationUid: String, override val description: String) :
      Notification {
    override val type = NotificationType.VET_ANSWER
  }

  data class JoinOffice(override val destinationUid: String, override val description: String) :
      Notification {
    override val type = NotificationType.JOIN_OFFICE
  }

  data class ConnectOffice(override val destinationUid: String, override val description: String) :
      Notification {
    override val type = NotificationType.CONNECT_OFFICE
  }
}

enum class NotificationType {
  NEW_REPORT,
  VET_ANSWER,
  JOIN_OFFICE,
  CONNECT_OFFICE;
  // TODO: Implement NEW_ALERT notification type

  /** Converts a NotificationType into its string representation. To use when sending to Firebase */
  fun toName(): String = name.lowercase()

  companion object {
    /** Tries to convert a type name into a NotificationType. To use when receiving from Firebase */
    fun fromName(name: String): NotificationType? =
        entries.firstOrNull { it.toName() == name.lowercase() }
  }
}
