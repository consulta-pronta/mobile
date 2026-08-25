package com.unnebulous.consultapronta.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.databinding.NavbarViewBinding
import androidx.core.content.withStyledAttributes
import com.unnebulous.consultapronta.Utils

class NavbarView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

	private val binding: NavbarViewBinding
	private var currentButton: Utils.NavbarButton = Utils.NavbarButton.FIRST
		set(newButton) {
			resetColors()
			setColor(newButton, R.color.accent.toInt())

			field = newButton
		}

	init {
		clipChildren = false
		clipToPadding = false

		binding = NavbarViewBinding.inflate(
			LayoutInflater.from(context),
			this,
			true
		)

		attrs?.let {
			applyAttributes(it)
		}

		currentButton = Utils.NavbarButton.FIRST
	}

	private fun applyAttributes(attrs: AttributeSet) {
		context.withStyledAttributes(attrs, R.styleable.NavbarView) {

			val isProfessional = getBoolean(R.styleable.NavbarView_professional, false)

			if (isProfessional) {
				binding.secondElementIcon.setImageResource(R.drawable.ic_group)
				binding.secondElementText.text =
					context.getString(R.string.navbar_second_element_professional_text)

				binding.mainButtonIcon.setImageResource(R.drawable.ic_graphic)

				binding.fourthElementIcon.setImageResource(R.drawable.ic_pill)
				binding.fourthElementText.text =
					context.getString(R.string.navbar_fourth_element_professional_text)
			} else {
				binding.secondElementIcon.setImageResource(R.drawable.ic_history)
				binding.secondElementText.text =
					context.getString(R.string.navbar_second_element_patient_text)

				binding.mainButtonIcon.setImageResource(R.drawable.ic_add_circle)

				binding.fourthElementIcon.setImageResource(R.drawable.ic_hospital)
				binding.fourthElementText.text =
					context.getString(R.string.navbar_fourth_element_patient_text)
			}

		}
	}

	private fun getButtonByEnum(button: Utils.NavbarButton): ViewGroup {
		return when (button) {
			Utils.NavbarButton.FIRST -> binding.firstElement
			Utils.NavbarButton.SECOND -> binding.secondElement
			Utils.NavbarButton.MAIN -> binding.mainButton
			Utils.NavbarButton.FOURTH -> binding.fourthElement
			Utils.NavbarButton.FIFTH -> binding.fifthElement
		}
	}

	private fun getButtonIconByEnum(button: Utils.NavbarButton): ImageView {
		return when (button) {
			Utils.NavbarButton.FIRST -> binding.firstElementIcon
			Utils.NavbarButton.SECOND -> binding.secondElementIcon
			Utils.NavbarButton.MAIN -> binding.mainButtonIcon
			Utils.NavbarButton.FOURTH -> binding.fourthElementIcon
			Utils.NavbarButton.FIFTH -> binding.fifthElementIcon
		}
	}

	private fun getButtonTextByEnum(button: Utils.NavbarButton): TextView? {
		return when (button) {
			Utils.NavbarButton.FIRST -> binding.firstElementText
			Utils.NavbarButton.SECOND -> binding.secondElementText
			Utils.NavbarButton.MAIN -> null
			Utils.NavbarButton.FOURTH -> binding.fourthElementText
			Utils.NavbarButton.FIFTH -> binding.fifthElementText
		}
	}

	private fun setColor(who: Utils.NavbarButton, color: Int) {
		val variable = ContextCompat.getColor(context, color)

		getButtonIconByEnum(who).imageTintList = ColorStateList.valueOf(variable)

		if (who != Utils.NavbarButton.MAIN) {
			getButtonTextByEnum(who)!!.setTextColor(variable)
		}
	}

	private fun resetColors() {
		Utils.NavbarButton.entries.forEach { button ->
			if (button !== Utils.NavbarButton.MAIN) {
				setColor(button, R.color.textLight.toInt())
			} else {
				setColor(button, R.color.textDark.toInt())
			}
		}
	}

	fun setOnClickListener(who: Utils.NavbarButton, l: OnClickListener) {
		val button = getButtonByEnum(who)
		button.setOnClickListener { view ->
			l.onClick(view)
			currentButton = who
		}
	}
}