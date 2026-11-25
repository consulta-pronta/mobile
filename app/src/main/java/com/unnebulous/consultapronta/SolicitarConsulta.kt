
package com.unnebulous.consultapronta

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.unnebulous.consultapronta.database.AuthManager
import com.unnebulous.consultapronta.database.Consultation
import com.unnebulous.consultapronta.database.DatabaseManager
import com.unnebulous.consultapronta.database.MedicalPermission
import com.unnebulous.consultapronta.database.User
import com.unnebulous.consultapronta.databinding.BottomSheetBinding
import com.unnebulous.consultapronta.databinding.FragmentSolicitarConsultaBinding
import com.unnebulous.consultapronta.views.SelectOptionItemView
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SolicitarConsulta : Fragment() {

	private var _binding: FragmentSolicitarConsultaBinding? = null
	private val binding get() = _binding!!

	private val dateFormatter by lazy {
		DateTimeFormatter.ofPattern(getString(R.string.DATE_FORMAT))
	}

	private val timeFormatter = DateTimeFormatter.ofPattern(getString(R.string.time_format))

	private var professionalSelected: String? = null

	private var consultationDate: LocalDate? = null
	private var consultationTime: LocalTime? = null

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

		val motivations =  resources.getStringArray(R.array.motivations)

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
			Utils.showTimePicker(
				this,
				consultationTime ?: LocalTime.now()
			) { time ->
				consultationTime = time
				binding.consultationTimeInput.text =
					time.format(timeFormatter)
			}
		}

		binding.consultationDateInput.setOnClickListener {
			Utils.showDatePicker(
				this,
				false,
				consultationDate ?: LocalDate.now()
			) { date ->
				consultationDate = date

				val buttonText = if (LocalDate.now().isEqual(date)) {
					getString(R.string.today)
				} else {
					date.format(dateFormatter)
				}

				binding.consultationDateInput.text = buttonText
			}
		}

		binding.professionalSelect.setOnClickListener {
			binding.professionalSelect.isEnabled = false

			viewLifecycleOwner.lifecycleScope.launch {
				try {
					val professionals = getProfessionals()

					configBottomSheet { dialogBinding, dialog ->

						dialogBinding.icon.visibility = View.GONE
						dialogBinding.title.text =
							getString(R.string.select_professional)

						dialogBinding.body.apply {
							for (professional in professionals) {
								val option = SelectOptionItemView(
									requireContext(),
									type = Utils.SelectOptionItemType.RADIO
								)

								option.title = professional.value
								option.itemId = professional.key

								if (option.itemId == professionalSelected) {
									option.checked = true
								}

								option.onRadioChangeListener = { itemId ->
									handleRadioChange(dialogBinding, itemId)
								}

								addView(option)
							}
						}

						dialogBinding.positiveButton.text =
							getString(R.string.save)

						dialogBinding.positiveButton.setOnClickListener {
							val optionSelected =
								catchProfessionalSelected(dialogBinding)

							if (optionSelected.first != null) {
								professionalSelected = optionSelected.first

								binding.professionalSelect.setTextColor(
									ContextCompat.getColor(
										requireContext(),
										R.color.textDark
									)
								)

								binding.professionalSelect.text =
									optionSelected.second

								dialog.dismiss()
							}
						}
					}

				} catch (e: Exception) {
					Log.e(
						"CONSULTATION_DEBUG",
						getString(R.string.failed_to_load_professionals),
						e
					)

					showSnackbar(
						getString(R.string.load_professionals_wasnt_possible),
						Utils.SnackBarType.WARNING
					)

				} finally {
					if (_binding != null) {
						binding.professionalSelect.isEnabled = true
					}
				}
			}
		}

		binding.requestConsultationButton.setOnClickListener {

			val motivation = binding.motivationSelect.text.toString()
			val details = binding.symptomDetailsInput.text.toString()
			val hospital = binding.hospital.text.toString()

			if (
				professionalSelected == null ||
				motivation.isBlank() ||
				consultationDate == null ||
				consultationTime == null ||
				hospital.isBlank()
			) {
				showSnackbar(
					getString(R.string.error_blank_input),
					Utils.SnackBarType.WARNING
				)
				return@setOnClickListener
			}

			val patientId = AuthManager.uid

			if (patientId == null) {
				showSnackbar(
					getString(R.string.not_authenticated_reload),
					Utils.SnackBarType.WARNING
				)
				return@setOnClickListener
			}

			val consultationDoc = Consultation.Companion.FormData(
				professional_id = professionalSelected!!,
				patient_id = patientId,
				motivation = motivation,
				details = details,
				date_time = LocalDateTime.of(
					consultationDate,
					consultationTime
				).toFirestoreTimestamp(),
				hospital = hospital,
				status = Consultation.Companion.ConsultationStatus.SOLICITADO_PACIENTE
			)

			viewLifecycleOwner.lifecycleScope.launch {
				try {
					DatabaseManager
						.userCollection(
							Consultation.COLLECTION_NAME,
							patientId
						)
						.add(consultationDoc.toMap())
						.await()

					Log.d(
						Consultation.COLLECTION_NAME,
						getString(R.string.requestconsultation_success)
					)

					popBackStack()

				} catch (e: Exception) {
					Log.e(
						Consultation.COLLECTION_NAME,
						getString(R.string.requestconsultation_failure),
						e
					)

					showSnackbar(
						getString(R.string.failed_to_make_appointment),
						Utils.SnackBarType.WARNING
					)
				}
			}
		}
	}

	private fun catchProfessionalSelected(
		dialog: BottomSheetBinding
	): Pair<String?, String?> {

		var professionalSelected: Pair<String?, String?> = null to null

		dialog.body.children.forEach { view ->
			val option = view as SelectOptionItemView

			if (option.checked) {
				professionalSelected = option.itemId to option.title
				return@forEach
			}
		}

		return professionalSelected
	}

	private suspend fun getProfessionals(): Map<String, String> {
		val professionals = mutableMapOf<String, String>()

		val medicalPermissions = MedicalPermission
			.collection
			.get()
			.await()

		medicalPermissions.forEach { doc ->

			val medicalAccessStatus =
				doc.getEnum<Utils.MedicalAccessStatus>("status")

			if (medicalAccessStatus == Utils.MedicalAccessStatus.ACTIVE) {

				val professionalId = doc.getString("professional_id")
					?: return@forEach

				val professional = DatabaseManager
					.db
					.collection(User.COLLECTION_NAME)
					.document(professionalId)
					.get()
					.await()

				val professionalName = professional.getString("name")

				if (professional.exists() && professionalName != null) {
					professionals[professional.id] = professionalName
				}
			}
		}

		return professionals
	}

	private fun handleRadioChange(
		dialog: BottomSheetBinding,
		itemId: String
	) {
		dialog.body.children.forEach { view ->
			val option = view as SelectOptionItemView
			option.checked = option.itemId == itemId
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
