package com.unnebulous.consultapronta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.unnebulous.consultapronta.database.Report
import com.unnebulous.consultapronta.database.Symptom
import com.unnebulous.consultapronta.databinding.FragmentVisualizarRelatorioBinding
import com.unnebulous.consultapronta.recyclerview.adapter.ChronologyAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class VisualizarRelatorio : Fragment() {
	private var _binding: FragmentVisualizarRelatorioBinding? = null
	private val binding get() = _binding!!

	private lateinit var reportId: String
	private lateinit var report: Report

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		arguments?.let {
			reportId = it.getString(ARG_REPORT_ID, "ERROR")
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
		}

		configChart()

		_populateChart()

		val adapter = ChronologyAdapter()

		binding.recyclerview.adapter = adapter

		lifecycleScope.launch {
			try {
				val doc = Report.collection.document(reportId).get().await()
				val report = Report.fromDocument(doc)
				if (report == null || report.period_start == null || report.period_end == null) {
					popBackStack()
					return@launch
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

//				val symptoms = Symptom.getBetweenDates(report.period_start, report.period_end)
			} catch (e: Exception) {

			}
		}
	}

	// TODO: remover isso após o backend
	private fun _populateChart() {
		// lista de pontos
		val entries = listOf(
			Entry(0f, 8f),
			Entry(1f, 3.5f),
			Entry(2f, 6.2f)
		)

		// conjunto de dados
		val dataSet = LineDataSet(entries, "").apply {
			lineWidth = 5f
			color = ContextCompat.getColor(requireContext(), R.color.primaryDark) // cor da linha
			circleRadius = 4f
			mode = LineDataSet.Mode.LINEAR

			setDrawCircles(true)
			setCircleColor(ContextCompat.getColor(requireContext(), R.color.primary))
			setDrawCircleHole(false) // não remover
			setDrawValues(false)
		}

		// esse comando define quais serão os valores do eixo X
		binding.chart.xAxis.valueFormatter = IndexAxisValueFormatter(listOf("10/04", "12/04", "14/04"))

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

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	companion object {
		private const val ARG_REPORT_ID = "report_id"

		@JvmStatic
		fun newInstance(id: String) =
			VisualizarRelatorio().apply {
				arguments = Bundle().apply {
					putString(ARG_REPORT_ID, id)
				}
			}
	}
}