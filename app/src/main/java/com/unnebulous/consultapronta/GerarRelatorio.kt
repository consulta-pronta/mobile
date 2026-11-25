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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.unnebulous.consultapronta.database.AuthManager
import com.unnebulous.consultapronta.database.DatabaseManager
import com.unnebulous.consultapronta.database.MedicalPermission
import com.unnebulous.consultapronta.database.User

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
					binding.professionalsSelect.setOnClickListener {
						loadProfessionals()
					}

					for (professional in professionals) {
						val option = SelectOptionItemView(
							requireContext(),
							type = Utils.SelectOptionItemType.CHECKBOX
						)
						option.title = professional.value
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
							title = symptom.title
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
			binding.generateReportButton.setOnClickListener {

				if (binding.reportTitleInput.text.isBlank()) {
					showSnackbar(
						getString(R.string.error_without_title),
						Utils.SnackBarType.WARNING
					)
					return@setOnClickListener
				}

				if (binding.selectDateStart.text == getString(R.string.select___)) {
					showSnackbar(
						getString(R.string.error_report_without_date),
						Utils.SnackBarType.WARNING
					)
					return@setOnClickListener
				}

				viewLifecycleOwner.lifecycleScope.launch {
					try {
						val patientId = AuthManager.uid
							?: throw Exception("Paciente não autenticado.")

						val permissionCollection = DatabaseManager.userCollection(
							MedicalPermission.COLLECTION_NAME,
							patientId
						)

						val activePermissions = permissionCollection
							.get()
							.await()
							.documents
							.filter {
								it.getString("status")
									?.equals("active", ignoreCase = true) == true
							}
							.mapNotNull {
								it.getString("professional_id")
							}
							.toSet()

						val validProfessionals = professionalList.filter {
							it in activePermissions
						}

						if (validProfessionals.size != professionalList.size) {
							showSnackbar(
								"Algumas permissões foram revogadas. Selecione os profissionais novamente.",
								Utils.SnackBarType.WARNING
							)

							professionalList = ArrayList(validProfessionals)
							return@launch
						}

						val reportData = Report.FormData(
							title = binding.reportTitleInput.text.toString().trim(),
							professionals = validProfessionals,
							removed_symptoms = removedSymptomsList,
							period_start = periodStartDate,
							period_end = periodEndDate
						).toMap()

						DatabaseManager.userCollection(
							Report.COLLECTION_NAME,
							patientId
						).add(reportData).await()

						Log.i("report", "createReport:success")

						showSnackbar(
							"Relatório criado com sucesso.",
							Utils.SnackBarType.SUCCESS
						)

						popBackStack()

					} catch (e: Exception) {
						Log.e("report", "createReport:failure", e)

						showSnackbar(
							"Erro ao gerar relatório.",
							Utils.SnackBarType.WARNING
						)
					}
				}
			}
			}
	}

	private fun catchOptionsSelected(dialog: BottomSheetBinding): ArrayList<String> {
		val values = ArrayList<String>()

		dialog.body.children.forEach { view ->
			val option = view as SelectOptionItemView
			if (option.checked) {
				values.add(option.itemId)
			}
		}

		return values
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun showProfessionalSelection(
		professionals: List<User>
	) {
		val names = professionals.map {
			if (it.email.isBlank()) {
				it.name.ifBlank { "Profissional sem nome" }
			} else {
				"${it.name} (${it.email})"
			}
		}.toTypedArray()

		val selected = BooleanArray(professionals.size) { index ->
			professionals[index].id in professionalList
		}

		MaterialAlertDialogBuilder(requireContext())
			.setTitle("Profissionais permitidos")
			.setMultiChoiceItems(names, selected) { _, index, checked ->
				selected[index] = checked
			}
			.setNegativeButton(getString(R.string.cancel), null)
			.setPositiveButton(getString(R.string.save)) { _, _ ->

				professionalList = ArrayList(
					professionals
						.filterIndexed { index, _ -> selected[index] }
						.map { it.id }
				)

				showSnackbar(
					if (professionalList.isEmpty()) {
						"Nenhum profissional selecionado."
					} else {
						"${professionalList.size} profissional(is) selecionado(s)."
					},
					Utils.SnackBarType.SUCCESS
				)
			}
			.show()
	}

	private fun loadProfessionals() {
		viewLifecycleOwner.lifecycleScope.launch {
			try {
				val patientId = AuthManager.uid
					?: throw Exception("Paciente não autenticado.")

				val permissionCollection = DatabaseManager.userCollection(
					MedicalPermission.COLLECTION_NAME,
					patientId
				)

				val permissions = permissionCollection
					.get()
					.await()
					.documents
					.filter {
						it.getString("status")
							?.equals("active", ignoreCase = true) == true
					}
					.mapNotNull {
						it.getString("professional_id")
					}
					.toSet()

				if (permissions.isEmpty()) {
					showSnackbar(
						"Você ainda não possui profissionais autorizados.",
						Utils.SnackBarType.INFO
					)
					return@launch
				}

				val professionals = DatabaseManager.db
					.collection(User.COLLECTION_NAME)
					.whereEqualTo("user_type", "profissional")
					.get()
					.await()
					.documents
					.mapNotNull { doc ->
						if (doc.id !in permissions) return@mapNotNull null

						User(
							id = doc.id,
							name = doc.getString("name").orEmpty(),
							email = doc.getString("email").orEmpty(),
							user_type = Utils.UserType.PROFISSIONAL
						)
					}
					.sortedBy { it.name.lowercase() }

				if (professionals.isEmpty()) {
					showSnackbar(
						"Nenhum profissional autorizado foi encontrado.",
						Utils.SnackBarType.INFO
					)
					return@launch
				}

				showProfessionalSelection(professionals)

			} catch (e: Exception) {
				Log.e(
					"MedicalPermission",
					"loadProfessionals:failure",
					e
				)

				showSnackbar(
					"Erro ao carregar profissionais.",
					Utils.SnackBarType.WARNING
				)
			}
		}
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