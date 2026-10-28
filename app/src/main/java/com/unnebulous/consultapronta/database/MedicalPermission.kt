package com.unnebulous.consultapronta.database

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.unnebulous.consultapronta.Utils
import com.unnebulous.consultapronta.getEnum

data class MedicalPermission(
	override val id: String = "",
	val status: Utils.MedicalAccessStatus? = Utils.MedicalAccessStatus.REVOKED,
	val permissions: Map<String, Boolean>? = emptyMap(),
	val professional_id: String? = "",
	val created_at: Timestamp? = null,
	val granted_at: Timestamp? = null,
	val revoked_at: Timestamp? = null
): BaseDocument {

	fun toFormData() = FormData(
		status = status,
		permissions = permissions,
		professional_id = professional_id,
		created_at = created_at
	)
	companion object {
		const val COLLECTION_NAME = "medicalPermissions"
		val collection get() = DatabaseManager.userCollection(COLLECTION_NAME)

		data class FormData(
			val status: Utils.MedicalAccessStatus?,
			val permissions: Map<String, Boolean>?,
			val professional_id: String?,
			val created_at: Timestamp?,
			var granted_at: Any? = null,
			var revoked_at: Any? = null
		) {
			fun toMap(): Map<String, Any?> = mapOf(
				"status" to status.toString().lowercase(),
				"permissions" to permissions,
				"professional_id" to professional_id,
				"created_at" to created_at,
				"granted_at" to granted_at,
				"revoked_at" to revoked_at
			)
		}

		@Suppress("UNCHECKED_CAST")
		fun fromDocument(doc: DocumentSnapshot) = MedicalPermission(
			id = doc.id,
			status = doc.getEnum<Utils.MedicalAccessStatus>("status"),
			permissions = (doc["permissions"] as Map<String, Boolean>),
			professional_id = doc["professional_id"].toString(),
			created_at = doc.getTimestamp("created_at"),
			granted_at = doc.getTimestamp("granted_at"),
			revoked_at = doc.getTimestamp("revoked_at")
		)
	}
}
