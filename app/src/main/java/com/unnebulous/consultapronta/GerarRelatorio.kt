
package com.unnebulous.consultapronta

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Timestamp
import com.unnebulous.consultapronta.database.AuthManager
import com.unnebulous.consultapronta.database.DatabaseManager
import com.unnebulous.consultapronta.database.MedicalPermission
import com.unnebulous.consultapronta.database.Report
import com.unnebulous.consultapronta.database.Symptom
import com.unnebulous.consultapronta.database.User
import com.unnebulous.consultapronta.databinding.BottomSheetBinding
import com.unnebulous.consultapronta.databinding.FragmentGerarRelatorioBinding
import com.unnebulous.consultapronta.views.SelectOptionItemView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GerarRelatorio : Fragment() {

	private var _binding: FragmentGerarRelatorioBinding? = null
	private val binding get() = _binding!!

	private val dateFormatter: DateTimeFormatter by lazy {
		DateTimeFormatter.ofPattern(getString(R.string.DATE_FORMAT))
	}

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

		_binding = FragmentGerarRelatorioBinding.inflate(
			inflater,
			container,
			false
		)

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		updateHeader {
			changeHeaderType(Utils.HeaderType.TITLED, true)

			setScreenTitle(
				getString(R.string.gen_reports_screen_title)
			)

			setGoBackButtonOnClickListener {
				popBackStack()
			}
		}

		resetNavbarEntryActive()

		binding.selectDateStart.setOnClickListener {

			Utils.showDatePicker(
				this,
				defaultDate = periodStartDate
					.toLocalDateTime()
					.toLocalDate()
			) { date ->

				val buttonText = if (LocalDate.now().isEqual(date)) {
					getString(R.string.today)
				} else {
					date.format(dateFormatter)
				}

				binding.selectDateStart.text = buttonText

				periodStartDate = date
					.toLocalDateTime()
					.toFirestoreTimestamp()

				updateSummary()
			}
		}

		binding.selectDateEnd.setOnClickListener {

			Utils.showDatePicker(
				this,
				defaultDate = periodEndDate
					.toLocalDateTime()
					.toLocalDate()
			) { date ->

				val buttonText = if (LocalDate.now().isEqual(date)) {
					getString(R.string.today)
				} else {
					date.format(dateFormatter)
				}

				binding.selectDateEnd.text = buttonText

				periodEndDate = date
					.toLocalDateTime()
					.toFirestoreTimestamp()

				updateSummary()
			}
		}

		binding.professionalsSelect.setOnClickListener {

			configBottomSheet { dialogBinding, dialog ->

				dialogBinding.icon.setImageResource(
					R.drawable.ic_shield_switch
				)

				dialogBinding.title.text = getString(
					R.string.bottom_sheet_view_permission_title
				)

				dialogBinding.positiveButton.text = getString(
					R.string.save
				)

				loadProfessionals(dialogBinding)

				dialogBinding.positiveButton.setOnClickListener {

					professionalList = catchOptionsSelected(
						dialogBinding
					)

					dialog.dismiss()

					showSnackbar(
						if (professionalList.isEmpty()) {
							getString(R.string.no_selected_professional)
						} else {
							getString(R.string.selected_professionals, professionalList.size)
						},
						Utils.SnackBarType.SUCCESS
					)
				}
			}
		}

		binding.viewSymptomsIncluded.setOnClickListener {

			configBottomSheet { dialogBinding, _ ->

				dialogBinding.icon.setImageResource(
					R.drawable.ic_history
				)

				dialogBinding.title.text = getString(
					R.string.symptoms_included
				)

				dialogBinding.positiveButton.visibility = View.INVISIBLE

				dialogBinding.negativeButton.text = getString(
					R.string.close
				)

				dialogBinding.body.removeAllViews()

				for (symptom in originalSymptomList) {

					val item = SelectOptionItemView(
						requireContext(),
						type = Utils.SelectOptionItemType.COMPLETE
					).apply {

						title = symptom.title

						setSubtitle(
							symptom.created_at?.toBrazilianLocale(requireContext()) ?: ""
						)

						setIcon(R.drawable.ic_document)

						val checkbox = CheckBox(requireContext()).apply {

							buttonTintList = ContextCompat.getColorStateList(
								context,
								R.color.light_checkbox_color
							)

							isChecked = !removedSymptomsList.contains(
								symptom.id
							)

							setOnCheckedChangeListener { _, isChecked ->

								if (isChecked) {
									removedSymptomsList.remove(symptom.id)
								} else {
									if (!removedSymptomsList.contains(symptom.id)) {
										removedSymptomsList.add(symptom.id)
									}
								}

								updateSummary()
							}
						}

						addAside(checkbox)
					}

					dialogBinding.body.addView(item)
				}
			}
		}

		binding.generateReportButton.setOnClickListener {

			if (binding.reportTitleInput.text.isBlank()) {

				showSnackbar(
					getString(R.string.error_without_title),
					Utils.SnackBarType.WARNING
				)

				return@setOnClickListener

			} else if (
				binding.selectDateStart.text == getString(R.string.select___)
			) {

				showSnackbar(
					getString(R.string.error_report_without_date),
					Utils.SnackBarType.WARNING
				)

				return@setOnClickListener
			}

			val reportData = Report.FormData(
				binding.reportTitleInput.text.toString(),
				professionalList,
				removedSymptomsList,
				periodStartDate,
				periodEndDate
			).toMap()

			viewLifecycleOwner.lifecycleScope.launch {

				try {

					Report.collection.add(reportData).await()

					Log.i(
						"report",
						getString(R.string.createreport_success)
					)

					popBackStack()

				} catch (e: CancellationException) {
					throw e

				} catch (e: Exception) {

					Log.e(
						"report",
						getString(R.string.createreport_failure),
						e
					)

					if (_binding != null) {
						showSnackbar(
							getString(R.string.not_possible_to_generate_report),
							Utils.SnackBarType.WARNING
						)
					}
				}
			}
		}

		updateSummary()
	}

	private fun catchOptionsSelected(
		dialog: BottomSheetBinding
	): ArrayList<String> {

		val values = ArrayList<String>()

		dialog.body.children.forEach { view ->

			val option = view as? SelectOptionItemView

			if (option?.checked == true) {
				values.add(option.itemId)
			}
		}

		return values
	}

	private fun loadProfessionals(
		dialogBinding: BottomSheetBinding
	) {

		viewLifecycleOwner.lifecycleScope.launch {

			try {

				val patientId = AuthManager.uid
					?: throw IllegalStateException(
						getString(R.string.non_authenticated_patient)
					)

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
						getString(R.string.dont_have_authorized_professionals),
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

						if (doc.id !in permissions) {
							return@mapNotNull null
						}

						User(
							id = doc.id,
							name = doc.getString("name").orEmpty(),
							email = doc.getString("email").orEmpty(),
							user_type = Utils.UserType.PROFISSIONAL
						)
					}
					.sortedBy {
						it.name.lowercase()
					}

				if (professionals.isEmpty()) {

					showSnackbar(
						getString(R.string.no_professional_found),
						Utils.SnackBarType.INFO
					)

					return@launch
				}

				dialogBinding.body.removeAllViews()

				professionals.forEach { professional ->

					val option = SelectOptionItemView(
						requireContext(),
						type = Utils.SelectOptionItemType.CHECKBOX
					).apply {

						title = if (professional.email.isBlank()) {
							professional.name.ifBlank {
								context.getString(R.string.nameless_professional)
							}
						} else {
							"${professional.name} (${professional.email})"
						}

						itemId = professional.id

						checked = professional.id in professionalList
					}

					dialogBinding.body.addView(option)
				}

			} catch (e: CancellationException) {
				throw e

			} catch (e: Exception) {

				Log.e(
					"MedicalPermission",
					getString(R.string.loadprofessionals_failure),
					e
				)

				if (_binding != null) {
					showSnackbar(
						getString(R.string.failed_to_load_professionals),
						Utils.SnackBarType.WARNING
					)
				}
			}
		}
	}

	private fun updateSummary() {

		viewLifecycleOwner.lifecycleScope.launch {

			try {

				originalSymptomList = Symptom.getBetweenDates(
					periodStartDate,
					periodEndDate
				)

				symptomList = originalSymptomList.filter {
					!removedSymptomsList.contains(it.id)
				}

				val average = symptomList
					.getIntensityAverage()

				binding.intensityIcon.imageTintList = ColorStateList.valueOf(
					Utils.intensityToColor(
						requireContext(),
						average
					)
				)

				binding.numberSymptomsRegisters.text =
					symptomList.size.toString()

				binding.intensityAverage.text =
					getString(
						R.string.intensity_report,
						average
					).replace('.', ',')

				binding.mostAffectedArea.text =
					symptomList.getMostAffectArea()
						?: getString(R.string.none_registered)

			} catch (e: CancellationException) {
				throw e

			} catch (e: Exception) {

				Log.e(
					"report",
					getString(R.string.getsymptomsbydate_failure),
					e
				)
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
