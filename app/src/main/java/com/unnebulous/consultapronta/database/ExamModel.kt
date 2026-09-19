package com.unnebulous.consultapronta.database

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.unnebulous.consultapronta.Utils
import com.unnebulous.consultapronta.getEnum

data class ExamModel(
	override val id: String = "",
	val name: String = "",
	val category: Utils.ExamCategory? = null,
	val type: String = "",
	val place: String = "",
	val date: Timestamp? = null,
	val status: Utils.ExamStatus? = null,
): BaseDocument {
	companion object {
		const val COLLECTION_NAME = "exams"
		val collection get() = DatabaseManager.userCollection(COLLECTION_NAME)

		fun fromDocument(doc: DocumentSnapshot) = ExamModel(
			id = doc.id,
			name = doc["name"].toString(),
			category = doc.getEnum<Utils.ExamCategory>("category"),
			type = doc["type"].toString(),
			place = doc["place"].toString(),
			date = doc.getTimestamp("date"),
			status = doc.getEnum<Utils.ExamStatus>("status"),
		)
	}
}
