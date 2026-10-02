package com.unnebulous.consultapronta.recyclerview.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.database.Medication
import com.unnebulous.consultapronta.databinding.CardMedicationBinding
import com.unnebulous.consultapronta.toISODate
import kotlinx.coroutines.launch

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
		override fun areItemsTheSame(old: Medication, new: Medication) = old.id == new.id

		override fun areContentsTheSame(old: Medication, new: Medication) = old == new
	}

	class MedicationViewHolder(
		private val binding: CardMedicationBinding
	): RecyclerView.ViewHolder(binding.root) {
		fun bind(medication: Medication) {
			binding.apply {
				medication.apply {
					if (route == null || frequency_unit == null || dose_unit == null) {
						return
					}

					val context = itemView.context

					medicationName.text = name

					val frequencyUnitString = (
						if (frequency_value.toInt() > 1) frequency_unit.plural
						else frequency_unit.display
					).lowercase()
					useTime.text = context.getString(
						R.string.medication_card_use_time,
						dose_value.toInt(),
						dose_unit.unit,
						frequency_value.toInt(),
						frequencyUnitString,
					)

					typeUsage.text = context.getString(
						R.string.medication_card_type_usage,
						route.display.lowercase(),
					)

					val today = Timestamp.now().toISODate()
					checkbox.isChecked = wasTakenOn(today)

					checkbox.setOnCheckedChangeListener { _, bool ->
						itemView.findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
							try {
								Medication.collection.document(id)
									.update("taken_dates.$today", bool)

								Log.i(Medication.COLLECTION_NAME, "setMedicationChecked:success")
							} catch (e: Exception) {
								Log.e(Medication.COLLECTION_NAME, "setMedicationChecked:failure", e)
							}
						}
					}
				}
			}
		}
	}
}