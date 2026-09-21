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
				val route = Utils.MedicationRoute.fromDisplay(
					medicationRouteSelect.text.toString()
				)
				val doseUnit = Utils.MedicationDoseUnit.fromDisplay(
					medicationDoseUnit.text.toString()
				)
				val frequencyUnit = Utils.MedicationFrequencyUnit.fromDisplay(
					medicationFrequencyUnit.text.toString()
				)

				if (route == null || doseUnit == null || frequencyUnit == null) {
					Toast
						.makeText(context, "Preencha os inputs corretamente", Toast.LENGTH_SHORT)
						.show()
					return@setOnClickListener
				}

				lifecycleScope.launch {
					try {
						Medication.collection.add(Medication.Companion.FormData(
							name = medicationNameInput.text.toString(),

							route = route,
							dose = Pair(
								medicationDoseValue.text.toString().toDouble(),
								doseUnit
							),
							frequency = Pair(
								medicationFrequencyValue.text.toString().toDouble(),
								frequencyUnit),
							duration_days = medicationDuration.text.toString().toInt(),

							custom_instructions = medicationCustomInstructions.text.toString(),
							notes = medicationNotes.text.toString(),
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