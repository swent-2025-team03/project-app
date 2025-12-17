package com.android.agrihealth.data.model.device.notifications

/** Stores notification information to be displayed on other's devices */
sealed interface Notification {
  val destinationUid: String
  val type: NotificationType
  val description: String

  /** Notification to send to a vet when a new report came in */
  data class NewReport(override val destinationUid: String, override val description: String) :
      Notification {
    override val type = NotificationType.NEW_REPORT
  }

  /** Notification to send to a farmer when a vet answers their report */
  data class VetAnswer(override val destinationUid: String, override val description: String) :
      Notification {
    override val type = NotificationType.VET_ANSWER
  }

  /** Notification to send to a vet when another vet joins their office */
  data class JoinOffice(override val destinationUid: String, override val description: String) :
      Notification {
    override val type = NotificationType.JOIN_OFFICE
  }

  /** Notification to send to a vet when a farmer connects to their office */
  data class ConnectOffice(override val destinationUid: String, override val description: String) :
      Notification {
    override val type = NotificationType.CONNECT_OFFICE
  }
}

/** Enum with all the notification types */
enum class NotificationType {
  NEW_REPORT,
  VET_ANSWER,
  JOIN_OFFICE,
  CONNECT_OFFICE;

  /** Converts a NotificationType into its string representation. To use when sending to Firebase */
  fun toName(): String = name.lowercase()

  companion object {
    /** Tries to convert a type name into a NotificationType. To use when receiving from Firebase */
    fun fromName(name: String): NotificationType? =
        entries.firstOrNull { it.toName() == name.lowercase() }
  }
}
