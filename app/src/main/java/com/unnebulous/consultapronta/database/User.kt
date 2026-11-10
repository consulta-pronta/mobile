package com.unnebulous.consultapronta.database

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.unnebulous.consultapronta.Utils
import com.unnebulous.consultapronta.getEnum

data class User(
	override val id: String = "",
	val email: String = "",
	val name: String = "",
	val phone: String = "",
	val cpf: String = "",
	val address: String = "",
	val user_type: Utils.UserType? = Utils.UserType.PACIENTE,
	val created_at: Timestamp? = null,
): BaseDocument {
	companion object {
		const val COLLECTION_NAME = "users"

		fun fromDocument(doc: DocumentSnapshot): User? {
			if (!doc.exists()) { return null }

			return User(
				id = doc.id,
				email = doc["email"].toString(),
				name = doc["name"].toString(),
				phone = doc["phone"].toString(),
				cpf = doc["cpf"].toString(),
				address = doc["address"].toString(),
				user_type = doc.getEnum<Utils.UserType>("user_type"),
				created_at = doc.getTimestamp("created_at"),
			)
		}

		data class FormData(
			var email: String,
			var name: String,
			var phone: String,
			var cpf: String,
			var address: String,
			var user_type: Utils.UserType,
			var created_at: Timestamp,
		) {
			fun toMap() = mapOf(
				"email" to email,
				"name" to name,
				"phone" to phone,
				"cpf" to cpf,
				"address" to address,
				"user_type" to user_type.toString().lowercase(),
				"created_at" to created_at
			)
		}
	}
}
