package com.unnebulous.consultapronta.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.transition.TransitionManager
import com.google.android.material.button.MaterialButton
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.databinding.FilterViewBinding

class FilterView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {

	lateinit var onFilterSelected: (text: String) -> Unit

	private val binding: FilterViewBinding
	private var isMinimized = false

	init {
		binding = FilterViewBinding.inflate(
			LayoutInflater.from(context),
			this,
			true
		)

		attrs?.let {
			context.withStyledAttributes(attrs, R.styleable.FilterView) {
				val isOrderBy = getBoolean(R.styleable.FilterView_isOrderByFilter, false)

				setCanOrderBy(isOrderBy)
			}
		}

		binding.filterList.addOnButtonCheckedListener { group, checkedId, isChecked ->
			if (isChecked) {
				onFilterSelected(group.findViewById<MaterialButton>(checkedId).text.toString())
			}
		}

		binding.filterHeading.setOnClickListener {
			toggleMinimize()
		}
	}

	fun addFilter(filterText: String) {
		val newFilter = MaterialButton(context).apply {
			id = generateViewId()
			text = filterText
			backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.filter_button_background_color))
			isCheckable = true

			setTextColor(ContextCompat.getColor(context, R.color.filter_button_text_color))
			setTypeface(null, Typeface.BOLD)
		}

		binding.filterList.addView(newFilter)
	}

	fun setCanOrderBy(value: Boolean) {
		if (value) {
			binding.orderByButton.visibility = VISIBLE
			binding.icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sort))
			binding.title.text = ContextCompat.getString(context, R.string.order_by)
		} else {
			binding.orderByButton.visibility = GONE
			binding.icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_filter))
			binding.title.text = ContextCompat.getString(context, R.string.filter)
		}
	}

	private fun toggleMinimize() {
		isMinimized = !isMinimized

		(parent as? ViewGroup)?.let {
			TransitionManager.beginDelayedTransition(it)
		} ?: TransitionManager.beginDelayedTransition(this)

		binding.filterScrollContainer.visibility = if (isMinimized) GONE else VISIBLE

		binding.arrowMaximized.animate()
			.rotation(if (isMinimized) 90f else 270f)
            .setDuration(250)
			.start()
	}

	fun getSelectedFilterText(): String? {
		val checkedId = binding.filterList.checkedButtonId
		if (checkedId != NO_ID) {
			// sim, findViewById. Não achei outra opção
			return binding.filterList.findViewById<MaterialButton>(checkedId).text.toString()
		}
		return null
	}
}