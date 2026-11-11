package com.unnebulous.consultapronta.database

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.unnebulous.consultapronta.Utils
import com.unnebulous.consultapronta.getEnum

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