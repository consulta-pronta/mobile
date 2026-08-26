package com.unnebulous.consultapronta.views

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.transition.TransitionManager
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.Utils
import com.unnebulous.consultapronta.databinding.UserTypeSelectorViewBinding

class UserTypeSelectorView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

	private val binding: UserTypeSelectorViewBinding

	private var userType = Utils.UserType.PATIENT

	init {
		binding = UserTypeSelectorViewBinding.inflate(
			LayoutInflater.from(context),
			this,
			true
		)

		post {
			var spacing =
				(binding.selectorToggle.width / 2) - ((binding.patientText.width + binding.patientText.paddingStart) / 2)
			var params = binding.patientText.layoutParams as MarginLayoutParams
			params.marginStart = spacing
			binding.patientText.layoutParams = params

			spacing =
				(binding.selectorToggle.width / 2) - ((binding.professionalText.width + binding.professionalText.paddingStart) / 2)
			params = binding.professionalText.layoutParams as MarginLayoutParams
			params.marginEnd = spacing
			binding.professionalText.layoutParams = params
		}
	}

	private fun updateTextColors() {
		val accentColor = ContextCompat.getColor(context, R.color.accent)
		val primaryDarkColor = ContextCompat.getColor(context, R.color.primaryDark)
		val animationDuration = 300L

		if (userType == Utils.UserType.PATIENT) {
			ObjectAnimator.ofObject(
				binding.patientText,
				"textColor",
				ArgbEvaluator(),
				accentColor,
				primaryDarkColor
			).apply {
				setDuration(animationDuration)
				start()
			}

			ObjectAnimator.ofObject(
				binding.professionalText,
				"textColor",
				ArgbEvaluator(),
				primaryDarkColor,
				accentColor
			).apply {
				setDuration(animationDuration)
				start()
			}


		} else {
			ObjectAnimator.ofObject(
				binding.patientText,
				"textColor",
				ArgbEvaluator(),
				primaryDarkColor,
				accentColor
			).apply {
				setDuration(animationDuration)
				start()
			}

			ObjectAnimator.ofObject(
				binding.professionalText,
				"textColor",
				ArgbEvaluator(),
				accentColor,
				primaryDarkColor
			).apply {
				setDuration(animationDuration)
				start()
			}
		}

	}

	fun changeUser(): Utils.UserType {
		userType = if (userType == Utils.UserType.PROFESSIONAL) Utils.UserType.PATIENT else Utils.UserType.PROFESSIONAL

		val params = binding.selectorToggle.layoutParams as LayoutParams

		params.horizontalBias = 1f * userType.ordinal

		TransitionManager.beginDelayedTransition(this)
		updateTextColors()
		binding.selectorToggle.layoutParams = params

		val isPatientSelected = userType == Utils.UserType.PATIENT

		binding.patientText.isClickable = !isPatientSelected
		binding.patientText.isFocusable = !isPatientSelected

		binding.professionalText.isClickable = isPatientSelected
		binding.professionalText.isFocusable = isPatientSelected

		return userType
	}

	override fun setOnClickListener(l: OnClickListener?) {
		binding.patientText.setOnClickListener(l)
		binding.professionalText.setOnClickListener(l)
	}
}