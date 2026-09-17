package com.unnebulous.consultapronta.database

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue

data class Report(
	val id: String = "",
	val title: String = "",
	val professionals: List<String> = emptyList(),
	val period_start: Timestamp? = null,
	val period_end: Timestamp? = null,
	val created_at: Timestamp? = null,
) {
	companion object {
		const val COLLECTION_NAME = "reports"
		val collection get() = DatabaseManager.userCollection(COLLECTION_NAME)

		fun fromDocument(doc: DocumentSnapshot): Report? {
			if (!doc.exists()) { return null }

			val report = doc.toObject(Report::class.java)!!
				.copy(id = doc.id)

			return report
		}

		data class FormData(
			var title: String = "",
			var professionals: List<String> = emptyList(),
			var period_start: Timestamp? = null,
			var period_end: Timestamp? = null,
		) {
			fun toMap(): Map<String, Any?> = mapOf(
				"title" to title,
				"professionals" to professionals,
				"period_start" to period_start,
				"period_end" to period_end,
				"created_at" to FieldValue.serverTimestamp(),
			)
		}
	}
}