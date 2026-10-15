package com.unnebulous.consultapronta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.unnebulous.consultapronta.database.Professional
import com.unnebulous.consultapronta.databinding.FragmentPermissoesMedicasBinding
import com.unnebulous.consultapronta.recyclerview.adapter.MedicalPermissionsAdapter
import com.unnebulous.consultapronta.views.SelectOptionItemView

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

		val adapter = MedicalPermissionsAdapter().apply {
			onPermissionChangeClick = {
				configBottomSheet { dialogBinding, dialog ->
					dialogBinding.body.apply {
						val permissionsLabel = TextView(requireContext()).apply {
							text = getString(R.string.permissions_label)
							textSize = 16f
							setTypeface(null, android.graphics.Typeface.BOLD)
							setTextColor(ContextCompat.getColor(requireContext(), R.color.textLight))
							layoutParams = LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.WRAP_CONTENT,
								LinearLayout.LayoutParams.WRAP_CONTENT
							).apply {
								bottomMargin = 12
							}
						}
						addView(permissionsLabel)

						val permissions = listOf(
							"prescrever_medicamentos" to getString(R.string.permission_prescribe_medications),
							"solicitar_exames" to getString(R.string.permission_request_exams),
							"solicitar_consultas" to getString(R.string.permission_request_consultations)
						)

						for ((key, title) in permissions) {
							val option = SelectOptionItemView(
								requireContext(),
								type = Utils.SelectOptionItemType.CHECKBOX
							).apply {
								setTitle(title)
								itemId = key
								background = ContextCompat.getDrawable(requireContext(), R.drawable.shape_input)
								backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.surface)
								layoutParams = LinearLayout.LayoutParams(
									LinearLayout.LayoutParams.MATCH_PARENT,
									LinearLayout.LayoutParams.WRAP_CONTENT
								).apply {
									topMargin = 8
									bottomMargin = 8
								}
							}
							addView(option)
						}
					}
				}
			}
		}

		adapter.submitList(
			listOf(
				Professional(
					"I love ConsultaPronta (CP)",
					"",
					"",
					"",
					"",
					""
				)
			)
		)
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}