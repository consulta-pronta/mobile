package com.unnebulous.consultapronta.database

import com.google.firebase.Timestamp

data class Symptom(
	val id: String = "",
	val title: String = "",
	val description: String = "",
	val dateTime: Timestamp? = null,
	val place: String = "",
	val intensity: Int = 0,
	val createdAt: Timestamp? = null
)