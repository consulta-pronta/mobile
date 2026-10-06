package com.unnebulous.consultapronta.recyclerview.adapter

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.marginEnd
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.database.Report
import com.unnebulous.consultapronta.databinding.CardReportBinding
import com.unnebulous.consultapronta.databinding.CardReportProfessionalBinding
import com.unnebulous.consultapronta.diffDays
import com.unnebulous.consultapronta.showSnackbar
import com.unnebulous.consultapronta.toBrazilianLocale

class ReportListAdapter: ListAdapter<Report, RecyclerView.ViewHolder>(ReportComparator()) {

	private enum class ViewType { PATIENT, PROFESSIONAL }

	lateinit var onClick: (Report) -> Unit
	lateinit var renameReport: (Report) -> Unit
	lateinit var deleteReport: (Report) -> Unit
	var userIsProfessional = false

	override fun getItemViewType(position: Int): Int {
		return if (userIsProfessional) {
			ViewType.PROFESSIONAL.ordinal
		} else {
			ViewType.PATIENT.ordinal
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return when (viewType) {
			ViewType.PATIENT.ordinal -> {
				val binding = CardReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
				ReportPatientViewHolder(binding)
			}

			ViewType.PROFESSIONAL.ordinal -> {
				val binding = CardReportProfessionalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
				ReportProfessionalViewHolder(binding)
			}

			else -> {
				val binding = CardReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
				ReportPatientViewHolder(binding)
			}
		}
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		when (holder) {
			is ReportPatientViewHolder -> holder.bind(getItem(position), onClick, renameReport, deleteReport)
			is ReportProfessionalViewHolder -> holder.bind(getItem(position), onClick)
		}
	}

	class ReportComparator : DiffUtil.ItemCallback<Report>() {
		override fun areItemsTheSame(old: Report, new: Report) = old.id == new.id

		override fun areContentsTheSame(old: Report, new: Report) =  old == new
	}

	class ReportPatientViewHolder(private val binding: CardReportBinding): RecyclerView.ViewHolder(binding.root) {
		fun bind(report: Report, onClick: (Report) -> Unit, renameReport: (Report) -> Unit, deleteReport: (Report) -> Unit) {
			binding.apply {
				val period = report.period_start!!.diffDays(report.period_end!!)

				reportCardTitle.text = report.title
				reportCardPeriod.text = itemView.context.getString(
					R.string.report_card_period_placeholder,
					period
				)
				reportCardDate.text = report.created_at!!.toBrazilianLocale()

				root.setOnClickListener { onClick(report) }

				optionsButton.setOnClickListener {
					val contextThemeWrapper = ContextThemeWrapper(binding.root.context, R.style.PopupMenuTheme)

					PopupMenu(contextThemeWrapper, optionsButton).apply {
						menuInflater.inflate(R.menu.menu_reports, this.menu)

						setOnMenuItemClickListener { menuItem ->
							when (menuItem.itemId) {
								R.id.menu_allowed_professionals -> {}

								R.id.menu_rename_report -> {
									renameReport(report)
								}

								R.id.menu_delete_report -> {
									deleteReport(report)
								}
							}

							true
						}

						show()
					}
				}
			}
		}
	}

	class ReportProfessionalViewHolder(private val binding: CardReportProfessionalBinding): RecyclerView.ViewHolder(binding.root) {
		fun bind(report: Report, onClick: (Report) -> Unit) {

			/*
			binding.reports.addView(
				addNewReport("TITULO", "PERIODO FORMATADO (R.string.report_card_period_placeholder_professional)") {
					// on click
				}
			)
			 */

		}

		private fun addNewReport(title: String, period: String, onClick: () -> Unit): View {
			val context = binding.root.context

			val icon = AppCompatImageView(context).apply {
				layoutParams = ViewGroup.MarginLayoutParams(
					35,
					35
				).apply {
					marginEnd = R.dimen.default_inner_elements_spacing
				}

				setImageResource(R.drawable.ic_reports)
				imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primaryDark))
				background = ContextCompat.getDrawable(context, R.drawable.shape_circle_accent)
				backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.surface))
				setPadding(6, 6, 6, 6)
			}

			val titleView = TextView(context).apply {
				textSize = 20f
				setTypeface(null, Typeface.BOLD)
				text = title
			}

			val periodView = TextView(context).apply {
				text = period
			}

			val infoLayout = LinearLayout(context).apply {
				orientation = LinearLayout.VERTICAL

				addView(titleView)
				addView(periodView)
			}

			return LinearLayout(context).apply {
				orientation = LinearLayout.HORIZONTAL

				addView(icon)
				addView(infoLayout)
			}
		}
	}
}