package com.unnebulous.consultapronta.recyclerview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unnebulous.consultapronta.database.SymptomHistory
import com.unnebulous.consultapronta.databinding.SymptomCardBinding

class SymptomHistoryAdapter: ListAdapter<SymptomHistory, SymptomHistoryAdapter.SymptomHistoryViewHolder>(SymptomHistoryComparator()) {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SymptomHistoryViewHolder {
		val binding = SymptomCardBinding.inflate(
			LayoutInflater.from(parent.context),
			parent,
			false
		)

		return SymptomHistoryViewHolder(binding)
	}

	override fun onBindViewHolder(holder: SymptomHistoryViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	class SymptomHistoryComparator : DiffUtil.ItemCallback<SymptomHistory>() {
		// TODO
		override fun areItemsTheSame(old: SymptomHistory, new: SymptomHistory) = false

		override fun areContentsTheSame(old: SymptomHistory, new: SymptomHistory) = false
	}

	// TODO: criar layout
	class SymptomHistoryViewHolder(private val binding: SymptomCardBinding): RecyclerView.ViewHolder(binding.root) {
		fun bind(symptom: SymptomHistory) {
			binding.apply {
				// TODO
			}
		}
	}
}