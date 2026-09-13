package com.unnebulous.consultapronta.recyclerview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.unnebulous.consultapronta.database.Symptom
import com.unnebulous.consultapronta.databinding.CardChronologyBinding

class ChronologyAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
	enum class ItemType { SYMPTOM, IDK_LOL }

	override fun getItemViewType(position: Int): Int {
		// TODO: alguma coisa para verificar o que é o que
		return when (position) {
			else -> ItemType.SYMPTOM.ordinal
		}
	}

	// TODO: com base no tipo, retornar o ViewHolder certo
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		val binding = CardChronologyBinding.inflate(LayoutInflater.from(parent.context), parent, false)

		return when (viewType) {
			ItemType.SYMPTOM.ordinal -> ChronologySymptomViewHolder(binding)
			else -> ChronologySymptomViewHolder(binding)
		}
	}

	// TODO: com base no tipo de ViewHolder, dar binding
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		// val item = something

		when (holder) {
			is ChronologySymptomViewHolder -> holder.bind(Symptom())
		}
	}

	// TODO: tem que retornar a quantidade de items, ora pois
	override fun getItemCount(): Int = 0

	class ChronologySymptomViewHolder(val binding: CardChronologyBinding): RecyclerView.ViewHolder(binding.root) {
		fun bind(item: Symptom) {

		}
	}
}