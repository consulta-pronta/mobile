package com.unnebulous.consultapronta

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.children
import com.unnebulous.consultapronta.databinding.BottomSheetBinding
import com.unnebulous.consultapronta.databinding.FragmentSolicitarConsultaBinding
import com.unnebulous.consultapronta.views.SelectOptionItemView
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.sequences.forEach

class SolicitarConsulta : Fragment() {

	private var _binding: FragmentSolicitarConsultaBinding? = null
	private val binding get() = _binding!!

	private val dateFormatter by lazy { DateTimeFormatter.ofPattern(getString(R.string.DATE_FORMAT)) }
	private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentSolicitarConsultaBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		updateHeader {
			changeHeaderType(Utils.HeaderType.TITLED, true)
			setScreenTitle(getString(R.string.request_consultation))
			setGoBackButtonOnClickListener {
				popBackStack()
			}
		}

		// val motivations = resources.getStringArray(R.array.motivations)
		val motivations = arrayOf(
			"Rotina",
			"Sintomas",
			"Prevenção",
			"Entrega de exames",
			"Complicações ou condições crônicas"
		)

		val adapter = ArrayAdapter(
			requireContext(),
			R.layout.item_spinner,
			motivations
		)

		adapter.setDropDownViewResource(R.layout.item_spinner)

		binding.motivationSelect.setAdapter(adapter)

		binding.motivationSelect.setOnClickListener {
			binding.motivationSelect.showDropDown()
		}

		binding.motivationDropdown.setEndIconOnClickListener {
			binding.motivationSelect.showDropDown()
		}

		binding.consultationTimeInput.setOnClickListener {
			Utils.showTimePicker(this) { time ->
				val buttonText = time.format(timeFormatter)

				binding.consultationTimeInput.text = buttonText
			}
		}

		binding.consultationDateInput.setOnClickListener {
			Utils.showDatePicker(this, false) { date ->
				val buttonText = if (LocalDate.now().isEqual(date)) {
					getString(R.string.today)
				} else {
					date.format(dateFormatter)
				}

				binding.consultationDateInput.text = buttonText
			}
		}

		binding.professionalSelect.setOnClickListener {
			configBottomSheet { dialogBinding, dialog ->
				dialogBinding.icon.visibility = View.GONE
				dialogBinding.title.text = getString(R.string.select_professional)

				dialogBinding.body.apply {
					val _examples = mapOf(
						"1" to "Dra. Cláudia Leite",
						"2" to "Dr. Cláudio Leitoso",
						"Yotsuba" to "!"
					)

					for (professional in _examples) {
						val option = SelectOptionItemView(requireContext(), type = Utils.SelectOptionItemType.CHECKBOX)
						option.setTitle(professional.value)
						option.itemId = professional.key
						addView(option)
					}
				}

				dialogBinding.positiveButton.text = getString(R.string.save)

				dialogBinding.positiveButton.setOnClickListener {
					for (itemSelected in catchOptionsSelected(dialogBinding)) {
						Log.i("InfoPronto", itemSelected)
					}

					dialog.dismiss()
				}
			}
		}
	}

	private fun catchOptionsSelected(dialog: BottomSheetBinding): ArrayList<String> {
		val values = ArrayList<String>()

		dialog.body.children.forEach { view ->
			val option = view as SelectOptionItemView
			if (option.getChecked()) {
				values.add(option.itemId)
			}
		}

		return values
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}