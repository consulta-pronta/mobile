package com.unnebulous.consultapronta.data

import com.google.firebase.Timestamp

data class User(
	val email: String = "",
	val name: String = "",
	val phone: String = "",
	val cpf: String = "",
	val createdAt: Timestamp? = null,
)
