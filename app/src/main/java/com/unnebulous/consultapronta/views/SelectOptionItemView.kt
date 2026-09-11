package com.unnebulous.consultapronta.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.unnebulous.consultapronta.Utils
import com.unnebulous.consultapronta.databinding.SelectOptionItemViewBinding
import androidx.core.view.isNotEmpty
import com.unnebulous.consultapronta.R

class SelectOptionItemView @JvmOverloads constructor(
	context: Context,
	private var type: Utils.SelectOptionItemType = Utils.SelectOptionItemType.CHECKBOX,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {
	private val binding: SelectOptionItemViewBinding
	private var isInflating = true

	var itemId: String = ""

	init {
		binding = SelectOptionItemViewBinding.inflate(
			LayoutInflater.from(context),
			this,
			true
		)

		attrs?.let {
			applyAttributes(it)
		}

		configType()

		isInflating = false
	}

	override fun addView(child: View?) {
		if (isInflating) {
			super.addView(child)
		} else {
			addAside(child)
		}
	}

	fun setTitle(title: String) {
		when (type) {
			Utils.SelectOptionItemType.RADIO -> binding.itemRadioButton.text = title
			Utils.SelectOptionItemType.CHECKBOX -> binding.itemCheckboxButton.text = title
			Utils.SelectOptionItemType.COMPLETE -> binding.title.text = title
		}
	}

	fun setSubtitle(title: String) {
		if (type != Utils.SelectOptionItemType.COMPLETE) {
			return
		}

		binding.subtitle.text = title
	}

	fun addAside(view: View?) {
		if (type != Utils.SelectOptionItemType.COMPLETE || binding.aside.isNotEmpty()) {
			return
		}

		binding.aside.addView(view)
	}

	fun removeAside() {
		if (type != Utils.SelectOptionItemType.COMPLETE) {
			return
		}

		binding.aside.removeAllViews()
	}

	fun getChecked(getInAside: ((SelectOptionItemViewBinding) -> Boolean)? = null): Boolean {
		return when (type) {
			Utils.SelectOptionItemType.RADIO -> binding.itemRadioButton.isChecked
			Utils.SelectOptionItemType.CHECKBOX -> binding.itemCheckboxButton.isChecked
			Utils.SelectOptionItemType.COMPLETE -> getInAside?.invoke(binding) ?: false
		}
	}

	private fun configType() {
		when (type) {
			Utils.SelectOptionItemType.RADIO -> {
				binding.itemRadioButton.visibility = VISIBLE
			}

			Utils.SelectOptionItemType.CHECKBOX -> {
				binding.itemCheckboxButton.visibility = VISIBLE
			}

			Utils.SelectOptionItemType.COMPLETE -> {
				binding.itemCompleteLayout.visibility = VISIBLE
			}
		}
	}

	private fun applyAttributes(attrs: AttributeSet) {
		context.withStyledAttributes(attrs, R.styleable.SelectOptionItemView) {
			val type = getColor(R.styleable.SelectOptionItemView_selectOptionType, 0)
			val color = getColor(R.styleable.SelectOptionItemView_color, ContextCompat.getColor(context, R.color.textDark))
			val buttonColor = getColorStateList(R.styleable.SelectOptionItemView_buttonColor)

			this@SelectOptionItemView.type = when (type) {
				0 -> Utils.SelectOptionItemType.RADIO
				1 -> Utils.SelectOptionItemType.CHECKBOX
				2 -> Utils.SelectOptionItemType.COMPLETE
				else -> Utils.SelectOptionItemType.RADIO
			}

			when (this@SelectOptionItemView.type) {
				Utils.SelectOptionItemType.RADIO -> {
					binding.itemRadioButton.setTextColor(color)
					buttonColor?.let {
						binding.itemRadioButton.buttonTintList = it
					}
				}

				Utils.SelectOptionItemType.CHECKBOX -> {
					binding.itemCheckboxButton.setTextColor(color)
					buttonColor?.let {
						binding.itemCheckboxButton.buttonTintList = it
					}
				}

				Utils.SelectOptionItemType.COMPLETE -> {
					binding.apply {
						icon.setColorFilter(color)
						title.setTextColor(color)
						subtitle.setTextColor(color)
					}
				}
			}
		}
	}
}