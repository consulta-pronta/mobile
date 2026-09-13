package com.unnebulous.consultapronta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.unnebulous.consultapronta.databinding.FragmentEditSymptomBinding
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class EditSymptom : Fragment() {

	private var _binding: FragmentEditSymptomBinding? = null
	private val binding get() = _binding!!
	private val dateFormatter by lazy { DateTimeFormatter.ofPattern(getString(R.string.DATE_FORMAT)) }
	private val timeFormatter by lazy { DateTimeFormatter.ofPattern(getString(R.string.time_format)) }

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
				val buttonText = if (LocalTime.now().equals(time)) {
					getString(R.string.time_format)
				} else {
					time.format(timeFormatter)
				}

				binding.timeSelect.text = buttonText
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}