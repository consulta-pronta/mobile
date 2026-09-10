package com.unnebulous.consultapronta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SymptomRegister : Fragment() {

	private var param1: String? = null
	private var param2: String? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		arguments?.let {
			param1 = it.getString(ARG_PARAM1)
			param2 = it.getString(ARG_PARAM2)
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {

		return inflater.inflate(
			R.layout.fragment_symptom_register,
			container,
			false
		)
	}

	override fun onViewCreated(
		view: View,
		savedInstanceState: Bundle?
	) {
		super.onViewCreated(view, savedInstanceState)

		// Spinner
		val bodyPartSpinner =
			view.findViewById<MaterialAutoCompleteTextView>(
				R.id.body_part_spinner
			)

		val bodyPartLayout =
			view.findViewById<TextInputLayout>(
				R.id.body_part_dropdown
			)

		val bodyParts = arrayOf(
			"Cabeça",
			"Pescoço",
			"Peito",
			"Costas",
			"Braço",
			"Mão",
			"Barriga",
			"Perna",
			"Pé"
		)

		val adapter = ArrayAdapter(
			requireContext(),
			android.R.layout.simple_dropdown_item_1line,
			bodyParts
		)

		bodyPartSpinner.setAdapter(adapter)

		bodyPartSpinner.setOnClickListener {
			bodyPartSpinner.showDropDown()
		}

		bodyPartLayout.setEndIconOnClickListener {
			bodyPartSpinner.showDropDown()
		}

		//Slider
		val frame = view.findViewById<FrameLayout>(R.id.frame)

		val slider =
			view.findViewById<Slider>(R.id.intensity_slider)

		val intensityValue =
			view.findViewById<TextView>(R.id.intensity_value)

		slider.addOnChangeListener { slider, value, _ ->

			intensityValue.text = value.toInt().toString()

			frame.post {

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

				intensityValue.translationX =
					thumbX -
						intensityValue.width / 2f
			}
		}

		frame.post {
			slider.value = 5f
		}
	}

	companion object {

		@JvmStatic
		fun newInstance(param1: String, param2: String) =
			SymptomRegister().apply {
				arguments = Bundle().apply {
					putString(ARG_PARAM1, param1)
					putString(ARG_PARAM2, param2)
				}
			}
	}
}