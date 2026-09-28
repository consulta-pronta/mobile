package com.unnebulous.consultapronta

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FieldValue
import com.unnebulous.consultapronta.database.Symptom
import com.unnebulous.consultapronta.databinding.FragmentSymptomRegisterBinding
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SymptomRegister : Fragment() {

	private var _binding: FragmentSymptomRegisterBinding? = null
	private val binding get() = _binding!!
	private val dateFormatter by lazy { DateTimeFormatter.ofPattern(getString(R.string.DATE_FORMAT)) }
	private val timeFormatter by lazy { DateTimeFormatter.ofPattern(getString(R.string.time_format)) }


	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentSymptomRegisterBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(
		view: View,
		savedInstanceState: Bundle?
	) {
		super.onViewCreated(view, savedInstanceState)

		updateHeader {
			changeHeaderType(Utils.HeaderType.TITLED, true)
			setScreenTitle(getString(R.string.title_register_page))
			setGoBackButtonOnClickListener {
				popBackStack()
			}
		}
		setNavbarEntryActive(Utils.NavbarButton.MAIN)

		var localDate: LocalDate? = null
		var localTime: LocalTime? = null
		binding.symptomIntensity.text = getString(R.string.symptom_intensity, 1)

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

		binding.intensitySlider.addOnChangeListener { slider, intensityValue, _ ->
			val color = Utils.intensityToColor(requireContext(), intensityValue.toDouble())

			slider.trackActiveTintList = ColorStateList.valueOf(color)
			slider.thumbTintList = ColorStateList.valueOf(color)
			binding.symptomIntensity.text = getString(R.string.symptom_intensity, intensityValue.toInt())
		}

		binding.dateSelect.setOnClickListener {
			Utils.showDatePicker(this, localDate ?: LocalDate.now()) { date ->
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
			Utils.showTimePicker(this, localTime ?: LocalTime.now()) { time ->
				localTime = time

				val buttonText = if (LocalTime.now().equals(time)) {
					getString(R.string.time_format)
				} else {
					time.format(timeFormatter)
				}

				binding.timeSelect.text = buttonText
			}
		}

		binding.symptomRegister.setOnClickListener {
			if (localTime == null || localDate == null) {
				TODO()
			}

			val symptomDoc = binding.run {
				Symptom.Companion.FormData(
					title = questionSymptomArea.text.toString(),
					description = detailSymptomArea.text.toString(),
					date_time = LocalDateTime.of(localDate, localTime)
						.toFirestoreTimestamp(),
					place = bodyPartSpinner.text.toString(),
					intensity = intensitySlider.value.toInt(),
				)
			}

			lifecycleScope.launch {
				try {
					Symptom.collection.add(symptomDoc)

					Log.i(Symptom.COLLECTION_NAME, "addSymptom:success")
					popBackStack()
				} catch (e: Exception) {
					Log.e(Symptom.COLLECTION_NAME, "addSymptom:failure", e)
				}
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}