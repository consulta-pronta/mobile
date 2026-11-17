package com.unnebulous.consultapronta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
					"LOGICA" to "AQUI"
				}

				onDeleteClick = { (_, value) ->
					lifecycleScope.launch {
						DatabaseManager
							.userDocument
							.update("patient_data.allergies", FieldValue.arrayRemove(value))
					}
				}
			}

			familyHistoryList.apply {
				onAddClick = {
					"LOGICA" to "AQUI"
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
					val familyHistory = familyHistoryData as Map<String, String>

					familyHistory.forEach { (familyRelative, problem) ->
						familyHistoryList.addItem(familyRelative to problem)
					}
				}
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}