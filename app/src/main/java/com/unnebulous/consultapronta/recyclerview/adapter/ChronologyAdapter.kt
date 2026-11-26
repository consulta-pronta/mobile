package com.unnebulous.consultapronta.recyclerview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.Utils
import com.unnebulous.consultapronta.database.BaseDocument
import com.unnebulous.consultapronta.database.Symptom
import com.unnebulous.consultapronta.databinding.CardChronologyBinding
import com.unnebulous.consultapronta.toBrazilianLocale

class ChronologyAdapter: ListAdapter<BaseDocument, RecyclerView.ViewHolder>(WeAreCompartor()) {
	class WeAreCompartor: DiffUtil.ItemCallback<BaseDocument>() {
		override fun areItemsTheSame(old: BaseDocument, new: BaseDocument) = old.id == new.id
		override fun areContentsTheSame(old: BaseDocument, new: BaseDocument) = old == new
	}

	enum class ItemType { SYMPTOM }

	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			is Symptom -> ItemType.SYMPTOM.ordinal
			else -> -1
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		val binding = CardChronologyBinding.inflate(inflater, parent, false)

		return ChronologyViewHolder(binding)
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		val item = getItem(position)
		val isLast = position == itemCount - 1
		when (holder) {
			is ChronologyViewHolder -> holder.bind(item as Symptom, isLast)
		}
	}

	class ChronologyViewHolder(
		val binding: CardChronologyBinding
	): RecyclerView.ViewHolder(binding.root) {
		fun bind(item: Symptom, isLast: Boolean = false) {
			if (item.id.isBlank()) {
				binding.root.forEach { view ->
					view.clipToOutline = true
					view.foreground = ContextCompat.getDrawable(binding.root.context, R.drawable.skeleton_gradient)
					view.alpha = Utils.SKELETON_OPACITY
				}
				return
			}

			binding.apply {
				title.text = item.title
				date.text = item.date_time?.toBrazilianLocale(root.context) ?: "Data desconhecida"
				description.text = ContextCompat
					.getString(binding.root.context, R.string.template_chronology_symptom_description)
					.format(item.intensity, item.description)
				icon.setImageResource(R.drawable.ic_graphic)

				if (isLast) {
					stick.visibility = View.INVISIBLE
				}
			}
		}
	}
}