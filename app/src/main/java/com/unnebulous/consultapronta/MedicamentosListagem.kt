package com.unnebulous.consultapronta

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.unnebulous.consultapronta.database.Medication
import com.unnebulous.consultapronta.databinding.FragmentMedicamentosListagemBinding
import com.unnebulous.consultapronta.recyclerview.adapter.MedicationAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MedicamentosListagem : Fragment() {

	private var _binding: FragmentMedicamentosListagemBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentMedicamentosListagemBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		updateHeader {
			changeHeaderType(Utils.HeaderType.TITLED, true)
			setScreenTitle(getString(R.string.medications_screen_title))
			setGoBackButtonOnClickListener {
				popBackStack()
			}
		}

		val adapter = MedicationAdapter()
		binding.recyclerview.adapter = adapter

		lifecycleScope.launch {
			try {
				val docs = Medication.collection.get().await()

				adapter.submitList(docs.map { Medication.fromDocument(it) })
				Log.e(Medication.COLLECTION_NAME, "getMedications:success")
			} catch (e: Exception) {
				Log.e(Medication.COLLECTION_NAME, "getMedications:failure", e)
			}
		}

		binding.addNewMedication.setOnClickListener {
			changeFragmentWithBackStack(AdicionarMedicamento())
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}