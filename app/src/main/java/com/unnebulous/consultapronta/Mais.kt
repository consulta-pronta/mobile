package com.unnebulous.consultapronta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.unnebulous.consultapronta.databinding.FragmentMaisBinding

class Mais : Fragment() {
	
	private var _binding: FragmentMaisBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding = FragmentMaisBinding.inflate(layoutInflater, container, false)
		return inflater.inflate(R.layout.fragment_mais, container, false)
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}