package com.unnebulous.consultapronta.views

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.databinding.IntensityViewBinding

class IntensityView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

	private val binding: IntensityViewBinding

	init {
		binding = IntensityViewBinding.inflate(
			LayoutInflater.from(context),
			this,
			true
		)
	}

	fun setIntensity(intensity: String) {
		setIntensity(intensity.toInt())
	}

	fun setIntensity(intensity: Int) {
		val color = if (intensity <= 4) {
			ContextCompat.getColor(context, R.color.success)
		} else if (intensity <= 7) {
			ContextCompat.getColor(context, R.color.warning)
		} else {
			ContextCompat.getColor(context, R.color.error)
		}

		binding.intensityIcon.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
		binding.intensityText.setTextColor(color)
		binding.intensityNumber.setTextColor(color)

		val text = "$intensity/10"
		binding.intensityNumber.text = text
	}
}