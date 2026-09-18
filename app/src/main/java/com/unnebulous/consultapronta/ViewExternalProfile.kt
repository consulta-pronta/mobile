package com.unnebulous.consultapronta

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.transition.TransitionManager
import com.unnebulous.consultapronta.databinding.FragmentViewExternalProfileBinding
import com.unnebulous.consultapronta.views.OptionItemView

class ViewExternalProfile : Fragment() {

	private var _binding: FragmentViewExternalProfileBinding? = null
	private val binding get() = _binding!!

	private var isExternalDataMinimized = false

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentViewExternalProfileBinding.inflate(inflater, container, false)
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

		configUserType(Utils.UserType.PROFESSIONAL)

		binding.minimizeExternalUserDataLayoutButton.setOnClickListener {
			minimizeExternalUserDataLayout()
		}
	}

	private fun addContactForm(drawableResId: Int, name: String, onClickListener: View.OnClickListener) {
		val option = OptionItemView(requireContext()).apply {
			setText(name)
			setIcon(drawableResId)

			setOnClickListener(onClickListener)
		}

		binding.contactInfoLayout.addView(option)
	}

	private fun addExternalData(key: String, value: String) {
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
		binding.externalUserDataCard.addView(row)
	}

	private fun addExternalData(custom: () -> MutableList<View>) {
		val views = custom()

		for (view: View in views) {
			binding.externalUserDataCard.addView(view)
		}
	}
	private fun minimizeExternalUserDataLayout() {
		isExternalDataMinimized = !isExternalDataMinimized

		TransitionManager.beginDelayedTransition(binding.root as ViewGroup)

		binding.externalUserDataLayout.visibility = if (isExternalDataMinimized) View.GONE else View.VISIBLE

		binding.minimizeExternalUserDataLayoutButton.animate()
			.rotation(if (isExternalDataMinimized) 270f else 90f)
			.setDuration(250)
			.start()
	}

	private fun configUserType(userType: Utils.UserType) {
		var title: String
		var specificScreenTitle: String

		when (userType) {
			Utils.UserType.PATIENT -> {
				title = getString(R.string.professional_data)
				specificScreenTitle = getString(R.string.appointments_text)
			}
			Utils.UserType.PROFESSIONAL -> {
				title = getString(R.string.health_information)
				specificScreenTitle = getString(R.string.reports_text)
			}
		}

		binding.externalUserDataTitle.text = title
		binding.gotoUserSpecificScreenButton.text = specificScreenTitle
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}