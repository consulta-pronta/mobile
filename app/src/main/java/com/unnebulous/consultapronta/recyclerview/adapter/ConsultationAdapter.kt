package com.unnebulous.consultapronta.recyclerview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.unnebulous.consultapronta.database.Consultation
import com.unnebulous.consultapronta.databinding.CardConsultationsBinding
import com.unnebulous.consultapronta.toLocalDateTime

class ConsultationAdapter: ListAdapter<Consultation, ConsultationAdapter.ConsultationViewHolder>(ConsultationComparator()) {

	lateinit var getProfessionalInfo: (String) -> Map<String, String>
	lateinit var acceptRequest: (String) -> Unit
	lateinit var denyRequest: (String) -> Unit

	override fun getItemViewType(position: Int): Int {
		return super.getItemViewType(position)
		// TODO: possivelmente útil para colocar o card de aceitar ou negar consultas propostas pelo profissional
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int
	): ConsultationViewHolder {
		val binding = CardConsultationsBinding.inflate(
			LayoutInflater.from(parent.context),
			parent,
			false
		)

		return ConsultationViewHolder(binding)
	}

	override fun onBindViewHolder(holder: ConsultationViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	inner class ConsultationViewHolder(
		private val binding: CardConsultationsBinding
	) : RecyclerView.ViewHolder(binding.root) {
		fun bind(consultation: Consultation) {
			binding.apply {
				val professional = getProfessionalInfo(consultation.id)

				professionalName.text = professional["name"]
				professionalCrm.text = professional["crm"]

				hospital.text = consultation.hospital

				datetime.text = consultation.date_time?.toLocalDateTime()?.format(TODO("Formatação"))
				// exemplo de data formatada: 12 de jun. de 2026, 09:30

				// buttonsLayout.visibility = View.GONE
				positiveButton.setOnClickListener {
					acceptRequest(consultation.id)
				}

				negativeButton.setOnClickListener {
					denyRequest(consultation.id)
				}
			}
		}
	}

	class ConsultationComparator : DiffUtil.ItemCallback<Consultation>() {
		override fun areItemsTheSame(old: Consultation, new: Consultation) = old.id == new.id
		override fun areContentsTheSame(old: Consultation, new: Consultation) = old == new
	}
}