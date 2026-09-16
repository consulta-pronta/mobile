package com.unnebulous.consultapronta.database

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import kotlin.String


data class Symptom(
	val id: String = "",
	val title: String = "",
	val description: String = "",
	val date_time: Timestamp? = null,
	val place: String = "",
	val intensity: Int = 0,
	val created_at: Timestamp? = null,
) {
	companion object {
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

data class SymptomUpdate(
	val id: String = "",
	val title: String = "",
	val description: String = "",
	val date_time: Timestamp? = null,
	val place: String = "",
	val intensity: Int = 0,
	val reason_for_update: String = "",
	val created_at: Timestamp? = null,
)


data class SymptomData(
	val title: String = "",
	val description: String = "",
	val date_time: Timestamp? = null,
	val place: String = "",
	val intensity: Int = 0,
	val created_at: FieldValue? = null
)

data class SymptomUpdateData(
	val title: String = "",
	val description: String = "",
	val date_time: Timestamp? = null,
	val place: String = "",
	val intensity: Int = 0,
	val created_at: Timestamp? = null,

	var reason_for_update: String = "",
) {
	companion object {
		fun fromDocument(doc: DocumentSnapshot, reason: String) = SymptomUpdateData(
			title = doc["title"].toString(),
			description = doc["description"].toString(),
			date_time = doc.getTimestamp("date_time"),
			place = doc["place"].toString(),
			intensity = doc["intensity"].toString().toInt(),
			reason_for_update = reason,
			created_at = doc.getTimestamp("created_at"),
		)
	}
}