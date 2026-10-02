package com.unnebulous.consultapronta.database

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.unnebulous.consultapronta.mergedHistoric
import kotlinx.coroutines.tasks.await

data class Symptom(
	override val id: String = "",
	val title: String = "",
	val description: String = "",
	val date_time: Timestamp? = null,
	val place: String = "",
	val intensity: Int = 0,
	val created_at: Timestamp? = null
): BaseDocument {
	val historicCollection get() = collection.document(id).collection(HISTORIC_COLLECTION_NAME)

	suspend fun getHistoric(): List<Symptom> {
		val queryResult = historicCollection.get().await()
		return queryResult.documents.mapNotNull { fromDocument(it) }
	}

	fun toFormData() = FormData(
		title = title,
		description = description,
		date_time = date_time,
		place = place,
		intensity = intensity,
	)

	companion object {
		const val COLLECTION_NAME = "symptom"
		const val HISTORIC_COLLECTION_NAME = "historic"
		val collection get() = DatabaseManager.userCollection(COLLECTION_NAME)

		fun fromDocument(doc: DocumentSnapshot) = Symptom(
			id = doc.id,
			title = doc["title"].toString(),
			description = doc["description"].toString(),
			date_time = doc.getTimestamp("date_time"),
			place = doc["place"].toString(),
			intensity = doc["intensity"].toString().toInt(),
			created_at = doc.getTimestamp("created_at"),
		)

		suspend fun getBetweenDates(
			start: Timestamp,
			end: Timestamp,
			deep: Boolean = false
		): List<Symptom> {
			val queryResult = collection
				.whereGreaterThanOrEqualTo("created_at", start)
				.whereLessThanOrEqualTo("created_at", end)
				.get()
				.await()

			var list = queryResult.documents.map { fromDocument(it) }
			if (deep) { list = list.mergedHistoric() }

			return list.sortedByDescending { it.date_time }
		}

		data class FormData(
			val title: String,
			val description: String,
			val date_time: Timestamp?,
			val place: String,
			val intensity: Int,
		) {
			fun toMap(): Map<String, Any?> = mapOf(
				"title" to title,
				"description" to description,
				"date_time" to date_time,
				"place" to place,
				"intensity" to intensity,
				"created_at" to FieldValue.serverTimestamp(),
			)
		}
	}
}