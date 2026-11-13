package com.unnebulous.consultapronta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.unnebulous.consultapronta.database.DatabaseManager
import com.unnebulous.consultapronta.databinding.FragmentHealthInformationBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HealthInformation : Fragment() {
	private var _binding: FragmentHealthInformationBinding? = null
	private val binding get() = _binding!!

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

		lifecycleScope.launch {
			binding.apply {
				val user = DatabaseManager
					.userDocument
					.get()
					.await()

				bloodtypeInput.text = user["blood_type"].toString().toEditable()
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}