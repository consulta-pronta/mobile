package com.unnebulous.consultapronta.recyclerview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unnebulous.consultapronta.database.Medication
import com.unnebulous.consultapronta.databinding.CardMedicationBinding

class MedicationAdapter: ListAdapter<Medication, MedicationAdapter.MedicationViewHolder>(MedicationComparator()) {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
		val binding = CardMedicationBinding.inflate(
			LayoutInflater.from(parent.context),
			parent,
			false
		)

		return MedicationViewHolder(binding)
	}

	override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	class MedicationComparator : DiffUtil.ItemCallback<Medication>() {
		override fun areItemsTheSame(old: Medication, new: Medication) = false //old.id == new.id

		override fun areContentsTheSame(old: Medication, new: Medication) = false // old == new
	}

	class MedicationViewHolder(private val binding: CardMedicationBinding): RecyclerView.ViewHolder(binding.root) {
		fun bind(medication: Medication) {}
	}
}