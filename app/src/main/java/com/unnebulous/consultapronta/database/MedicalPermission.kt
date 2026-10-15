package com.unnebulous.consultapronta.database

import com.google.firebase.Timestamp
import com.unnebulous.consultapronta.Utils

data class MedicalPermission(
	override val id: String = "",
	val status: Utils.MedicalAccessStatus? = Utils.MedicalAccessStatus.REVOKED,
	val permissions: Map<String, Boolean>,
	val created_at: Timestamp? = null,
	val granted_at: Timestamp? = null,
	val revoked_at: Timestamp? = null
): BaseDocument {

	fun toFormData() = FormData(
		status = status!!,
		permissions = permissions,
		created_at = created_at,
		granted_at = granted_at,
		revoked_at = revoked_at
	)
	companion object {
		const val COLLECTION_NAME = "medical_permission"
		val collection get() = DatabaseManager.userCollection(COLLECTION_NAME)

		data class FormData(
			val status: Utils.MedicalAccessStatus,
			val permissions: Map<String, Boolean>,
			val created_at: Timestamp?,
			val granted_at: Timestamp? = null,
			val revoked_at: Timestamp? = null
		) {
			fun toMap(): Map<String, Any?> = mapOf(
				"status" to status.toString().lowercase(),
				"permissions" to permissions,
				"created_at" to created_at,
				"granted_at" to granted_at,
				"revoked_at" to revoked_at
			)
		}
	}
}
