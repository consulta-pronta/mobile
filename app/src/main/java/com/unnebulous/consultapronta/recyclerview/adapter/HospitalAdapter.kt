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

class HospitalAdapter : ListAdapter<Hospital, HospitalAdapter.HospitalViewHolder>(HospitalComparator()){
	lateinit var onClick: (Hospital) -> Unit

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalViewHolder {
		val binding = HospitalCardBinding.inflate(
			LayoutInflater.from(parent.context), parent, false
		)

		return HospitalViewHolder(binding)
	}

	override fun onBindViewHolder(viewHolder: HospitalViewHolder, position: Int) {
		viewHolder.bind(getItem(position), onClick)
	}

	class HospitalComparator : DiffUtil.ItemCallback<Hospital>() {
		override fun areItemsTheSame(old: Hospital, new: Hospital) =
			false
		// exemplo: old.uid == new.uid

		override fun areContentsTheSame(old: Hospital, new: Hospital) =
			false
		// exemplo: old == new
	}

	class HospitalViewHolder(private val binding: HospitalCardBinding) : RecyclerView.ViewHolder(binding.root) {
		private val context = binding.root.context
		fun bind(hospital: Hospital, onClick: (Hospital) -> Unit) {
			// TODO: bind HospitalCard view

		}
		private fun setOccupancyColor(occupancy: Int, context: Context){
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