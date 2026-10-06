package com.unnebulous.consultapronta.recyclerview.adapter

import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.database.Report
import com.unnebulous.consultapronta.databinding.CardReportBinding
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
		val binding = CardReportBinding.inflate(
			LayoutInflater.from(parent.context), parent, false
		)

		return when (viewType) {
			ViewType.PATIENT.ordinal -> {
				val binding = CardReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
				ReportPatientViewHolder(binding)
			}

			ViewType.PROFESSIONAL.ordinal -> {
				val binding = CardReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

	class ReportProfessionalViewHolder(private val binding: CardReportBinding): RecyclerView.ViewHolder(binding.root) {

		/*

		<com.unnebulous.consultapronta.views.ProfilePictureView
				android:id="@+id/patient_profile_picture"

				android:layout_width="60dp"
				android:layout_height="60dp"

				app:isInput="false"
				app:layout_constraintTop_toTopOf="parent"
				app:layout_constraintBottom_toBottomOf="parent"
				app:layout_constraintStart_toStartOf="parent" />

			<LinearLayout
				android:layout_width="wrap_content"
				android:layout_height="wrap_content"
				android:orientation="vertical"
				android:layout_marginStart="@dimen/default_inner_elements_spacing"

				app:layout_constraintTop_toTopOf="parent"
				app:layout_constraintBottom_toBottomOf="parent"
				app:layout_constraintStart_toEndOf="@id/patient_profile_picture">
				<TextView
					android:id="@+id/patient_name"

					android:layout_width="wrap_content"
					android:layout_height="wrap_content"
					android:textStyle="bold"

					tools:text="@tools:sample/full_names" />

				<TextView
					android:id="@+id/patient_email"

					android:layout_width="wrap_content"
					android:layout_height="wrap_content"

					tools:text="@tools:sample/lorem[1]" />

				<TextView
					android:id="@+id/patient_phone_number"

					android:layout_width="wrap_content"
					android:layout_height="wrap_content"

					tools:text="@tools:sample/us_phones" />
			</LinearLayout>
		 */
		fun bind(report: Report, onClick: (Report) -> Unit) {

		}
	}
}