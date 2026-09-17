package com.unnebulous.consultapronta.database

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

data class Report(
	val title: String = "",
	val professionals: List<String> = emptyList(),
	val period_start: Timestamp? = null,
	val period_end: Timestamp? = null,
	val created_at: Timestamp? = null,
) {
	companion object {
		const val COLLECTION_NAME = "reports"
		val collection get() = DatabaseManager.userCollection(COLLECTION_NAME)

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