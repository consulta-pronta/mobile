package com.unnebulous.consultapronta.database

import com.google.firebase.firestore.FirebaseFirestore

object DatabaseManager {
	private val uid get() = AuthManager.uid!!

	val db get() = FirebaseFirestore.getInstance()

	val userCollection get() = db.collection("users")
	val userDocument get() = userCollection.document(uid)

	fun userCollection(collection: String) = userDocument.collection(collection)

	fun userDocument(collection: String, id: String) = userCollection(collection).document(id)
}