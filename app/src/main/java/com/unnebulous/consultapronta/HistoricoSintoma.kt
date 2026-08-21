package com.unnebulous.consultapronta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.unnebulous.consultapronta.databinding.FragmentHistoricoSintomaBinding

class HistoricoSintoma : Fragment() {
	private var _binding: FragmentHistoricoSintomaBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentHistoricoSintomaBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.header.setScreenTitle(getString(R.string.screentitle_symptom_history))
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}