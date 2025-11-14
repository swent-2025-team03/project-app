package com.android.agrihealth.data.model.office

import com.android.agrihealth.data.model.location.Location
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

private const val OFFICES_COLLECTION_PATH = "offices"

class OfficeRepositoryFirestore(private val db: FirebaseFirestore = Firebase.firestore) :
    OfficeRepository {
  override suspend fun addOffice(office: Office) {
    val map = mapFromOffice(office)
    db.collection(OFFICES_COLLECTION_PATH).document(office.id).set(map).await()
  }

  override suspend fun updateOffice(office: Office) {
    val snapshot = db.collection(OFFICES_COLLECTION_PATH).document(office.id).get().await()
    if (!snapshot.exists()) throw Exception("Office does not exist")
    val map = mapFromOffice(office)
    // Prevent changing id via updates
    val updateMap = map.toMutableMap().apply { remove("id") }
    db.collection(OFFICES_COLLECTION_PATH).document(office.id).update(updateMap).await()
  }

  override suspend fun deleteOffice(id: String) {
    db.collection(OFFICES_COLLECTION_PATH).document(id).delete().await()
  }

  override suspend fun getOffice(id: String): Result<Office> {
    return try {
      val snapshot = db.collection(OFFICES_COLLECTION_PATH).document(id).get().await()
      if (!snapshot.exists()) return Result.failure(NullPointerException("No such office"))
      val data = snapshot.data ?: return Result.failure(Exception("Office has no data"))
      val office = officeFromData(id, data)
      Result.success(office)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun listOffices(): Result<List<Office>> {
    return try {
      val snapshot = db.collection(OFFICES_COLLECTION_PATH).get().await()
      val offices = snapshot.documents.map { doc -> officeFromData(doc.id, doc.data ?: emptyMap()) }
      Result.success(offices)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  private fun mapFromOffice(office: Office): Map<String, Any?> {
    return mapOf(
        "name" to office.name,
        "address" to office.address,
        "description" to office.description,
        "vets" to office.vets,
        "ownerId" to office.ownerId)
  }

  private fun officeFromData(id: String, data: Map<String, Any>): Office {
    val name = data["name"] as? String ?: throw Exception("Missing name")
    val address = data["address"] as? Location?
    val description = data["description"] as? String?
    val vets = data["vets"] as? List<String> ?: emptyList()
    val ownerId = data["ownerId"] as? String ?: throw Exception("Missing ownerId")
    return Office(
        id = id,
        name = name,
        address = address,
        description = description,
        vets = vets,
        ownerId = ownerId)
  }
}
