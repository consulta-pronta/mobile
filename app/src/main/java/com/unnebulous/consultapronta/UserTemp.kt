package com.unnebulous.consultapronta

data class UserTemp(
	var userType: Utils.UserType = Utils.UserType.PATIENT,
	var name: String = "",
	var cpf: String = "",
	var email: String = "",
	var phoneNumber: String = "",
	var password: String = "",
	var contactForms: HashMap<String, String> = HashMap()
)