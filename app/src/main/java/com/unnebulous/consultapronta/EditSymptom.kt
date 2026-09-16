package com.unnebulous.consultapronta

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.unnebulous.consultapronta.database.DatabaseController
import com.unnebulous.consultapronta.database.SymptomData
import com.unnebulous.consultapronta.database.SymptomUpdateData
import com.unnebulous.consultapronta.databinding.FragmentEditSymptomBinding
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class EditSymptom : Fragment() {

	private var _binding: FragmentEditSymptomBinding? = null
	private val binding get() = _binding!!
	private val dateFormatter by lazy { DateTimeFormatter.ofPattern(getString(R.string.DATE_FORMAT)) }
	private val timeFormatter by lazy { DateTimeFormatter.ofPattern(getString(R.string.time_format)) }

	private lateinit var symptomId: String
	private lateinit var localDate: LocalDate
	private lateinit var localTime: LocalTime

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		arguments?.let {
			symptomId = it.getString(ARG_SYMPTOM_ID, "ERROR")
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentEditSymptomBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(
		view: View,
		savedInstanceState: Bundle?
	) {
		super.onViewCreated(view, savedInstanceState)

		updateHeader {
			changeHeaderType(Utils.HeaderType.TITLED, true)
			setScreenTitle(getString(R.string.title_edit_page))
			setGoBackButtonOnClickListener {
				popBackStack()
			}
		}
		val bodyParts = arrayOf(
			"Cabeça",
			"Rosto",
			"Pescoço",
			"Tórax",
			"Abdômen",
			"Costas",
			"Ombro",
			"Braço",
			"Antebraço",
			"Mão",
			"Coxa",
			"Perna",
			"Tornozelo",
			"Pé",
			"Nádegas",
			"Vagina",
			"Pênis"

		)

		val adapter = ArrayAdapter(
			requireContext(),
			R.layout.item_body_part,
			bodyParts
		)

		adapter.setDropDownViewResource(R.layout.item_body_part)

		binding.bodyPartSpinner.setAdapter(adapter)

		binding.bodyPartSpinner.setOnClickListener {
			binding.bodyPartSpinner.showDropDown()
		}

		binding.bodyPartDropdown.setEndIconOnClickListener {
			binding.bodyPartSpinner.showDropDown()
		}

		binding.intensitySlider.addOnChangeListener { slider, value, _ ->

			binding.intensityValue.text = value.toInt().toString()

			binding.frame.post {

				val fraction =
					(value - slider.valueFrom) /
						(slider.valueTo - slider.valueFrom)

				val start =
					slider.thumbWidth / 2f

				val end =
					slider.width -
						slider.thumbWidth / 2f

				val thumbX =
					start +
						fraction * (end - start)

				binding.intensityValue.translationX =
					thumbX -
						binding.intensityValue.width / 2f
			}
		}

		binding.frame.post {
			binding.intensitySlider.value = 5f
		}

		binding.dateSelect.setOnClickListener {
			Utils.showDatePicker(this) { date ->
				localDate = date

				val buttonText = if (LocalDate.now().isEqual(date)) {
					getString(R.string.today)
				} else {
					date.format(dateFormatter)
				}

				binding.dateSelect.text = buttonText
			}
		}

		binding.timeSelect.setOnClickListener {
			Utils.showTimePicker(this) { time ->
				localTime = time

				val buttonText = if (LocalTime.now().equals(time)) {
					getString(R.string.time_format)
				} else {
					time.format(timeFormatter)
				}

				binding.timeSelect.text = buttonText
			}
		}

		binding.buttonSubmit.setOnClickListener {
			val oldDocRef = DatabaseController.userDocument("symptom", symptomId)
			oldDocRef
				.get()
				.addOnSuccessListener { document ->
					if (document.exists()) {
						val symptom = SymptomUpdateData.fromDocument(
							document,
							binding.questionEditArea.text.toString()
						)

						oldDocRef
							.collection("historic")
							.add(symptom)
							.addOnSuccessListener {
								Log.i("symptom", "editSymptomDocument:createHistoric:success")

								onCreateHistoric(document)

								popBackStack()
							}
							.addOnFailureListener { e ->
								Log.w("symptom", "editSymptomDocument:createHistoric:failure", e)
							}
					}
				}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	fun onCreateHistoric(document: DocumentSnapshot) {
		val symptomData = binding.run {
			SymptomData(
				title = detailSymptomArea.text.toString(),
				description = detailSymptomArea.text.toString(),
				date_time = LocalDateTime.of(localDate, localTime)
					.toFirestoreTimestamp(),
				place = bodyPartSpinner.text.toString(),
				intensity = intensitySlider.value.toInt(),
				created_at = FieldValue.serverTimestamp(),
			)
		}

		DatabaseController.userDocument("symptom", document.id)
			.set(symptomData)
			.addOnSuccessListener {
				Log.i("symptom", "editSymptomDocument:updateTopLevel:success")

				popBackStack()
			}
			.addOnFailureListener { e ->
				Log.w("symptom", "editSymptomDocument:updateTopLevel:failure", e)
			}
	}

	companion object {
		private const val ARG_SYMPTOM_ID = "symptom_id"

		@JvmStatic
		fun newInstance(id: String) =
			EditSymptom().apply {
				arguments = Bundle().apply {
					putString(ARG_SYMPTOM_ID, id)
				}
			}
	}
}