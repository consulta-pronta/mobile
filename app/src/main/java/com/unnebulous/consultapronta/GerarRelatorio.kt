package com.unnebulous.consultapronta

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.unnebulous.consultapronta.databinding.BottomSheetBinding
import com.unnebulous.consultapronta.databinding.FragmentGerarRelatorioBinding
import com.unnebulous.consultapronta.views.SelectOptionItemView
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GerarRelatorio : Fragment() {
	private var _binding: FragmentGerarRelatorioBinding? = null
	private val binding get() = _binding!!

	// TODO: substituir após o término da branch
	// private val dateFormatter = DateTimeFormatter.ofPattern(getString(R.string.DATE_FORMAT))
	private val dateFormatter by lazy { DateTimeFormatter.ofPattern(getString(R.string.DATE_FORMAT)) }

	private var reportPeriodStartDate = LocalDate.now()
	private var reportPeriodEndDate = LocalDate.now()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentGerarRelatorioBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		updateHeader {
			changeHeaderType(Utils.HeaderType.TITLED, true)
			setScreenTitle(getString(R.string.gen_reports_screen_title))
			setGoBackButtonOnClickListener {
				popBackStack()
			}
		}

		binding.selectDateStart.setOnClickListener {
			Utils.showDatePicker(this) { date ->
				val buttonText = if (LocalDate.now().isEqual(date)) {
					getString(R.string.today)
				} else {
					date.format(dateFormatter)
				}

				binding.selectDateStart.text = buttonText
				reportPeriodStartDate = date
			}
		}

		binding.selectDateEnd.setOnClickListener {
			Utils.showDatePicker(this) { date ->
				val buttonText = if (LocalDate.now().isEqual(date)) {
					getString(R.string.today)
				} else {
					date.format(dateFormatter)
				}

				binding.selectDateEnd.text = buttonText
				reportPeriodEndDate = date
			}
		}

		binding.professionalsSelect.setOnClickListener {
			configBottomSheet { dialogBinding, dialog ->
				dialogBinding.icon.setImageResource(R.drawable.ic_shield_switch)
				dialogBinding.title.text = getString(R.string.bottom_sheet_view_permission_title)

				dialogBinding.body.apply {
					val _examples = mapOf(
						"1" to "Dra. Cláudia Leite",
						"2" to "Dr. Cláudio Leitoso",
						"Yotsuba" to "!"
					)

					for (professional in _examples) {
						val option = SelectOptionItemView(requireContext(), Utils.SelectOptionItemType.CHECKBOX)
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