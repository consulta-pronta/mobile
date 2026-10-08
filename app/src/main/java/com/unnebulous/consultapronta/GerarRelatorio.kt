package com.unnebulous.consultapronta

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Timestamp
import com.unnebulous.consultapronta.database.Report
import com.unnebulous.consultapronta.database.Symptom
import com.unnebulous.consultapronta.databinding.BottomSheetBinding
import com.unnebulous.consultapronta.databinding.FragmentGerarRelatorioBinding
import com.unnebulous.consultapronta.views.OptionItemView
import com.unnebulous.consultapronta.views.SelectOptionItemView
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GerarRelatorio : Fragment() {
	private var _binding: FragmentGerarRelatorioBinding? = null
	private val binding get() = _binding!!

	private val dateFormatter: DateTimeFormatter by lazy { DateTimeFormatter.ofPattern(getString(R.string.DATE_FORMAT)) }

	private var periodStartDate = Timestamp.now()
	private var periodEndDate = Timestamp.now()
	private var professionalList = ArrayList<String>()
	private var symptomList = emptyList<Symptom>()
	private var originalSymptomList = emptyList<Symptom>()
	private var removedSymptomsList = mutableListOf<String>()

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
		resetNavbarEntryActive()

		binding.selectDateStart.setOnClickListener {
			Utils.showDatePicker(this, defaultDate = periodStartDate.toLocalDateTime().toLocalDate()) { date ->
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
			Utils.showDatePicker(this, defaultDate = periodEndDate.toLocalDateTime().toLocalDate()) { date ->
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
							type = Utils.SelectOptionItemType.CHECKBOX
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

		binding.viewSymptomsIncluded.setOnClickListener {
			configBottomSheet { dialogBinding, _ ->
				dialogBinding.icon.setImageResource(R.drawable.ic_history)
				dialogBinding.title.text = getString(R.string.symptoms_included)
				dialogBinding.positiveButton.visibility = View.INVISIBLE
				dialogBinding.negativeButton.text = getString(R.string.close)

				dialogBinding.body.apply {
					for (symptom in originalSymptomList) {
						val item = SelectOptionItemView(requireContext(), type = Utils.SelectOptionItemType.COMPLETE).apply {
							setTitle(symptom.title)
							setSubtitle(symptom.created_at?.toBrazilianLocale() ?: "")
							setIcon(R.drawable.ic_document)

							val checkbox = CheckBox(requireContext()).apply {
								buttonTintList = ContextCompat.getColorStateList(context, R.color.light_checkbox_color)
								isChecked = !removedSymptomsList.contains(symptom.id)
							}

							checkbox.setOnCheckedChangeListener { _, isChecked ->
								if (isChecked) {
									removedSymptomsList.remove(symptom.id)
								} else {
									removedSymptomsList.add(symptom.id)
								}

								updateSummary()
							}

							addAside(checkbox)
						}

						addView(item)
					}
				}

			}
		}

		binding.generateReportButton.setOnClickListener {
			val reportData = Report.Companion.FormData(
				binding.reportTitleInput.text.toString(),
				professionalList,
				removedSymptomsList,
				periodStartDate,
				periodEndDate,
			).toMap()

			lifecycleScope.launch {
				try {
					Report.collection.add(reportData).await()

					Log.i("report", "createReport:success")
					popBackStack()
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
					originalSymptomList = Symptom.getBetweenDates(periodStartDate, periodEndDate)
					symptomList = originalSymptomList.filter {
						!removedSymptomsList.contains(it.id)
					}

					val average = symptomList.getIntensityAverage()
					intensityIcon.imageTintList = ColorStateList.valueOf(Utils.intensityToColor(requireContext(), average))

					numberSymptomsRegisters.text = symptomList.size.toString()
					intensityAverage.text = getString(R.string.intensity_report, average).replace('.', ',')
					mostAffectedArea.text = symptomList.getMostAffectArea() ?: "Nenhuma registrada"
				} catch (e: Exception) {
					Log.e("report", "getSymptomsByDate:failure", e)
				}
			}
		}
	}
}