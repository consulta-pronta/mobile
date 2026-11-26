package com.unnebulous.consultapronta

import com.google.firebase.Timestamp

data class UserTemp(
	val email: String,
	val name: String,
	val password: String,
	val phone: String,
	val cpf: String,
	val address: String,
	val biological_sex: String,
	val gender: String,
	val user_type: Utils.UserType? = Utils.UserType.PACIENTE,
	val birth_date: String,
	val contact_forms: MutableMap<String, String> = mutableMapOf(),
	val user_data: Map<String, Any>
)