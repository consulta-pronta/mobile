package com.unnebulous.consultapronta.database

import com.google.firebase.firestore.FirebaseFirestore

object DatabaseManager {

	val uid: String
		get() = requireNotNull(AuthManager.uid) {
			"Não existe um usuário autenticado no Firebase."
		}

	val db get() = FirebaseFirestore.getInstance()

	val userCollection
		get() = db.collection(User.COLLECTION_NAME)

	val userDocument
		get() = userCollection.document(uid)

	fun userCollection(
		collection: String,
		userId: String = uid
	) = db.collection(User.COLLECTION_NAME)
		.document(userId)
		.collection(collection)

	fun userDocument(
		collection: String,
		id: String,
		userId: String = uid
	) = userCollection(collection, userId).document(id)
}