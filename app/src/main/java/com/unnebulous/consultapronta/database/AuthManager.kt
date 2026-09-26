package com.unnebulous.consultapronta.database

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

object AuthManager {
	val auth get() = FirebaseAuth.getInstance()

	val user get() = auth.currentUser

	val uid get() = user?.uid

	suspend fun getUserData(): User? {
		val doc = DatabaseManager.userDocument.get().await()
		return User.fromDocument(doc)
	}
}