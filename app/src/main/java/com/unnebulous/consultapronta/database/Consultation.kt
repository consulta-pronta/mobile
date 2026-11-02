package com.unnebulous.consultapronta.database

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import kotlin.String

data class Consultation(
	override val id: String = "",
	val professional_id: String = "",
	val patient_id: String = "",
	val motivation: String = "",
	val details: String? = "",
	val date_time: Timestamp? = null,
	val hospital: String
): BaseDocument {

	fun toFormData() = FormData(
		professional_id = professional_id,
		patient_id = patient_id,
		motivation = motivation,
		details = details,
		date_time = date_time,
		hospital = hospital
	)

	companion object {
		const val COLLECTION_NAME = "consultation"
		val collection get() = DatabaseManager.userCollection(COLLECTION_NAME)

		fun fromDocument(doc: DocumentSnapshot) = Consultation(
			id = doc.id,
			professional_id = doc["professional_id"].toString(),
			patient_id = doc["patient_id"].toString(),
			motivation = doc["motivation"].toString(),
			details = doc["details"].toString(),
			date_time = doc.getTimestamp("date_time"),
			hospital = doc["hospital"].toString()
		)

		data class FormData(
			val professional_id: String,
			val patient_id: String,
			val motivation: String,
			val details: String?,
			val date_time: Timestamp?,
			val hospital: String
		) {
			fun toMap(): Map<String, Any?> = mapOf(
				"professional_id" to professional_id,
				"patient_id" to patient_id,
				"motivation" to motivation,
				"details" to details,
				"date_time" to date_time,
				"hospital" to hospital
			)
		}
	}
}