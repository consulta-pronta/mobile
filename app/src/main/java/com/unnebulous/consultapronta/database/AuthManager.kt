package com.unnebulous.consultapronta.database

import com.google.firebase.auth.FirebaseAuth

object AuthManager {
	val auth get() = FirebaseAuth.getInstance()

	val user get() = auth.currentUser

	val uid get() = user?.uid
}