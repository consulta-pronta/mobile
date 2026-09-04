package com.unnebulous.consultapronta.recyclerview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unnebulous.consultapronta.database.Report
import com.unnebulous.consultapronta.databinding.CardReportBinding

class ReportListAdapter: ListAdapter<Report, ReportListAdapter.ReportListViewHolder>(ReportComparator()) {

	lateinit var onClick: (Report) -> Unit

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportListViewHolder {
		val binding = CardReportBinding.inflate(
			LayoutInflater.from(parent.context), parent, false
		)

		return ReportListViewHolder(binding)
	}

	override fun onBindViewHolder(holder: ReportListViewHolder, position: Int) {
		holder.bind(getItem(position), onClick)
	}

	class ReportComparator : DiffUtil.ItemCallback<Report>() {
		override fun areItemsTheSame(old: Report, new: Report) = false //old.id == new.id

		override fun areContentsTheSame(old: Report, new: Report) = false // old == new
	}

	class ReportListViewHolder(private val binding: CardReportBinding): RecyclerView.ViewHolder(binding.root) {
		fun bind(report: Report, onClick: (Report) -> Unit) {
		}
	}
}