package com.unnebulous.consultapronta.recyclerview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.database.Report
import com.unnebulous.consultapronta.databinding.CardReportBinding
import com.unnebulous.consultapronta.diffDays
import com.unnebulous.consultapronta.toBrazilianLocale

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
		override fun areItemsTheSame(old: Report, new: Report) = old.id == new.id

		override fun areContentsTheSame(old: Report, new: Report) =  old == new
	}

	class ReportListViewHolder(private val binding: CardReportBinding): RecyclerView.ViewHolder(binding.root) {
		fun bind(report: Report, onClick: (Report) -> Unit) {
			binding.apply {
				val period = report.period_start!!.diffDays(report.period_end!!)

				reportCardTitle.text = report.title
				reportCardPeriod.text = itemView.context.getString(
					R.string.report_card_period_placeholder,
					period
				)
				reportCardDate.text = report.created_at!!.toBrazilianLocale()

				root.setOnClickListener { onClick(report) }
			}
		}
	}
}