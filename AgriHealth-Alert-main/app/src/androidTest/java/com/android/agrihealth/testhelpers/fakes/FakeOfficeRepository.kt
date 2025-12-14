package com.android.agrihealth.testhelpers.fakes

import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.data.model.office.OfficeRepository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.delay

private const val OFFICE_NOT_FOUND = "Office not found"

/** In-memory fake implementation of OfficeRepository for testing purposes. */
class FakeOfficeRepository(
    initialOffices: List<Office> = emptyList(),
    private val delayMs: Long = 0L
) : OfficeRepository {

  private val offices =
      ConcurrentHashMap<String, Office>().apply { initialOffices.forEach { put(it.id, it) } }

  override fun getNewUid(): String = UUID.randomUUID().toString()

  override suspend fun addOffice(office: Office) {
    delay(delayMs)
    offices[office.id] = office
  }

  override suspend fun updateOffice(office: Office) {
    delay(delayMs)
    if (!offices.containsKey(office.id)) throw NoSuchElementException(OFFICE_NOT_FOUND)
    offices[office.id] = office
  }

  override suspend fun deleteOffice(id: String) {
    delay(delayMs)
    offices.remove(id)
  }

  override suspend fun getOffice(id: String): Result<Office> {
    delay(delayMs)
    return offices[id]?.let { Result.success(it) }
        ?: Result.failure(NoSuchElementException(OFFICE_NOT_FOUND))
  }

  override suspend fun getVetsInOffice(officeId: String): List<String> {
    delay(delayMs)
    return offices[officeId]?.vets ?: emptyList()
  }
}
