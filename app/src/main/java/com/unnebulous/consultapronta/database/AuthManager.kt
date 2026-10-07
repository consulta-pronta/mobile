package com.unnebulous.consultapronta.database

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.unnebulous.consultapronta.Utils
import kotlinx.coroutines.tasks.await

object AuthManager {
	val auth get() = FirebaseAuth.getInstance()

	val user get() = auth.currentUser

	val uid get() = user?.uid

	const val PATIENT_USER_DATA = "data_paciente"
	const val PROFESSIONAL_USER_DATA = "data_profissional"

	suspend fun getUserData(): User? {
		val doc = DatabaseManager.userDocument.get().await()
		return User.fromDocument(doc)
	}

	fun getUserDataByType(userType: Utils.UserType): CollectionReference {
		val data = DatabaseManager.userCollection(
			if (userType == Utils.UserType.PACIENTE) PATIENT_USER_DATA
			else PROFESSIONAL_USER_DATA
		)
		return data
	}
}