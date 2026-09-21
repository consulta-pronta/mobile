package com.unnebulous.consultapronta.database

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.unnebulous.consultapronta.Utils
import com.unnebulous.consultapronta.getEnum

data class Medication(
	override val id: String = "",
	val name: String = "",

	val route: Utils.MedicationRoute? = null,
	val dose_value: Double = 0.0,
	val dose_unit: Utils.MedicationDoseUnit? = null,
	val frequency_value: Double = 0.0,
	val frequency_unit: Utils.MedicationFrequencyUnit? = null,
	val duration_days: Int = 0,

	val custom_instructions: String = "",
	val notes: String = "",

	// criar data de comeco
	val taken_dates: Map<String, Boolean> = emptyMap(),
	val registered_in: Timestamp? = null,
): BaseDocument {
	val dose get() = dose_value to dose_unit
	val frequency get() = frequency_value to frequency_unit

	fun wasTakenOn(date: String) = taken_dates[date]?: false

	companion object {
		const val COLLECTION_NAME = "medications"
		val collection get() = DatabaseManager.userCollection(COLLECTION_NAME)

		@Suppress("UNCHECKED_CAST")
		fun fromDocument(doc: DocumentSnapshot) = Medication(
			id = doc.id,
			name = doc["name"].toString(),

			route = doc.getEnum<Utils.MedicationRoute>("route"),
			dose_value = doc["dose_value"].toString().toDouble(),
			dose_unit = doc.getEnum<Utils.MedicationDoseUnit>("dose_unit"),
			frequency_value = doc["frequency_value"].toString().toDouble(),
			frequency_unit = doc.getEnum<Utils.MedicationFrequencyUnit>("frequency_unit"),
			duration_days = doc["duration_days"].toString().toInt(),

			custom_instructions = doc["custom_instructions"].toString(),
			notes = doc["notes"].toString(),

			taken_dates = (doc["taken_dates"] as? Map<String, Boolean>)?: emptyMap(),
			registered_in = doc.getTimestamp("registered_in"),
		)

		data class FormData(
			val name: String,

			val route: Utils.MedicationRoute,
			val dose: Pair<Double, Utils.MedicationDoseUnit>,
			val frequency: Pair<Double, Utils.MedicationFrequencyUnit>,
			val duration_days: Int,

			val custom_instructions: String,
			val notes: String,
		) {
			fun toMap() = mapOf(
				"name" to name,

				"route" to route.toString().lowercase(),
				"dose_value" to dose.first,
				"dose_unit" to dose.second.toString().lowercase(),
				"frequency_value" to frequency.first,
				"frequency_unit" to frequency.second.toString().lowercase(),
				"duration_days" to duration_days,

				"custom_instructions" to custom_instructions,
				"notes" to notes,
				"registered_in" to FieldValue.serverTimestamp(),
			)
		}
	}
}