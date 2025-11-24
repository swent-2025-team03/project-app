package com.android.agrihealth.testutil

import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.data.model.office.OfficeRepository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/** In-memory fake implementation of OfficeRepository for testing purposes. */
class FakeOfficeRepository(initialOffices: List<Office> = emptyList()) : OfficeRepository {

  private val offices =
      ConcurrentHashMap<String, Office>().apply { initialOffices.forEach { put(it.id, it) } }

  override fun getNewUid(): String = UUID.randomUUID().toString()

  override suspend fun addOffice(office: Office) {
    offices[office.id] = office
  }

  override suspend fun updateOffice(office: Office) {
    if (!offices.containsKey(office.id)) throw NoSuchElementException("Office not found")
    offices[office.id] = office
  }

  override suspend fun deleteOffice(id: String) {
    if (offices.remove(id) == null) throw NoSuchElementException("Office not found")
  }

  override suspend fun getOffice(id: String): Result<Office> {
    return offices[id]?.let { Result.success(it) }
        ?: Result.failure(NoSuchElementException("Office not found"))
  }
}
