package com.unnebulous.consultapronta.database

import com.google.firebase.Timestamp

data class User(
	val email: String = "",
	val name: String = "",
	val phone: String = "",
	val cpf: String = "",
	val createdAt: Timestamp? = null,
)
