package com.unnebulous.consultapronta.database

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

object DatabaseController {
	// Não precisa de getter, mas assim não mostra nenhum aviso desnecessário
	val db: FirebaseFirestore
		get() = FirebaseFirestore.getInstance()

	val uid: String
		get() = Firebase.auth.currentUser!!.uid

	fun userCollection(collection: String) =
		db.collection("users").document(uid).collection(collection)

	fun userDocument(collection: String, id: String) =
		userCollection(collection).document(id)
}