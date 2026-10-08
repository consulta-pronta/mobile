package com.unnebulous.consultapronta.database

import com.google.firebase.firestore.FirebaseFirestore

object DatabaseManager {
	private val uid get() = AuthManager.uid!!

	const val PATIENT_USER_DATA = "data_paciente"
	const val PROFESSIONAL_USER_DATA = "data_profissional"

	val db get() = FirebaseFirestore.getInstance()

	val userCollection get() = db.collection(User.COLLECTION_NAME)
	val userDocument get() = userCollection.document(uid)

	fun userCollection(collection: String) = userDocument.collection(collection)

	fun userDocument(collection: String, id: String) = userCollection(collection).document(id)
}