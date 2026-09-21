package com.unnebulous.consultapronta.recyclerview.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.database.Symptom
import com.unnebulous.consultapronta.databinding.CardSymptomHistoryBinding
import com.unnebulous.consultapronta.toBrazilianLocale

class SymptomHistoryAdapter: ListAdapter<
	Symptom,
	SymptomHistoryAdapter.SymptomHistoryViewHolder>(SymptomHistoryComparator())
{
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SymptomHistoryViewHolder {
		val context = parent.context

		val binding = CardSymptomHistoryBinding.inflate(
			LayoutInflater.from(context),
			parent,
			false
		)

		return SymptomHistoryViewHolder(binding, context)
	}

	override fun onBindViewHolder(holder: SymptomHistoryViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	class SymptomHistoryComparator : DiffUtil.ItemCallback<Symptom>() {
		override fun areItemsTheSame(old: Symptom, new: Symptom) = old.id == new.id
		override fun areContentsTheSame(old: Symptom, new: Symptom) = old == new
	}

	class SymptomHistoryViewHolder(
		private val binding: CardSymptomHistoryBinding,
		private val context: Context
	): RecyclerView.ViewHolder(binding.root) {
		fun bind(symptom: Symptom) {
			binding.apply {
				dateText.text = symptom.date_time?.toBrazilianLocale() ?: "Data desconhecida"
				intensity.setIntensity(symptom.intensity)
				titleText.text = symptom.title
				descriptionText.text = symptom.description
				locationChip.setLocation(symptom.place)

				// sem anexos por enquanto
				separator.visibility = View.GONE
				annexesList.visibility = View.GONE
			}

			// NÃO REMOVA O COMENTÁRIO ABAIXO
			// binding.annexesList.removeAllViews() // isso impede que haja view repetida quando for reciclado
		}

		private fun addNewAnnex() {
			val defaultSize = 250

			val annexView = ImageView(context).apply {
				layoutParams = ViewGroup.LayoutParams(defaultSize, defaultSize)
				setBackgroundResource(R.drawable.shape_annex_background)
			}

			binding.annexesList.addView(annexView)
		}
	}
}