package com.unnebulous.consultapronta.views

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.transition.TransitionManager
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.databinding.ViewDualToggleSelectorBinding

class DualToggleSelectorView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

	private val binding: ViewDualToggleSelectorBinding

	var optionSelected = 0
	private var enumConstants: Array<out Enum<*>>? = null

	var option: Enum<*>
		get() = enumConstants?.get(optionSelected) ?: throw IllegalStateException()
		set(value) {
			enumConstants = value.javaClass.enumConstants
			optionSelected = enumConstants?.indexOf(value) ?: 0
		}

	init {
		binding = ViewDualToggleSelectorBinding.inflate(
			LayoutInflater.from(context),
			this,
			true
		)

		attrs?.let {
			applyAttributes(it)
		}

		post {
			var spacing =
				(binding.selectorToggle.width / 2) - ((binding.firstText.width + binding.firstText.paddingStart) / 2)
			var params = binding.firstText.layoutParams as MarginLayoutParams
			params.marginStart = spacing
			binding.firstText.layoutParams = params

			spacing =
				(binding.selectorToggle.width / 2) - ((binding.secondText.width + binding.secondText.paddingStart) / 2)
			params = binding.secondText.layoutParams as MarginLayoutParams
			params.marginEnd = spacing
			binding.secondText.layoutParams = params
		}
	}

	private fun updateTextColors() {
		val accentColor = ContextCompat.getColor(context, R.color.accent)
		val primaryDarkColor = ContextCompat.getColor(context, R.color.primaryDark)
		val animationDuration = 300L

		if (optionSelected == 0) {
			ObjectAnimator.ofObject(
				binding.firstText,
				"textColor",
				ArgbEvaluator(),
				accentColor,
				primaryDarkColor
			).apply {
				duration = animationDuration
				start()
			}

			ObjectAnimator.ofObject(
				binding.secondText,
				"textColor",
				ArgbEvaluator(),
				primaryDarkColor,
				accentColor
			).apply {
				duration = animationDuration
				start()
			}


		} else {
			ObjectAnimator.ofObject(
				binding.firstText,
				"textColor",
				ArgbEvaluator(),
				primaryDarkColor,
				accentColor
			).apply {
				duration = animationDuration
				start()
			}

			ObjectAnimator.ofObject(
				binding.secondText,
				"textColor",
				ArgbEvaluator(),
				accentColor,
				primaryDarkColor
			).apply {
				duration = animationDuration
				start()
			}
		}

	}

	fun <T : Enum<T>> switch(): T {
		optionSelected = if (optionSelected == 1) 0 else 1

		val params = binding.selectorToggle.layoutParams as LayoutParams

		params.horizontalBias = 1f * optionSelected

		TransitionManager.beginDelayedTransition(this)
		updateTextColors()
		binding.selectorToggle.layoutParams = params

		val isFirstOptiontSelected = optionSelected == 0

		binding.firstText.isClickable = !isFirstOptiontSelected
		binding.firstText.isFocusable = !isFirstOptiontSelected

		binding.secondText.isClickable = isFirstOptiontSelected
		binding.secondText.isFocusable = isFirstOptiontSelected

		@Suppress("UNCHECKED_CAST")
		return option as T
	}

	fun setFirstOptionText(text: String) {
		binding.firstText.text = text
	}

	fun setSecondOptionText(text: String) {
		binding.secondText.text = text
	}

	fun applyAttributes(attrs: AttributeSet) {
		context.withStyledAttributes(attrs, R.styleable.DualToggleSelectorView) {
			val firstOptionText = getString(R.styleable.DualToggleSelectorView_firstOptionText) ?: ContextCompat.getString(context, R.string.not_available)
			val secondOptionText = getString(R.styleable.DualToggleSelectorView_secondOptionText) ?: ContextCompat.getString(context, R.string.not_available)

			setFirstOptionText(firstOptionText)
			setSecondOptionText(secondOptionText)
		}
	}

	override fun setOnClickListener(l: OnClickListener?) {
		binding.firstText.setOnClickListener(l)
		binding.secondText.setOnClickListener(l)
	}
}
