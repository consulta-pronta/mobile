package com.unnebulous.consultapronta

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.unnebulous.consultapronta.database.Professional
import com.unnebulous.consultapronta.databinding.BottomSheetBinding
import com.unnebulous.consultapronta.databinding.FragmentPermissoesMedicasBinding
import com.unnebulous.consultapronta.recyclerview.adapter.MedicalPermissionsAdapter
import com.unnebulous.consultapronta.views.SelectOptionItemView
import kotlin.sequences.forEach

class PermissoesMedicas : Fragment() {

	private var _binding: FragmentPermissoesMedicasBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentPermissoesMedicasBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		updateHeader {
			changeHeaderType(Utils.HeaderType.TITLED, true)
			setScreenTitle(getString(R.string.medical_permissions_title))
			setGoBackButtonOnClickListener {
				popBackStack()
			}
		}
		resetNavbarEntryActive()

		val permissionsGranted = mutableMapOf(
			"read_records" to false,
			"prescribe_medications" to false,
			"request_exam" to false,
			"request_appointments" to false
		)

		val adapter = MedicalPermissionsAdapter().apply {
			onPermissionChangeClick = {
				configBottomSheet { dialogBinding, dialog ->
					dialogBinding.icon.setImageResource(R.drawable.ic_shield_switch)
					dialogBinding.title.visibility = View.GONE

					dialogBinding.body.apply {
						val permissionsLabel = TextView(requireContext()).apply {
							text = getString(R.string.permissions_label)
							textSize = 16f
							setTypeface(null, android.graphics.Typeface.BOLD)
							setTextColor(ContextCompat.getColor(requireContext(), R.color.textDark))
							layoutParams = LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.WRAP_CONTENT,
								LinearLayout.LayoutParams.WRAP_CONTENT
							)
						}
						addView(permissionsLabel)

						val permissions = mapOf(
							"prescribe_medications" to getString(R.string.permission_prescribe_medications),
							"request_exam" to getString(R.string.permission_request_exams),
							"request_appointments" to getString(R.string.permission_request_consultations)
						)

						for ((key, title) in permissions) {
							val option = SelectOptionItemView(
								requireContext(),
								type = Utils.SelectOptionItemType.CHECKBOX
							).apply {
								itemId = key
								setTitle(title)
							}
							addView(option)
						}
					}

					dialogBinding.positiveButton.apply {
						text = getString(R.string.confirm_changes)
						setOnClickListener {
							for ((optionId, isChecked) in catchOptionsSelected(dialogBinding)) {
								permissionsGranted[optionId] = isChecked
								Log.i("HellYeahPronto", "$optionId : $isChecked")
							}

							dialog.dismiss()
						}
					}
				}
			}
		}

		binding.recyclerview.adapter = adapter

		adapter.submitList(
			listOf(
				Professional(
					"I love ConsultaPronta",
					"Robert",
					"1234",
					"AC",
					"email@email.email",
					"(00) 00000-0000",
					"Marista"
				)
			)
		)
	}

	private fun catchOptionsSelected(dialog: BottomSheetBinding): Map<String, Boolean> {
		val values = mutableMapOf<String, Boolean>()

		dialog.body.children.forEach { view ->
			if (view is SelectOptionItemView) {
				values[view.itemId] = view.getChecked()
			}
		}

		return values
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}