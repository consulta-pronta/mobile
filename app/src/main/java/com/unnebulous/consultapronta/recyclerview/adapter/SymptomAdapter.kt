package com.unnebulous.consultapronta.recyclerview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unnebulous.consultapronta.database.Symptom
import com.unnebulous.consultapronta.databinding.SymptomCardBinding
import java.text.SimpleDateFormat
import java.util.Locale

class SymptomAdapter: ListAdapter<Symptom, SymptomAdapter.SintomaViewHolder>(SintomaComparator()) {
	enum class SymptomViewType { COMPACT, DETAILED }

	lateinit var onClick: (Symptom) -> Unit

	override fun getItemViewType(position: Int): Int {
		return super.getItemViewType(position)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SintomaViewHolder {
		val binding = SymptomCardBinding.inflate(
			LayoutInflater.from(parent.context),
			parent,
			false
		)

		return SintomaViewHolder(binding)
	}

	override fun onBindViewHolder(viewHolder: SintomaViewHolder, position: Int) {
		viewHolder.bind(getItem(position), onClick)
	}

	class SintomaComparator : DiffUtil.ItemCallback<Symptom>() {
		override fun areItemsTheSame(old: Symptom, new: Symptom) = old.id == new.id

		override fun areContentsTheSame(old: Symptom, new: Symptom) = old == new
	}

	class SintomaViewHolder(private val binding: SymptomCardBinding): RecyclerView.ViewHolder(binding.root) {
		fun bind(symptom: Symptom, onCLick: (Symptom) -> Unit) {
			binding.apply {
				titleText.text = symptom.title
				descriptionText.text = symptom.description
				symptomDateText.text = symptom.dateTime.let { timestamp ->
					val date = timestamp!!.toDate()
					val locale = Locale.forLanguageTag("pt-BR")

					SimpleDateFormat("d 'de' MMM 'de' yyyy", locale).format(date)
				}
				intensityView.setIntensity(symptom.intensity)
				placeView.setLocation(symptom.place)

				// Não existem ainda
				symptomAnnexesQuantityText.visibility = View.GONE
				editButton.visibility = View.GONE

				root.setOnClickListener { onCLick(symptom) }
			}
		}
	}
}