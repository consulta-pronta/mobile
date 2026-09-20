package com.unnebulous.consultapronta

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.DocumentReference
import com.unnebulous.consultapronta.database.Symptom
import com.unnebulous.consultapronta.databinding.FragmentEditSymptomBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
			val topLevelRef = Symptom.collection.document(symptomId)
			lifecycleScope.launch {
				try {
					createHistoric(topLevelRef)
					updateCurrent(topLevelRef)

					Log.i(Symptom.COLLECTION_NAME, "editSymptom:success")
					popBackStack()
				} catch (e: Exception) {
					Log.e(Symptom.COLLECTION_NAME, "editSymptom:failure", e)
				}
			}
		}
	}

	private fun createHistoric(ref: DocumentReference) {
		lifecycleScope.launch {
			try {
				val data = ref.get().await()
				val symptom = Symptom.fromDocument(data)

				symptom.historicCollection.add(symptom.toFormData()).await()
			} catch (e: Exception) {
				throw e
			}
		}
	}

	private fun updateCurrent(ref: DocumentReference) {
		lifecycleScope.launch {
			try {
				val symptomData = binding.run {
					Symptom.Companion.FormData(
						title = detailSymptomArea.text.toString(),
						description = detailSymptomArea.text.toString(),
						date_time = LocalDateTime.of(localDate, localTime)
							.toFirestoreTimestamp(),
						place = bodyPartSpinner.text.toString(),
						intensity = intensitySlider.value.toInt(),
					)
				}

				ref.set(symptomData.toMap())
			} catch (e: Exception) {
				throw e
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
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