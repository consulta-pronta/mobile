package com.unnebulous.consultapronta.database

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

data class Symptom(
	val id: String = "",
	val title: String = "",
	val description: String = "",
	val date_time: Timestamp? = null,
	val place: String = "",
	val intensity: Int = 0,
	val created_at: Timestamp? = null
)

data class SymptomData(
	val title: String = "",
	val description: String = "",
	val date_time: Timestamp? = null,
	val place: String = "",
	val intensity: Int = 0,
	val created_at: FieldValue? = null
)