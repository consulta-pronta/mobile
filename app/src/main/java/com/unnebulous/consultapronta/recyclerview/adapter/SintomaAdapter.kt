package com.unnebulous.consultapronta.recyclerview.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unnebulous.consultapronta.database.Symptom
import com.unnebulous.consultapronta.databinding.SintomaCardBinding

// TODO: Trocar "Any" pela classe de sintoma e ViewHolder
class SintomaAdapter: ListAdapter<Symptom, SintomaAdapter.SintomaViewHolder>(SintomaComparator()) {
	enum class SymptomViewType { COMPACT, DETAILED }

	override fun getItemViewType(position: Int): Int {
		return super.getItemViewType(position)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SintomaViewHolder {
		TODO("Not yet implemented")
	}

	override fun onBindViewHolder(holder: SintomaViewHolder, position: Int) {
		TODO("Not yet implemented")
	}

	class SintomaComparator : DiffUtil.ItemCallback<Symptom>() {
		override fun areItemsTheSame(old: Symptom, new: Symptom) =
			false
			// old.uid == new.uid

		override fun areContentsTheSame(old: Symptom, new: Symptom) =
			false
			// old == new
	}

	class SintomaViewHolder(private val binding: SintomaCardBinding): RecyclerView.ViewHolder(binding.root) {
		fun bind() {

		}
	}
}