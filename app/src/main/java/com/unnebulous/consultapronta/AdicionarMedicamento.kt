package com.unnebulous.consultapronta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.unnebulous.consultapronta.databinding.FragmentAdicionarMedicamentoBinding
import com.unnebulous.consultapronta.databinding.FragmentMedicamentosListagemBinding
import com.unnebulous.consultapronta.recyclerview.adapter.MedicationAdapter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AdicionarMedicamento : Fragment() {

	private var _binding: FragmentAdicionarMedicamentoBinding? = null
	private val binding get() = _binding!!

	private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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

		val typeConsumptions = arrayOf(
			"Oral (comprimido)",
			"Oral (gotas)",
			"Sublingual (comprimido)",
			"Sublingual (gotas)",
			"Injetável",
			"Externo (Pomadas, cremes, etc)",
			"Inalatório",
			"Retal",
			"Oftálmico (colírios)",
			"Oftológico (orelha)",
		)

		val adapter = ArrayAdapter(
			requireContext(),
			R.layout.item_spinner,
			typeConsumptions
		)

		adapter.setDropDownViewResource(R.layout.item_spinner)

		binding.typeConsumptionSelect.setAdapter(adapter)

		binding.typeConsumptionSelect.setOnClickListener {
			binding.typeConsumptionSelect.showDropDown()
		}

		binding.typeConsumptionDropdown.setEndIconOnClickListener {
			binding.typeConsumptionSelect.showDropDown()
		}

		binding.usageTimeInput.setOnClickListener {
			Utils.showTimePicker(this, LocalTime.of(0, 0)) { time ->
				if (time.hour == 0) {
					return@showTimePicker
				}

				val buttonText = time.format(timeFormatter)

				binding.usageTimeInput.text = buttonText
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}