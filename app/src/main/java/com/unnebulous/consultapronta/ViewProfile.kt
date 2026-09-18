package com.unnebulous.consultapronta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import com.unnebulous.consultapronta.databinding.FragmentViewProfileBinding
import android.graphics.Typeface
import android.view.Gravity
import android.widget.Button
import androidx.transition.TransitionManager

class ViewProfile : Fragment() {

	private var _binding: FragmentViewProfileBinding? = null
	private val binding get() = _binding!!

	private var isPersonalDataMinimized = false
	private var isAditionalDataMinimized = false

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentViewProfileBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		updateHeader {
			changeHeaderType(Utils.HeaderType.TITLED)
			setGoBackButtonOnClickListener {
				popBackStack()
			}
		}

		setAditionalDataLayoutTitle(Utils.UserType.PATIENT)

		binding.minimizePersonalDataLayoutButton.setOnClickListener {
			isPersonalDataMinimized = !isPersonalDataMinimized
			minimizeSection(binding.personalDataCard, it, isPersonalDataMinimized)
		}

		binding.minimizeAditionalDataLayoutButton.setOnClickListener {
			isAditionalDataMinimized = !isAditionalDataMinimized
			minimizeSection(binding.aditionalDataCard, it, isAditionalDataMinimized)
		}
	}

	private fun addAditionalData(key: String, value: String) {
		val row = LinearLayout(context).apply {
			layoutParams = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
			)
			orientation = LinearLayout.HORIZONTAL
		}

		val keyTextView = TextView(context).apply {
			layoutParams = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
			)
			text = key
			setTextColor(ContextCompat.getColor(context, R.color.textDark))
			setTypeface(null, Typeface.BOLD)
		}

		val valueTextView = TextView(context).apply {
			val params = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
			)
			params.marginStart = resources.getDimensionPixelSize(R.dimen.list_items_spacing)
			layoutParams = params
			
			text = value
			setTextColor(ContextCompat.getColor(context, R.color.textDark))
		}

		row.addView(keyTextView)
		row.addView(valueTextView)
		binding.aditionalDataCard.addView(row)
	}

	private fun addAditionalData(custom: () -> MutableList<View>) {
		val views = custom()

		for (view: View in views) {
			binding.aditionalDataCard.addView(view)
		}
	}

	private fun minimizeSection(section: ViewGroup, button: View, isMinimized: Boolean) {
		TransitionManager.beginDelayedTransition(binding.root as ViewGroup)

		section.visibility = if (isMinimized) View.GONE else View.VISIBLE

		button.animate()
			.rotation(if (isMinimized) 270f else 90f)
			.setDuration(250)
			.start()
	}

	private fun setAditionalDataLayoutTitle(userType: Utils.UserType) {
		val title = when (userType) {
			Utils.UserType.PATIENT -> getString(R.string.health_information)
			Utils.UserType.PROFESSIONAL -> getString(R.string.professional_data)
		}

		binding.aditionalDataTitle.text = title
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}