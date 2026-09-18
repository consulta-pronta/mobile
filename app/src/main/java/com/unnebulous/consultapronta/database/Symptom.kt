package com.unnebulous.consultapronta.database

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.unnebulous.consultapronta.toFirestoreTimestamp
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime

data class Symptom(
	val id: String = "",
	val title: String = "",
	val description: String = "",
	val date_time: Timestamp? = null,
	val place: String = "",
	val intensity: Int = 0,
	val created_at: Timestamp? = null
) {
	companion object {
		const val COLLECTION_NAME = "symptom"
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

		suspend fun getBetweenDates(start: LocalDateTime, end: LocalDateTime) =
			getBetweenDates(start.toFirestoreTimestamp(), end.toFirestoreTimestamp())

		suspend fun getBetweenDates(start: Timestamp, end: Timestamp): List<Symptom> {
			val queryResult = collection
				.whereGreaterThanOrEqualTo("created_at", start)
				.whereLessThanOrEqualTo("created_at", end)
				.get()
				.await()

			return queryResult.documents.map { fromDocument(it) }
		}
	}
}