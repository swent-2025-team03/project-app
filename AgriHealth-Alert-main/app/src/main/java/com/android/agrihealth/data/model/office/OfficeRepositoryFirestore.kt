package com.android.agrihealth.data.model.office

import com.android.agrihealth.data.model.location.locationFromMap
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

private const val OFFICES_COLLECTION_PATH = "offices"

class OfficeRepositoryFirestore(private val db: FirebaseFirestore = Firebase.firestore) :
    OfficeRepository {

  override fun getNewUid(): String {
    return db.collection(OFFICES_COLLECTION_PATH).document().id
  }

  override suspend fun addOffice(office: Office) {
    db.collection(OFFICES_COLLECTION_PATH).document(office.id).set(mapFromOffice(office)).await()
  }

  override suspend fun updateOffice(office: Office) {
    db.collection(OFFICES_COLLECTION_PATH).document(office.id).update(mapFromOffice(office)).await()
  }

  override suspend fun deleteOffice(id: String) {
    db.collection(OFFICES_COLLECTION_PATH).document(id).delete().await()
  }

  override suspend fun getOffice(id: String): Result<Office> {
    return try {
      val snapshot = db.collection(OFFICES_COLLECTION_PATH).document(id).get().await()

      if (!snapshot.exists()) {
        return Result.failure(NullPointerException("Office not found"))
      }

      val data = snapshot.data ?: return Result.failure(Exception("Office missing data"))
      Result.success(officeFromData(data))
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  private fun mapFromOffice(office: Office): Map<String, Any?> {
    return mapOf(
        "id" to office.id,
        "name" to office.name,
        "address" to office.address,
        "description" to office.description,
        "vets" to office.vets,
        "ownerId" to office.ownerId)
  }

  private fun officeFromData(data: Map<String, Any>): Office {
    val addressData = data["address"] as? Map<*, *>
    val address = locationFromMap(addressData)
    return Office(
        id = data["id"] as String,
        name = data["name"] as String,
        address = address,
        description = data["description"] as? String,
        vets = data["vets"] as? List<String> ?: emptyList(),
        ownerId = data["ownerId"] as String)
  }
}
