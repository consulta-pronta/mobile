package com.unnebulous.consultapronta.recyclerview.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.unnebulous.consultapronta.database.AuthManager
import com.unnebulous.consultapronta.database.Consultation
import com.unnebulous.consultapronta.databinding.CardConsultationsBinding
import com.unnebulous.consultapronta.toBrazillianFullDateAndTime
import com.unnebulous.consultapronta.toLocalDateTime

class ConsultationAdapter: ListAdapter<Pair<Consultation, Map<String, String>>, ConsultationAdapter.ConsultationViewHolder>(ConsultationComparator()) {
	lateinit var acceptRequest: (String) -> Unit
	lateinit var denyRequest: (String) -> Unit

	private enum class ViewType { VIEW, ACCEPT }

	private fun checkUserCompatibility(status: Consultation.Companion.ConsultationStatus): Boolean {
		if (
			status == Consultation.Companion.ConsultationStatus.MARCADA ||
			status == Consultation.Companion.ConsultationStatus.NEGADA
		) {
			return false
		}

		val statusString = status.toString()
		val userTypeInStatus = statusString.substring(statusString.indexOf('_') + 1).lowercase()
		return userTypeInStatus != AuthManager.userInFirestore.user_type.toString().lowercase()
	}

	override fun getItemViewType(position: Int): Int =
		if (checkUserCompatibility(getItem(position).first.status)) {
			ViewType.ACCEPT.ordinal
		} else {
			ViewType.VIEW.ordinal
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

		return ConsultationViewHolder(binding, viewType)
	}

	override fun onBindViewHolder(holder: ConsultationViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	inner class ConsultationViewHolder(
		private val binding: CardConsultationsBinding,
		val viewType: Int
	) : RecyclerView.ViewHolder(binding.root) {
		fun bind(pair: Pair<Consultation, Map<String, String>>) {
			binding.apply {
				val consultation = pair.first
				val professional = pair.second

				professionalName.text = professional["name"]
				professionalCrm.text = professional["crm"]

				hospital.text = consultation.hospital

				datetime.text = consultation.date_time?.toLocalDateTime()?.toBrazillianFullDateAndTime()

				buttonsLayout.isVisible = viewType == ViewType.ACCEPT.ordinal

				positiveButton.setOnClickListener {
					acceptRequest(consultation.id)
				}

				negativeButton.setOnClickListener {
					denyRequest(consultation.id)
				}
			}
		}
	}

	class ConsultationComparator : DiffUtil.ItemCallback<Pair<Consultation, Map<String, String>>>() {
		override fun areItemsTheSame(old: Pair<Consultation, Map<String, String>>, new: Pair<Consultation, Map<String, String>>) = old.first == new.first && old.second == new.second
		override fun areContentsTheSame(old: Pair<Consultation, Map<String, String>>, new: Pair<Consultation, Map<String, String>>) = old == new
	}
}