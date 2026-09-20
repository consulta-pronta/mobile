package com.unnebulous.consultapronta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.unnebulous.consultapronta.databinding.FragmentAdicionarMedicamentoBinding

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