package com.unnebulous.consultapronta.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.toColor
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.databinding.OptionItemViewBinding

class OptionItemView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {
	private val binding: OptionItemViewBinding

	init {
		binding = OptionItemViewBinding.inflate(
			LayoutInflater.from(context),
			this,
			true
		)

		attrs?.let {
			applyAttributes(it)
		}
	}

	fun setColor(color: Int) {
		binding.apply {
			icon.setColorFilter(color)
			nameText.setTextColor(color)
			arrow.setColorFilter(color)
		}
	}

	fun setColorResource(colorResId: Int) {
		val color = ContextCompat.getColor(context, colorResId)
		setColor(color)
	}

	fun setArrowVisibilityTo(isVisible: Boolean) {
		binding.arrow.visibility = if (isVisible) VISIBLE else GONE
	}

	fun setText(text: String?) {
		binding.nameText.text = text
	}

	fun setText(textResId: Int) {
		setText(context.getString(textResId))
	}

	fun setIcon(drawableResId: Int) {
		if (drawableResId != 0) {
			binding.icon.setImageResource(drawableResId)
		}
	}

	private fun applyAttributes(attrs: AttributeSet) {
		context.withStyledAttributes(attrs, R.styleable.OptionItemView) {
			val text = getString(R.styleable.OptionItemView_text)
			val defaultColor = ContextCompat.getColor(context, R.color.primaryDark)
			val color = getColor(R.styleable.OptionItemView_color, defaultColor)

			setText(text ?: "")
			setIcon(getResourceId(R.styleable.OptionItemView_iconSrc, 0))
			setArrowVisibilityTo(getBoolean(R.styleable.OptionItemView_hasArrow, true))
			setColor(color)
		}
	}
}