package com.unnebulous.consultapronta

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FieldValue
import com.unnebulous.consultapronta.database.DatabaseManager
import com.unnebulous.consultapronta.databinding.FragmentHealthInformationBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HealthInformation : Fragment() {
	private var _binding: FragmentHealthInformationBinding? = null
	private val binding get() = _binding!!

	private var allergiesKeyValue: Int = 0

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentHealthInformationBinding.inflate(
			inflater,
			container,
			false
		)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		updateHeader {
			changeHeaderType(Utils.HeaderType.TITLED, true)
			setScreenTitle(getString(R.string.health_information))
			setGoBackButtonOnClickListener {
				popBackStack()
			}
		}
		resetNavbarEntryActive()

		binding.apply {
			val bloodTypes = Utils.BLOODTYPE.entries.map { it.display }.toTypedArray()
			bloodtypeInput.apply {
				setAdapter(ArrayAdapter(requireContext(), R.layout.item_spinner, bloodTypes).apply {
					setDropDownViewResource(R.layout.item_spinner)
				})
				setOnClickListener {
					showDropDown()
				}
			}

			allergiesList.apply {
				onAddClick = {
					configBottomSheet { dialogBinding, dialog ->
						dialogBinding.apply {
							icon.isVisible = false
							title.text = getString(R.string.add_new_allegy)

							body.apply {
								val input = EditText(context).apply {
									layoutParams = ViewGroup.LayoutParams(
										ViewGroup.LayoutParams.MATCH_PARENT,
										ViewGroup.LayoutParams.WRAP_CONTENT
									)
									inputType = InputType.TYPE_CLASS_TEXT
									background = ContextCompat.getDrawable(context, R.drawable.shape_input)
									setCompoundDrawablesRelativeWithIntrinsicBounds(
										ContextCompat.getDrawable(context, R.drawable.ic_allergy),
										null,
										null,
										null
									)
									compoundDrawablePadding = 20
									hint = getString(R.string.hint_allergy_input)
									setTextColor(ContextCompat.getColor(context, R.color.textLight))
									setHintTextColor(ContextCompat.getColor(context, R.color.textLight60))
									compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.textLight))
								}

								addView(input)
							}

							positiveButton.apply {
								text = getString(R.string.save)
								setOnClickListener {
									var name: String? = null

									body.forEach { view ->
										if (view is EditText) {
											name = view.text.toString()
										}
									}

									if (name == null) return@setOnClickListener

									DatabaseManager
										.userDocument
										.update("patient_data.allergies", FieldValue.arrayUnion(name))

									allergiesList.addItem("${-allergiesKeyValue++}" to name)
									dialog.dismiss()
								}
							}
						}
					}
				}

				onDeleteClick = { (_, value) ->
					DatabaseManager
						.userDocument
						.update("patient_data.allergies", FieldValue.arrayRemove(value))
				}
			}

			familyHistoryList.apply {
				onAddClick = {
					configBottomSheet { dialogBinding, dialog ->
						dialogBinding.apply {
							icon.isVisible = false
							title.text = getString(R.string.add_new_family_history)

							body.apply {
								val relativeInput = EditText(context).apply {
									layoutParams = ViewGroup.LayoutParams(
										ViewGroup.LayoutParams.MATCH_PARENT,
										ViewGroup.LayoutParams.WRAP_CONTENT
									)
									inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
									background = ContextCompat.getDrawable(context, R.drawable.shape_input)
									setCompoundDrawablesRelativeWithIntrinsicBounds(
										ContextCompat.getDrawable(context, R.drawable.ic_person),
										null,
										null,
										null
									)
									compoundDrawablePadding = 20
									hint = getString(R.string.hint_relative_input)
									setTextColor(ContextCompat.getColor(context, R.color.textLight))
									setHintTextColor(ContextCompat.getColor(context, R.color.textLight60))
									compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.textLight))
								}

								val relativeProblemInput = EditText(context).apply {
									layoutParams = ViewGroup.LayoutParams(
										ViewGroup.LayoutParams.MATCH_PARENT,
										ViewGroup.LayoutParams.WRAP_CONTENT
									)
									inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
									background = ContextCompat.getDrawable(context, R.drawable.shape_input)
									setCompoundDrawablesRelativeWithIntrinsicBounds(
										ContextCompat.getDrawable(context, R.drawable.ic_reports),
										null,
										null,
										null
									)
									compoundDrawablePadding = 20
									hint = getString(R.string.hint_relative_problem_input)
									setTextColor(ContextCompat.getColor(context, R.color.textLight))
									setHintTextColor(ContextCompat.getColor(context, R.color.textLight60))
									compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.textLight))
								}

								addView(relativeInput)
								addView(relativeProblemInput)
							}

							positiveButton.apply {
								text = getString(R.string.save)
								setOnClickListener {
									var relative: String? = null
									var relativeProblem: String? = null

									body.forEach { view ->
										if (view is EditText) {
											if (relative == null) {
												relative = view.text.toString()
											} else {
												relativeProblem = view.text.toString()
											}
										}
									}

									if (relative == null || relativeProblem == null) return@setOnClickListener

									DatabaseManager
										.userDocument
										.update("patient_data.family_history.$relative", relativeProblem)

									familyHistoryList.addItem(relative to relativeProblem)
									dialog.dismiss()
								}
							}
						}
					}
				}

				onDeleteClick = { (key, _) ->
					lifecycleScope.launch {
						DatabaseManager
							.userDocument
							.update("patient_data.family_history.$key", FieldValue.delete())
					}
				}
			}
		}

		@Suppress("UNCHECKED_CAST")
		lifecycleScope.launch {
			binding.apply {
				val user = DatabaseManager
					.userDocument
					.get()
					.await()

				bloodtypeInput.text = user["patient_data.blood_type"].toString().toEditable()
				weightInput.text = user["patient_data.weight"].toString().toEditable()
				heightInput.text = user["patient_data.height"].toString().toEditable()

				val allergiesData = user.get("patient_data.allergies")

				if (allergiesData != null) {
					val allergies = allergiesData as List<String>

					allergies.forEach { allergy ->
						allergiesList.addItem((allergiesKeyValue++).toString() to allergy)
					}
				}

				val familyHistoryData = user.get("patient_data.family_history")

				if (familyHistoryData != null) {
					familyHistoryList.setFullMap(familyHistoryData as Map<String, String>)
				}
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}