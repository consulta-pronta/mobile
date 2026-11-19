package com.unnebulous.consultapronta.database

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.unnebulous.consultapronta.Utils
import com.unnebulous.consultapronta.getEnum
import kotlin.String
import kotlin.collections.Map

data class Notification(
	override val id: String = "",
	val title: String = "",
	val description: String = "",
	val created_at: Timestamp? = null,
	val accessed: Boolean = false,
	val notification_type: Utils.NotificationType?,
	val notification_scope: Utils.NotificationScope?,
	val data: Map<String, String> = emptyMap()

): BaseDocument{
	fun toMap(): Map<String, Any?> = mapOf(
		"title" to title,
		"description" to description,
		"accessed" to accessed,
		"notification_type" to notification_type.toString().lowercase(),
		"notification_scope" to notification_scope.toString().lowercase(),
		"created_at" to FieldValue.serverTimestamp(),
		"data" to data
	)

	companion object{
		const val COLLECTION_NAME = "notifications"
		val collection get() = DatabaseManager.userCollection(COLLECTION_NAME)

		@Suppress("UNCHECKED_CAST")
		fun fromDocument(doc: DocumentSnapshot) = Notification(
			id = doc.id,
			notification_type = doc.getEnum<Utils.NotificationType>("notification_type"),
			notification_scope = doc.getEnum<Utils.NotificationScope>("notification_scope"),
			created_at = doc.getTimestamp("created_at"),
			description = doc["description"].toString(),
			title = doc["title"].toString(),
			accessed = doc["accessed"].toString().toBoolean(),
			data = (doc["data"] as Map<String, String>),
		)
	}
}