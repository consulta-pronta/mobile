package com.unnebulous.consultapronta.recyclerview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unnebulous.consultapronta.database.Professional
import com.unnebulous.consultapronta.databinding.CardProfessionalMedicalPermissionsBinding

class MedicalPermissionsAdapter : ListAdapter<Professional, MedicalPermissionsAdapter.MedicalPermissionsViewHolder>(MedicalPermissionComparator()) {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicalPermissionsViewHolder {
		val binding = CardProfessionalMedicalPermissionsBinding.inflate(
			LayoutInflater.from(parent.context),
			parent,
			false
		)

		return MedicalPermissionsViewHolder(binding)
	}

	override fun onBindViewHolder(holder: MedicalPermissionsViewHolder, position: Int) {
		holder.bind()
	}

	class MedicalPermissionComparator : DiffUtil.ItemCallback<Professional>() {
		override fun areItemsTheSame(old: Professional, new: Professional): Boolean = old.id == new.id

		override fun areContentsTheSame(old: Professional, new: Professional): Boolean = old == new
	}

	class MedicalPermissionsViewHolder(private val binding: CardProfessionalMedicalPermissionsBinding): RecyclerView.ViewHolder(binding.root) {
		fun bind() {

		}
	}
}