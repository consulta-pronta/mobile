package com.unnebulous.consultapronta.recyclerview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unnebulous.consultapronta.database.Symptom
import com.unnebulous.consultapronta.databinding.CardSymptomBinding
import com.unnebulous.consultapronta.toBrazilianLocale

class SymptomAdapter: ListAdapter<Symptom, SymptomAdapter.SymptomViewHolder>(SymptomComparator()) {
	enum class SymptomViewType { COMPACT, DETAILED }

	lateinit var onClick: (Symptom) -> Unit

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SymptomViewHolder {
		val binding = CardSymptomBinding.inflate(
			LayoutInflater.from(parent.context),
			parent,
			false
		)

		return SymptomViewHolder(binding)
	}

	override fun onBindViewHolder(viewHolder: SymptomViewHolder, position: Int) {
		viewHolder.bind(getItem(position), onClick)
	}

	class SymptomComparator : DiffUtil.ItemCallback<Symptom>() {
		override fun areItemsTheSame(old: Symptom, new: Symptom) = old.id == new.id

		override fun areContentsTheSame(old: Symptom, new: Symptom) = old == new
	}

	class SymptomViewHolder(private val binding: CardSymptomBinding): RecyclerView.ViewHolder(binding.root) {
		fun bind(symptom: Symptom, onClick: (Symptom) -> Unit) {
			binding.apply {
				titleText.text = symptom.title
				descriptionText.text = symptom.description
				symptomDateText.text = symptom.dateTime!!.toBrazilianLocale()
				intensityView.setIntensity(symptom.intensity)
				placeView.setLocation(symptom.place)

				// Não existem ainda
				symptomAnnexesQuantityText.visibility = View.GONE
				editButton.visibility = View.GONE

				root.setOnClickListener { onClick(symptom) }
			}
		}
	}
}