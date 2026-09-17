package com.unnebulous.consultapronta

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Timestamp
import com.unnebulous.consultapronta.database.Report
import com.unnebulous.consultapronta.database.Symptom
import com.unnebulous.consultapronta.databinding.BottomSheetBinding
import com.unnebulous.consultapronta.databinding.FragmentGerarRelatorioBinding
import com.unnebulous.consultapronta.views.SelectOptionItemView
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GerarRelatorio : Fragment() {
	private var _binding: FragmentGerarRelatorioBinding? = null
	private val binding get() = _binding!!

	// TODO: Teoricamente usar esse comentado se conseguir sem dar erro
	// private val dateFormatter = DateTimeFormatter.ofPattern(getString(R.string.DATE_FORMAT))
	private val dateFormatter by lazy { DateTimeFormatter.ofPattern(getString(R.string.DATE_FORMAT)) }

	private var periodStartDate = Timestamp.now()
	private var periodEndDate = Timestamp.now()
	private var professionalList = ArrayList<String>()

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
				periodStartDate = date.toLocalDateTime().toFirestoreTimestamp()

				updateSummary()
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
				periodEndDate = date.toLocalDateTime().toFirestoreTimestamp()

				updateSummary()
			}
		}

		binding.professionalsSelect.setOnClickListener {
			configBottomSheet { dialogBinding, dialog ->
				dialogBinding.icon.setImageResource(R.drawable.ic_shield_switch)
				dialogBinding.title.text = getString(R.string.bottom_sheet_view_permission_title)

				dialogBinding.body.apply {
					// TODO: Get from database when uhh thing done if ykyk 
					val professionals = HashMap<String, String>()

					for (professional in professionals) {
						val option = SelectOptionItemView(
							requireContext(),
							Utils.SelectOptionItemType.CHECKBOX
						)
						option.setTitle(professional.value)
						option.itemId = professional.key
						addView(option)
					}
				}

				dialogBinding.positiveButton.text = getString(R.string.save)

				dialogBinding.positiveButton.setOnClickListener {
					professionalList = catchOptionsSelected(dialogBinding)

					dialog.dismiss()
				}
			}
		}

		binding.generateReportButton.setOnClickListener {
			val reportData = Report.Companion.FormData(
				binding.reportTitleInput.text.toString(),
				professionalList,
				periodStartDate,
				periodEndDate,
			).toMap()

			viewLifecycleOwner.lifecycleScope.launch {
				try {
//					Report.collection.add(reportData).await()
//
//					Log.i("report", "createReport:success")
//					popBackStack()
				} catch (e: Exception) {
					Log.e("report", "createReport:failure", e)
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

	private fun updateSummary() {
		binding.apply {
			lifecycleScope.launch {
				try {
					val queryResult = Symptom.collection
						.whereGreaterThanOrEqualTo("created_at", periodStartDate)
						.whereLessThanOrEqualTo("created_at", periodEndDate)
						.get()
						.await()

					val symptoms = queryResult.documents.map { Symptom.fromDocument(it) }

					numberSymptomsRegisters.text = symptoms.size.toString()
					intensityAverage.text = symptoms.map { it.intensity }.average().let { value ->
						if (value.isNaN()) "0" else value.toString()
					}
					mostAffectedArea.text = symptoms
						.groupBy { it.place }
						.maxByOrNull { it.value.size }
						?.key ?: "Nenhuma registrada"
				} catch (e: Exception) {
					Log.e("report", "getSymptomsByDate:failure", e)
				}
			}
		}
	}
}