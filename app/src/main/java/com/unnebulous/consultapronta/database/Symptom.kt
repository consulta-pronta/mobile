package com.unnebulous.consultapronta.database

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

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

	}
}