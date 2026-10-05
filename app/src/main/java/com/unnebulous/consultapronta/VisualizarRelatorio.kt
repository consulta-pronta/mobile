package com.unnebulous.consultapronta

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.unnebulous.consultapronta.database.AuthManager
import com.unnebulous.consultapronta.database.Report
import com.unnebulous.consultapronta.database.Symptom
import com.unnebulous.consultapronta.database.User
import com.unnebulous.consultapronta.databinding.FragmentVisualizarRelatorioBinding
import com.unnebulous.consultapronta.recyclerview.adapter.ChronologyAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.sequences.forEach

class VisualizarRelatorio : Fragment() {
	private var _binding: FragmentVisualizarRelatorioBinding? = null
	private val binding get() = _binding!!

	private lateinit var reportId: String
	private lateinit var patientId: String
	private lateinit var renameReport: () -> Unit

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		arguments?.let {
			reportId = it.getString(ARG_REPORT_ID, "ERROR")
			patientId = it.getString(ARG_PATIENT_ID, "")
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentVisualizarRelatorioBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		updateHeader {
			changeHeaderType(Utils.HeaderType.TITLED, true)
			setScreenTitle("Titulo")
			setGoBackButtonOnClickListener {
				popBackStack()
			}

			val button = ImageButton(requireContext()).apply {
				layoutParams = ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT
				)

				setImageResource(R.drawable.ic_more_three_dots_vertical)
				imageTintList = ContextCompat.getColorStateList(context, R.color.textLight)
				setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
				setPadding(
					5,
					5,
					5,
					5,
				)
			}

			button.setOnClickListener {
				val contextThemeWrapper = ContextThemeWrapper(requireContext(), R.style.PopupMenuTheme)

				PopupMenu(contextThemeWrapper, button).apply {
					menuInflater.inflate(R.menu.menu_reports, this.menu)

					setOnMenuItemClickListener { menuItem ->
						showSnackbar(menuItem.title.toString())

						when (menuItem.itemId) {
							R.id.menu_allowed_professionals -> {}

							R.id.menu_rename_report -> {
								renameReport()
							}

							R.id.menu_delete_report -> {
								lifecycleScope.launch {
									try {
										Report.collection.document(reportId).delete()
										popBackStack()
									} catch (e: Exception) {
										Log.e("report", "deleteReport:failure", e)
									}
								}
							}
						}

						true
					}

					show()
				}
			}

			addAside(button)
		}
		resetNavbarEntryActive()

		configChart()

		val adapter = ChronologyAdapter()

		binding.recyclerview.adapter = adapter

		lifecycleScope.launch {
			try {
				val userData = AuthManager.getUserData()

				if (userData?.user_type == null) {
					throw Exception("User has no type associated")
				}

				if (userData.user_type == Utils.UserType.PROFISSIONAL) {
					binding.patientProfileLayout.visibility = View.VISIBLE
					getPatientUserData()
				}
			}
			catch (e: Exception) {
				Log.wtf(User.COLLECTION_NAME, "getUserData:failure", e)
			}

			try {
				val doc = Report.collection.document(reportId).get().await()
				val report = Report.fromDocument(doc)
				if (report == null || report.period_start == null || report.period_end == null) {
					popBackStack()
					return@launch
				}

				renameReport = {
					configBottomSheet { dialogBinding, dialog ->
						dialogBinding.icon.visibility = View.GONE
						dialogBinding.title.text = getString(R.string.rename_report)

						val input = EditText(context).apply {
							layoutParams = ViewGroup.LayoutParams(
								ViewGroup.LayoutParams.MATCH_PARENT,
								ViewGroup.LayoutParams.WRAP_CONTENT
							)

							inputType = InputType.TYPE_CLASS_TEXT
							background = ContextCompat.getDrawable(context, R.drawable.shape_input)
							setCompoundDrawablesRelativeWithIntrinsicBounds(
								ContextCompat.getDrawable(context, R.drawable.ic_document),
								null,
								null,
								null
							)
							compoundDrawablePadding = 20
							hint = getString(R.string.type_new_name)
							text = report.title.toEditable()
							setTextColor(ContextCompat.getColor(context, R.color.textLight))
							setHintTextColor(ContextCompat.getColor(context, R.color.textLight60))
							compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.textLight))
						}

						dialogBinding.body.addView(input)

						dialogBinding.positiveButton.apply {
							text = getString(R.string.save)
							setOnClickListener {
								dialogBinding.body.children.forEach { view ->
									val input = view as EditText
									val newTitle = input.text.toString().trim()

									newTitle.ifBlank {
										showSnackbar(getString(R.string.type_valid_name), Utils.SnackBarType.WARNING)
										return@setOnClickListener
									}

									lifecycleScope.launch {
										try {
											Report.collection.document(report.id).update("title", newTitle)
											updateHeader { setScreenTitle(newTitle) }
										} catch (e: Exception) {
											Log.e(Report.COLLECTION_NAME, "renameReport:failure", e)
										}
									}
								}

								dialog.dismiss()
							}
						}
					}
				}

