package com.unnebulous.consultapronta.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.unnebulous.consultapronta.Utils
import com.unnebulous.consultapronta.databinding.SelectOptionItemViewBinding

class SelectOptionItemView @JvmOverloads constructor(
	context: Context,
	private val type: Utils.SelectOptionItemType = Utils.SelectOptionItemType.CHECKBOX,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {
	private val binding: SelectOptionItemViewBinding

	var itemId: String = ""

	init {
		binding = SelectOptionItemViewBinding.inflate(
			LayoutInflater.from(context),
			this,
			true
		)

		configType()
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

	fun addAside(view: View) {
		if (type != Utils.SelectOptionItemType.COMPLETE || binding.aside.childCount > 0) {
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
}