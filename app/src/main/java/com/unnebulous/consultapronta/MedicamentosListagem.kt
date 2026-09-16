package com.unnebulous.consultapronta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.unnebulous.consultapronta.databinding.FragmentMedicamentosListagemBinding
import com.unnebulous.consultapronta.recyclerview.adapter.MedicationAdapter

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
		}

		val adapter = MedicationAdapter()

		binding.recyclerview.adapter = adapter

		binding.addNewMedication.setOnClickListener {

		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}