				updateHeader { setScreenTitle(report.title) }
				binding.apply {
					reportIdText.text = getString(R.string.report_id, report.id)
					reportPeriodText.text = getString(
						R.string.view_report_period,
						report.period_start.toBrazilianLocale(),
						report.period_end.toBrazilianLocale()
					)
					reportDurationText.text = getString(
						R.string.view_report_duration,
						report.period_start.diffDays(report.period_end)
					)
				}

				val symptoms = report.getSymptomsList()

				adapter.submitList(symptoms)

				binding.apply {
					val area = symptoms.getMostAffectArea()
					val areas = symptoms.getAreaMap()[area]!!
					mostAffectedArea.text = area ?: "Nenhuma"
					mostAffectedAreaNumRegister.text = getString(
						R.string.most_affected_area_num_register,
						areas.size
					)
					val intensity = areas.getIntensityAverage()
					mostAffectedAreaIntensity.text = getString(
						R.string.most_affected_area_intensity,
						intensity
					)
					mostAffectedAreaIntensity.chipBackgroundColor = ColorStateList.valueOf(
						Utils.intensityToColor(requireContext(), intensity)
					)


				}

				populateChart(symptoms)
			} catch (e: Exception) {
				Log.e("report", "getReportAndSymptoms:failure", e)
			}
		}
	}

	private fun populateChart(symptoms: List<Symptom>) {
		val data = symptoms.map { it.date_time!! to it.intensity }.sortedBy { it.first }

		val xAxisLabels = listOf(data.first(), data.last()).map { it.first.toSimpleDate() }

//		// Jeito mais preciso, mas fica dificil de ver
//		val maxDiff = data.first().first.diffSeconds(data.last().first).toDouble()
//		val ratios = data.map {
//			it.first
//				.diffSeconds(data.first().first)
//				.toDouble()
//				.remap(0.0, maxDiff, 0.0, 1.0)
//				.toFloat()
//		}
		val ratios = List(data.size) { index ->
			index.toDouble().remap(0.0, data.size - 1.0, 0.0, 1.0).toFloat()
		}

		val entries = ratios
			.zip(data.map { it.second })
			.map { Entry(it.first, it.second.toFloat()) }
		Log.i("BRUH", entries.toString())

		populateChartHelper(entries, xAxisLabels)

	}

	private fun populateChartHelper(entries: List<Entry>, xAxisLabels: List<String>) {
		val dataSet = LineDataSet(entries, "").apply {
			lineWidth = 3f
			color = ContextCompat.getColor(requireContext(), R.color.primaryDark)
			circleRadius = 4f
			mode = LineDataSet.Mode.LINEAR

			setDrawCircles(true)
			setCircleColor(ContextCompat.getColor(requireContext(), R.color.primary))
			setDrawCircleHole(false) // não remover
			setDrawValues(false)
		}

		binding.chart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)

		binding.chart.data = LineData(dataSet)
		binding.chart.invalidate() // reinicia o gráfico
	}

	private fun configChart() {
		binding.chart.apply {
			description.isEnabled = false
			legend.isEnabled = false

			setDrawGridBackground(false)
			setTouchEnabled(false)
			setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface))

			xAxis.apply {
				position = XAxis.XAxisPosition.BOTTOM
				axisLineColor = ContextCompat.getColor(requireContext(), R.color.primaryDark)
				axisLineWidth = 2f
				textColor = ContextCompat.getColor(requireContext(), R.color.primaryDark)
				textSize = 12f
				granularity = 1f

				setDrawGridLines(false)
				setDrawAxisLine(true)
			}

			axisLeft.apply {
				axisMinimum = 0f
				axisMaximum = 10f
				axisLineColor = ContextCompat.getColor(requireContext(), R.color.primaryDark)
				axisLineWidth = 2f
				textColor = ContextCompat.getColor(requireContext(), R.color.primaryDark)
				textSize = 12f

				setDrawGridLines(false)
				setDrawAxisLine(true)
			}

			axisRight.isEnabled = false
		}
	}

	private fun getPatientUserData() {
		// TODO: precisa de permissões médicas
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	companion object {
		private const val ARG_REPORT_ID = "report_id"
		private const val ARG_PATIENT_ID = "patient_id"

		@JvmStatic
		fun newInstance(id: String, patientId: String? = null) =
			VisualizarRelatorio().apply {
				arguments = Bundle().apply {
					putString(ARG_REPORT_ID, id)
					putString(ARG_PATIENT_ID, patientId)
				}
			}
	}
}