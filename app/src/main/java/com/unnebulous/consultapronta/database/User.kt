package com.unnebulous.consultapronta.database

import com.google.firebase.Timestamp
import com.unnebulous.consultapronta.Utils

data class User(
	val email: String = "",
	val name: String = "",
	val phone: String = "",
	val cpf: String = "",
	val user_type: String = Utils.UserType.PATIENT.toString().lowercase(),
	val created_at: Timestamp? = null,
)
