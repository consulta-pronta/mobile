package com.unnebulous.consultapronta

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.unnebulous.consultapronta.databinding.FragmentHospitalBinding
import com.unnebulous.consultapronta.recyclerview.adapter.HospitalAdapter
import de.afarber.openmapview.LatLng

class Hospital : Fragment() {

	private var _binding: FragmentHospitalBinding? = null
	private val binding get() = _binding!!

	private var isMapView = false

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

		binding.recyclerview.adapter = adapter

		binding.mapView.setCenter(LatLng(-20.198054, -40.216428))
		binding.mapView.setZoom(20f)

		binding.buttonMapView.setOnClickListener {
			isMapView = !isMapView

			var buttonDrawable: Drawable?

			if (isMapView) {
				binding.mapView.visibility = View.VISIBLE
				binding.recyclerview.visibility = View.INVISIBLE
				binding.buttonMapView.text = getString(R.string.list_view)
				buttonDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_list)
			} else {
				binding.mapView.visibility = View.GONE
				binding.recyclerview.visibility = View.VISIBLE
				binding.buttonMapView.text = getString(R.string.map_view)
				buttonDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_map)
			}

			buttonDrawable?.setTint(ContextCompat.getColor(requireContext(), R.color.textLight))
			binding.buttonMapView.setCompoundDrawablesRelativeWithIntrinsicBounds(buttonDrawable, null, null, null)
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}