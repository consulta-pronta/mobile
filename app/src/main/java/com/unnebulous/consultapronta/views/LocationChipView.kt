package com.unnebulous.consultapronta.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.unnebulous.consultapronta.databinding.LocationChipViewBinding

class LocationChipView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

	private val binding: LocationChipViewBinding

	init {
		binding = LocationChipViewBinding.inflate(
			LayoutInflater.from(context),
			this,
			true
		)
	}

	fun setLocation(location: String) {
		if (!location[0].isUpperCase()) location[0].uppercaseChar()
		binding.locationText.text = location
	}
}