package com.unnebulous.consultapronta.views

import android.R.attr.textStyle
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Icon
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.databinding.FilterViewBinding

class FilterView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {

	private val binding: FilterViewBinding

	init {
		binding = FilterViewBinding.inflate(
			LayoutInflater.from(context),
			this,
			true
		)
	}

	fun setTitle(title: String) {
		binding.title.text = title
	}

	fun setIcon(icon: Icon) {
		binding.icon.setImageIcon(icon)
	}

	fun addFilter(filterText: String) {
		val newFilter = MaterialButton(context).apply {
			text = filterText
			backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary))
			minHeight = 0
			isCheckable = true

			setTextColor(ContextCompat.getColor(context, R.color.textLight))
			setTypeface(null, Typeface.BOLD)
		}

		binding.filterList.addView(newFilter)
	}
}