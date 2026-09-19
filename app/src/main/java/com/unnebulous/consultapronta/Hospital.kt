package com.unnebulous.consultapronta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.unnebulous.consultapronta.databinding.FragmentHospitalBinding
import com.unnebulous.consultapronta.recyclerview.adapter.HospitalAdapter

class Hospital : Fragment() {

	private var _binding: FragmentHospitalBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentHospitalBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		updateHeader {
			changeHeaderType(Utils.HeaderType.COMPACT)
		}

		val adapter = HospitalAdapter().apply {
			onClick = { hospital ->

			}
		}

		binding.hospitalCardList.adapter = adapter
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}