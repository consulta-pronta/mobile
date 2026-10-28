package com.unnebulous.consultapronta

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.unnebulous.consultapronta.database.Medication
import com.unnebulous.consultapronta.databinding.FragmentAdicionarMedicamentoBinding
import com.unnebulous.consultapronta.showSnackbar
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.String

class AdicionarMedicamento : Fragment() {

	private var _binding: FragmentAdicionarMedicamentoBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentAdicionarMedicamentoBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		updateHeader {
			changeHeaderType(Utils.HeaderType.TITLED, true)
			setScreenTitle(getString(R.string.register_medications_screen_title))
			setGoBackButtonOnClickListener {
				popBackStack()
			}
		}
		resetNavbarEntryActive()

		binding.apply {
			val routes = Utils.MedicationRoute.entries.map { it.display }.toTypedArray()
			setupSelect(medicationRouteSelect, routes)

			val doseUnits = Utils.MedicationDoseUnit.entries.map { it.display }.toTypedArray()
			setupSelect(medicationDoseUnit, doseUnits, resetOnClick = true)

			val frequencyUnits = Utils.MedicationFrequencyUnit.entries
				.map { it.display }
				.toTypedArray()
			setupSelect(medicationFrequencyUnit, frequencyUnits, resetOnClick = true)

			registerMedicationButton.setOnClickListener {
				val name = binding.medicationNameInput.text.toString()

				val route = Utils.MedicationRoute.fromDisplay( // tipo de consumo
					medicationRouteSelect.text.toString()
				)

				val dose = binding.medicationDoseValue.text.toString().toDoubleOrNull()
				val doseUnit = Utils.MedicationDoseUnit.fromDisplay(
					medicationDoseUnit.text.toString()
				)

				val frequency = medicationFrequencyValue.text.toString().toDoubleOrNull()
				val frequencyUnit = Utils.MedicationFrequencyUnit.fromDisplay(
					medicationFrequencyUnit.text.toString()
				)

				val duration = medicationDuration.text.toString().toIntOrNull()
				val customInstructions = medicationCustomInstructions.text.toString()
				val notes = medicationNotes.text.toString()

				if (
					name.isBlank()          ||
					route == null           ||
					dose == null            ||
					doseUnit == null        ||
					frequency == null       ||
					frequencyUnit == null
					) {
					showSnackbar(getString(R.string.error_blank_input), Utils.SnackBarType.WARNING)
					return@setOnClickListener
				}

				lifecycleScope.launch {
					try {
						Medication.collection.add(Medication.Companion.FormData(
							name = name,
							route = route,
							dose = Pair(dose, doseUnit),
							frequency = Pair(frequency, frequencyUnit),
							duration_days = duration ?: 0,
							custom_instructions = customInstructions,
							notes = notes,
						).toMap()).await()

						Log.i(Medication.COLLECTION_NAME, "addMedication:success")
						popBackStack()
					} catch (e: Exception) {
						Log.e(Medication.COLLECTION_NAME, "addMedication:failure", e)
					}
				}
			}
		}
	}

	private fun setupSelect(
		select: MaterialAutoCompleteTextView,
		array: Array<String>,
		resetOnClick: Boolean = false
	) {
		select.apply {
			setAdapter(createAdapter(array))
			setOnClickListener {
				if (resetOnClick) { setText("", false) }
				showDropDown()
			}
		}
	}

	private fun createAdapter(array: Array<String>) =
		ArrayAdapter(requireContext(), R.layout.item_spinner, array).apply {
			setDropDownViewResource(R.layout.item_spinner)
		}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}