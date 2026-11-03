package com.unnebulous.consultapronta.recyclerview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.database.Hospital
import com.unnebulous.consultapronta.databinding.HospitalCardBinding
import android.content.Context
import android.location.Location
import de.afarber.openmapview.LatLng

class HospitalAdapter : ListAdapter<Hospital, HospitalAdapter.HospitalViewHolder>(HospitalComparator()){
	lateinit var onClick: (Hospital) -> Unit
	lateinit var userLatLng: LatLng

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalViewHolder {
		val binding = HospitalCardBinding.inflate(
			LayoutInflater.from(parent.context), parent, false
		)

		return HospitalViewHolder(binding)
	}

	override fun onBindViewHolder(viewHolder: HospitalViewHolder, position: Int) {
		viewHolder.bind(getItem(position), onClick, userLatLng)
	}

	class HospitalComparator : DiffUtil.ItemCallback<Hospital>() {
		override fun areItemsTheSame(old: Hospital, new: Hospital) =
			old.name == new.name &&
				(old.latLng.latitude == new.latLng.latitude &&
				old.latLng.longitude == new.latLng.longitude)

		override fun areContentsTheSame(old: Hospital, new: Hospital) =
			old == new
	}

	class HospitalViewHolder(private val binding: HospitalCardBinding) : RecyclerView.ViewHolder(binding.root) {
		private val context = binding.root.context
		fun bind(hospital: Hospital, onClick: (Hospital) -> Unit, userLatLng: LatLng) {
			val distanceResults = FloatArray(1)

			binding.hopitalName.text = hospital.name
			binding.hospitalLocation.text = hospital.address
			Location.distanceBetween(
				userLatLng.latitude,
				userLatLng.longitude,
				hospital.latLng.latitude,
				hospital.latLng.longitude,
				distanceResults
			)
			val distance = if (distanceResults[0] < 1000f ) {
				"${distanceResults[0].toInt()} m"
			} else {
				"%.2f km".format(distanceResults[0]/1000f)
			}

			binding.hospitalDistance.text = distance
			binding.root.setOnClickListener {
				onClick(hospital)
			}
		}

		private fun setOccupancyColor(occupancy: Int){
			val color = if (occupancy <= 40) {
				ContextCompat.getColor(context, R.color.success)
			} else if (occupancy <= 75) {
				ContextCompat.getColor(context, R.color.warning)
			} else {
				ContextCompat.getColor(context, R.color.error)
			}

			binding.hospitalOccupancy.setBackgroundColor(color)

		}
	}
}