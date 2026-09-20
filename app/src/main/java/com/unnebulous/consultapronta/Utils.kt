package com.unnebulous.consultapronta

import android.text.Editable
import android.text.TextWatcher
import android.content.Context
import android.text.format.DateFormat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.unnebulous.consultapronta.Utils.MedicationRoute.INALATORIO
import com.unnebulous.consultapronta.Utils.MedicationRoute.INJETAVEL
import com.unnebulous.consultapronta.Utils.MedicationRoute.OFTALMICO
import com.unnebulous.consultapronta.Utils.MedicationRoute.OFTOLOGICO
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

object Utils {
	enum class UserType { PATIENT, PROFESSIONAL }

	enum class NavbarButton(val id: String) {
		FIRST("first_element"),
		SECOND("second_element"),
		MAIN("main_button"),
		FOURTH("fourth_element"),
		FIFTH("fifth_element"),
	}

	enum class HeaderType { COMPACT, TITLED }

	enum class SelectOptionItemType { RADIO, CHECKBOX, COMPLETE }

	enum class SnackBarType { INFO, SUCCESS, WARNING, DANGER }

	/* TODO: Mudar para inglês */
	enum class ExamCategory { LABORATORIAL, IMAGEM, FUNCIONAL, PREVENTIVO }

	enum class ExamStatus { SOLICITADO, TRIAGEM, LIBERADO, PENDENTE }

	enum class MedicationRoute {
		ORAL, SUBLINGUAL, INJETAVEL, EXTERNO, INALATORIO, RETAL, OFTALMICO, OFTOLOGICO;

		val display get() = when (this) {
			INJETAVEL -> "Injetável"
			INALATORIO -> "Inalatório"
			OFTALMICO -> "Oftálmico"
			OFTOLOGICO -> "Oftológico"
			else -> this.toString().capitalizeFix()
		}

		companion object {
			fun fromDisplay(value: String) =
				entries.firstOrNull { it.display == value }
		}
	}

	enum class MedicationDoseUnit {
		MILIGRAMA, GRAMA, MICROGRAMA, MILILITRO, UNIDADE_INTERNACIONAL, TABLETE;

		val display get() = when (this) {
			UNIDADE_INTERNACIONAL -> "Un. Internacional"
			else -> this.toString().capitalizeFix()
		}

		companion object {
			fun fromDisplay(value: String) =
				MedicationRoute.entries.firstOrNull { it.display == value }
		}
	}

	enum class MedicationFrequencyUnit {
		MINUTO, HORA, DIA, SEMANA, MES;

		val display get() = when (this) {
			MES -> "Mês"
			else -> this.toString().capitalizeFix()
		}

		companion object {
			fun fromDisplay(value: String) =
				MedicationRoute.entries.firstOrNull { it.display == value }
		}
	}


	fun showDatePicker(fragment: Fragment, onDateSelected: (LocalDate) -> Unit) {
		val constraintBuilder = CalendarConstraints.Builder()
			.setValidator(DateValidatorPointBackward.now())

		val datePicker = MaterialDatePicker.Builder.datePicker()
			.setTitleText(fragment.getString(R.string.date_picker_title))
			.setSelection(MaterialDatePicker.todayInUtcMilliseconds())
			.setCalendarConstraints(constraintBuilder.build())
			.build()

		datePicker.addOnPositiveButtonClickListener { selection ->
			val date = Instant.ofEpochMilli(selection)
				.atZone(ZoneOffset.UTC)
				.toLocalDate()

			onDateSelected(date)
		}

		datePicker.show(fragment.parentFragmentManager, "date_picker")
	}

	fun showTimePicker(fragment: Fragment, defaultTime: LocalTime = LocalTime.now(), onTimeSelected: (LocalTime) -> Unit) {
		val clockFormat = if (DateFormat.is24HourFormat(fragment.requireContext())) {
			TimeFormat.CLOCK_24H
		} else {
			TimeFormat.CLOCK_12H
		}

		val timePicker = MaterialTimePicker.Builder()
			.setTitleText(fragment.getString(R.string.time_picker_title))
			.setTimeFormat(clockFormat)
			.setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
			.setHour(defaultTime.hour)
			.setMinute(defaultTime.minute)
			.build()

		timePicker.addOnPositiveButtonClickListener {
			val time = LocalTime.of(timePicker.hour, timePicker.minute)
			onTimeSelected(time)
		}

		timePicker.show(fragment.parentFragmentManager, "time_picker")
	}

	fun buildCpfMask(): MaskWatcher = MaskWatcher("###.###.###-##")
	fun buildPhoneMask(): MaskWatcher = MaskWatcher("(##) #####-####")

	class MaskWatcher(private val mask: String) : TextWatcher {
		private var isUpdating: Boolean = false
		private var old = ""

		override fun afterTextChanged(s: Editable?) {}

		override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

		override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
			val str = unmask(s.toString())
			var mascara = ""

			if (isUpdating) {
				old = str
				isUpdating = false
				return
			}

			var i = 0
			for (m in mask.toCharArray()) {
				if (m != '#' && str.length > old.length) {
					mascara += m
					continue
				}
				try {
					mascara += str[i]
				} catch (e: Exception) {
					break
				}
				i++
			}

			isUpdating = true
			(s as? Editable)?.replace(0, s.length, mascara)
		}

		private fun unmask(s: String): String {
			return s.replace("[^0-9]*".toRegex(), "")
		}
	}

	fun intensityToColor(context: Context, intensity: Double) =
		ContextCompat.getColor(context, when {
			intensity <= 4 -> R.color.success
			intensity <= 7 -> R.color.warning
			else -> R.color.error
		})
}