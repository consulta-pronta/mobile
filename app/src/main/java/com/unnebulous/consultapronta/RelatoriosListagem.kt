package com.unnebulous.consultapronta

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.ListenerRegistration
import com.unnebulous.consultapronta.database.Report
import com.unnebulous.consultapronta.database.Symptom
import com.unnebulous.consultapronta.databinding.FragmentRelatoriosListagemBinding
import com.unnebulous.consultapronta.recyclerview.adapter.ReportListAdapter
import kotlinx.coroutines.launch
import kotlin.sequences.forEach
import kotlin.text.isBlank

class RelatoriosListagem : Fragment() {
	private var _binding: FragmentRelatoriosListagemBinding? = null
	private val binding get() = _binding!!

	private lateinit var reportListener: ListenerRegistration

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentRelatoriosListagemBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		updateHeader {
			changeHeaderType(Utils.HeaderType.TITLED, true)
			setScreenTitle(getString(R.string.reports_screen_title))
			setGoBackButtonOnClickListener {
				popBackStack()
			}
		}
		resetNavbarEntryActive()

		val adapter = ReportListAdapter().apply {
			onClick = { report ->
				changeFragmentWithBackStack(
					VisualizarRelatorio.newInstance(report.id)
				)
			}

			renameReport = { report ->
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

			deleteReport = { report ->
				lifecycleScope.launch {
					try {
						Report.collection.document(report.id).delete()
					} catch (e: Exception) {
						Log.e("report", "deleteReport:failure", e)
					}
				}
			}
		}

		binding.apply {
			recyclerview.adapter = adapter

			addNewReport.setOnClickListener {
				changeFragmentWithBackStack(GerarRelatorio())
			}
		}

		reportListener = Report
			.collection
			.addSnapshotListener { snapshots, exception ->
				if (exception != null) {
					Log.e("report", "getReportsListener:failure", exception)
					return@addSnapshotListener
				}
				if (snapshots == null) { return@addSnapshotListener }

				val reports = snapshots.documents.mapNotNull { Report.fromDocument(it) }
				adapter.submitList(reports)
			}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
		reportListener.remove()
	}
}