
package com.unnebulous.consultapronta

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.unnebulous.consultapronta.database.AuthManager
import com.unnebulous.consultapronta.database.DatabaseManager
import com.unnebulous.consultapronta.database.MedicalPermission
import com.unnebulous.consultapronta.database.Report
import com.unnebulous.consultapronta.database.Symptom
import com.unnebulous.consultapronta.database.User
import com.unnebulous.consultapronta.databinding.FragmentVisualizarRelatorioBinding
import com.unnebulous.consultapronta.recyclerview.adapter.ChronologyAdapter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class VisualizarRelatorio : Fragment() {

	private var _binding: FragmentVisualizarRelatorioBinding? = null
	private val binding get() = _binding!!

	private lateinit var reportId: String
	private lateinit var patientId: String

	private var renameReport: (() -> Unit)? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		reportId = arguments?.getString(ARG_REPORT_ID).orEmpty()
		patientId = arguments?.getString(ARG_PATIENT_ID).orEmpty()
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentVisualizarRelatorioBinding.inflate(
			inflater,
			container,
			false
		)

		return binding.root
	}

	@SuppressLint("UseCompatTextViewDrawableApis")
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val ownerId = patientId.ifBlank {
			AuthManager.uid.orEmpty()
		}

		if (reportId.isBlank() || ownerId.isBlank()) {
			showSnackbar(
				getString(R.string.report_not_identified),
				Utils.SnackBarType.WARNING
			)
			popBackStack()
			return
		}

		val reportRef = DatabaseManager.userDocument(
			Report.COLLECTION_NAME,
			reportId,
			ownerId
		)

		updateHeader {

			changeHeaderType(Utils.HeaderType.TITLED, true)
			setScreenTitle(getString(R.string.loading))
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
				setPadding(5, 5, 5, 5)
			}

			button.setOnClickListener {

				val contextThemeWrapper = ContextThemeWrapper(
					requireContext(),
					R.style.PopupMenuTheme
				)

				PopupMenu(contextThemeWrapper, button).apply {

					menuInflater.inflate(R.menu.menu_reports, menu)
					setOnMenuItemClickListener { menuItem ->

						when (menuItem.itemId) {
							R.id.menu_allowed_professionals -> {
								manageAllowedProfessionals()
							}
							R.id.menu_rename_report -> {
								renameReport?.invoke()
							}
							R.id.menu_delete_report -> {
								button.post {
									if (!isAdded || _binding == null) {
										return@post
									}
									showConfirmationDialog(
										getString(R.string.wish_to_delete_report),
										getString(R.string.on_confirm_delete_report)
									) {
										reportRef.delete().await()
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

		viewLifecycleOwner.lifecycleScope.launch {

			try {

				val userData = AuthManager.getUserData()

				if (userData?.user_type == null) {
					throw Exception(getString(R.string.no_clear_user_type))
				}

				if (userData.user_type == Utils.UserType.PROFISSIONAL) {
					binding.patientProfileLayout.visibility = View.VISIBLE
					getPatientUserData()
				}

			} catch (e: CancellationException) {
				throw e

			} catch (e: Exception) {
				Log.e(User.COLLECTION_NAME, getString(R.string.getuserdata_failure), e)
			}

			try {

				val doc = reportRef.get().await()

				if (!doc.exists()) {
					showSnackbar(
						getString(R.string.report_not_found),
						Utils.SnackBarType.WARNING
					)
					popBackStack()
					return@launch
				}

				val report = Report.fromDocument(doc)

				if (
					report == null ||
					report.period_start == null ||
					report.period_end == null
				) {
					showSnackbar(
						getString(R.string.invalid_data),
						Utils.SnackBarType.WARNING
					)
					popBackStack()
					return@launch
				}

				renameReport = {
					configBottomSheet { dialogBinding, dialog ->

						dialogBinding.icon.visibility = View.GONE
						dialogBinding.title.text =
							getString(R.string.rename_report)

						val input = EditText(requireContext()).apply {

							layoutParams = ViewGroup.LayoutParams(
								ViewGroup.LayoutParams.MATCH_PARENT,
								ViewGroup.LayoutParams.WRAP_CONTENT
							)

							inputType = InputType.TYPE_CLASS_TEXT
							background = ContextCompat.getDrawable(requireContext(), R.drawable.shape_input)

							setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable
								(requireContext(), R.drawable.ic_document),
								null, null, null
							)

							compoundDrawablePadding = 20
							hint = getString(R.string.type_new_name)

							setText(report.title)
							setTextColor(
								ContextCompat.getColor(requireContext(), R.color.textLight)
							)

							setHintTextColor(
								ContextCompat.getColor(requireContext(), R.color.textLight60)
							)

							compoundDrawableTintList = ColorStateList
								.valueOf(ContextCompat.getColor(requireContext(), R.color.textLight))
						}

						dialogBinding.body.addView(input)
						dialogBinding.positiveButton.apply {
							text = getString(R.string.save)

							setOnClickListener {
								val newTitle = input.text.toString().trim()

								if (newTitle.isBlank()) {
									showSnackbar(
										getString(R.string.type_valid_name),
										Utils.SnackBarType.WARNING
									)

									return@setOnClickListener
								}

								viewLifecycleOwner.lifecycleScope.launch {

									try {
										reportRef.update("title", newTitle).await()

										if (_binding != null) {
											updateHeader {
												setScreenTitle(newTitle)
											}
											showSnackbar(
												context.getString(R.string.rename_success),
												Utils.SnackBarType.SUCCESS
											)
										}

										dialog.dismiss()
									} catch (e: CancellationException) {
										throw e

									} catch (e: Exception) {
										Log.e(Report.COLLECTION_NAME, getString(R.string.renamereport_failure), e)

										if (_binding != null) {
											showSnackbar(
												getString(R.string.failed_to_rename_report),
												Utils.SnackBarType.WARNING
											)
										}
									}
								}
							}
						}
					}
				}

				updateHeader {
					setScreenTitle(report.title)
				}

				binding.apply {
					reportIdText.text = getString(
						R.string.report_id,
						report.id
					)

					reportPeriodText.text = getString(
						R.string.view_report_period,
						report.period_start.toBrazilianLocale(requireContext()),
						report.period_end.toBrazilianLocale(requireContext())
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
					val areas = area
						?.let { symptoms.getAreaMap()[it] }
						.orEmpty()

					mostAffectedArea.text = area ?: getString(R.string.none)
					mostAffectedAreaNumRegister.text = getString(
						R.string.most_affected_area_num_register,
						areas.size
					)

					val intensity: Double = if (areas.isEmpty()) {
						0.0
					} else {
						areas.getIntensityAverage()
					}

					mostAffectedAreaIntensity.text = getString(
						R.string.most_affected_area_intensity,
						intensity
					)
					mostAffectedAreaIntensity.chipBackgroundColor =
						ColorStateList.valueOf(Utils.intensityToColor(requireContext(), intensity))
				}
				populateChart(symptoms)

			} catch (e: CancellationException) {
				throw e

			} catch (e: Exception) {
				Log.e("report", getString(R.string.getreportandsymptoms_failure), e)

				if (_binding != null) {
					showSnackbar(
						getString(R.string.failed_to_load_report),
						Utils.SnackBarType.WARNING
					)
				}
			}
		}
	}

	private fun populateChart(symptoms: List<Symptom>) {

		val data = symptoms
			.mapNotNull { symptom ->
				symptom.date_time?.let {
					it to symptom.intensity
				}
			}
			.sortedBy { it.first }

		if (data.isEmpty()) {

			binding.chart.clear()
			binding.chart.invalidate()
			return
		}

		val xAxisLabels = if (data.size == 1) {

			listOf(data.first().first.toSimpleDate(requireContext()))

		} else {

			listOf(
				data.first().first.toSimpleDate(requireContext()),
				data.last().first.toSimpleDate(requireContext())
			)
		}

		val entries = data.mapIndexed { index, item ->
			val xPosition = if (data.size == 1) {
				0f
			} else {
				index.toFloat() / (data.size - 1).toFloat()
			}

			Entry(xPosition, item.second.toFloat())
		}
		populateChartHelper(entries, xAxisLabels)
	}

	private fun populateChartHelper(
		entries: List<Entry>,
		xAxisLabels: List<String>
	) {

		val context = requireContext()
		val dataSet = LineDataSet(entries, "")
			.apply {

			lineWidth = 3f
			color = ContextCompat.getColor(context, R.color.primaryDark)
			circleRadius = 4f
			mode = LineDataSet.Mode.LINEAR

			setDrawCircles(true)
			setCircleColor(ContextCompat.getColor(context, R.color.primary))
			setDrawCircleHole(false)
			setDrawValues(false)
		}

		binding.chart.apply {

			xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
			data = LineData(dataSet)
			invalidate()
		}
	}

	private fun configChart() {
		val context = requireContext()

		binding.chart.apply {
			description.isEnabled = false
			legend.isEnabled = false

			setDrawGridBackground(false)
			setTouchEnabled(false)
			setBackgroundColor(ContextCompat.getColor(context, R.color.surface))

			xAxis.apply {

				position = XAxis.XAxisPosition.BOTTOM
				axisLineColor = ContextCompat.getColor(context, R.color.primaryDark)
				axisLineWidth = 2f
				textColor = ContextCompat.getColor(context, R.color.primaryDark)
				textSize = 12f
				granularity = 1f
				setDrawGridLines(false)
				setDrawAxisLine(true)
			}

			axisLeft.apply {
				axisMinimum = 0f
				axisMaximum = 10f
				axisLineColor = ContextCompat.getColor(context, R.color.primaryDark)
				axisLineWidth = 2f
				textColor = ContextCompat.getColor(context, R.color.primaryDark)
				textSize = 12f
				setDrawGridLines(false)
				setDrawAxisLine(true)
			}
			axisRight.isEnabled = false
		}
	}
	private fun getPatientUserData() {
		// TODO: implementar a busca dos dados do paciente
		// considerando as permissões médicas.
	}

	private fun manageAllowedProfessionals() {
		val ownerId = patientId.ifBlank {
			AuthManager.uid.orEmpty()
		}

		if (ownerId.isBlank()) {
			showSnackbar(
				getString(R.string.failed_to_identify_patient),
				Utils.SnackBarType.WARNING
			)
			return
		}

		viewLifecycleOwner.lifecycleScope.launch {

			try {
				val professionals = DatabaseManager.db
					.collection(User.COLLECTION_NAME)
					.whereEqualTo("user_type", "profissional")
					.get()
					.await()
					.documents
					.map { doc ->

						User(id = doc.id,
							name = doc.getString("name").orEmpty(),
							email = doc.getString("email").orEmpty(),
							user_type = Utils.UserType.PROFISSIONAL
						)
					}
					.sortedBy { it.name.lowercase() }

				if (professionals.isEmpty()) {
					showSnackbar(
						getString(R.string.no_professional_registered),
						Utils.SnackBarType.INFO
					)
					return@launch
				}

				val reportRef = DatabaseManager.userDocument(Report.COLLECTION_NAME, reportId, ownerId)
				val permissionCollection = DatabaseManager.userCollection(MedicalPermission.COLLECTION_NAME, ownerId)

				val reportSnapshot = reportRef.get().await()
				if (!reportSnapshot.exists()) {
					showSnackbar(
						getString(R.string.report_not_found),
						Utils.SnackBarType.WARNING
					)
					return@launch
				}

				val currentProfessionals = (
					reportSnapshot.get("professionals") as? List<*>
						?: emptyList<Any>()
					).mapNotNull { it as? String }

				val permissionSnapshots =
					permissionCollection.get().await()

				val activePermissions = permissionSnapshots.documents
					.filter { document ->
						document.getString("status")
							?.equals("active", ignoreCase = true) == true
					}
					.mapNotNull { it.getString("professional_id") }.toSet()

				showProfessionalsDialog(
					professionals = professionals,
					currentProfessionals = currentProfessionals,
					activePermissions = activePermissions,
					reportRef = reportRef,
					permissionCollection = permissionCollection
				)

			} catch (e: CancellationException) {
				throw e

			} catch (e: Exception) {
				Log.e("MedicalPermission", getString(R.string.updatepermissions_failure), e)

				if (_binding != null) {

					showSnackbar(
						getString(R.string.failed_to_load_permissions),
						Utils.SnackBarType.WARNING
					)
				}
			}
		}
	}

	private fun showProfessionalsDialog(
		professionals: List<User>,
		currentProfessionals: List<String>,
		activePermissions: Set<String>,
		reportRef: DocumentReference,
		permissionCollection: CollectionReference
	) {

		val names = professionals.map {
			if (it.email.isBlank()) {
				it.name.ifBlank {
					getString(R.string.nameless_professional)
				}

			} else {
				"${it.name} (${it.email})"
			}

		}.toTypedArray()

		val professionalIds = professionals.map {
			it.id
		}

		val selected = professionals.map {
			it.id in currentProfessionals &&
				it.id in activePermissions
		}.toBooleanArray()

		MaterialAlertDialogBuilder(requireContext())

			.setTitle(getString(R.string.allowed_professionals))
			.setMultiChoiceItems(names, selected) { _, index, checked -> selected[index] = checked
			}

			.setNegativeButton(getString(R.string.cancel), null)
			.setPositiveButton(getString(R.string.save)) { _, _ ->

				viewLifecycleOwner.lifecycleScope.launch {

					try {

						val batch = DatabaseManager.db.batch()
						val selectedIds = professionals
							.filterIndexed { index, _ ->
								selected[index]
							}
							.map {
								it.id
							}

						val updatedProfessionals =
							currentProfessionals
								.filter {
									it !in professionalIds
								}.plus(selectedIds).distinct()

						batch.update(
							reportRef,
							"professionals",
							updatedProfessionals
						)

						professionals.forEachIndexed { index, professional ->

							val professionalId = professional.id
							val permissionRef = permissionCollection.document(professionalId)

							if (selected[index]) {
								batch.set(
									permissionRef,
									mapOf(
										"status" to "active",
										"professional_id" to professionalId,
										"granted_at" to FieldValue.serverTimestamp(),
										"revoked_at" to null
									),
									SetOptions.merge()
								)
							} else if (
								professionalId in currentProfessionals ||
								professionalId in activePermissions
							) {
								batch.set(
									permissionRef,
									mapOf(
										"status" to "revoked",
										"professional_id" to professionalId,
										"revoked_at" to FieldValue.serverTimestamp()
									), SetOptions.merge()
								)
							}
						}
						batch.commit().await()

						if (_binding != null) {
							showSnackbar(
								getString(R.string.permissions_updated),
								Utils.SnackBarType.SUCCESS
							)
						}

					} catch (e: CancellationException) {
						throw e

					} catch (e: Exception) {
						Log.e("MedicalPermission", getString(R.string.updatepermissions_failure), e)

						if (_binding != null) {
							showSnackbar(
								getString(R.string.failed_to_update),
								Utils.SnackBarType.WARNING
							)
						}
					}
				}
			}
			.show()
	}

	private fun showConfirmationDialog(title: String, message: String,
		onConfirm: suspend () -> Unit) {

		MaterialAlertDialogBuilder(requireContext())

			.setTitle(title)
			.setMessage(message)
			.setNegativeButton(getString(R.string.cancel), null)
			.setPositiveButton(getString(R.string.delete)) { _, _ ->

				viewLifecycleOwner.lifecycleScope.launch {

					try {
						onConfirm()
						if (_binding != null) { popBackStack() }

					} catch (e: CancellationException) {
						throw e

					} catch (e: Exception) {
						Log.e(
							"confirmation",
							getString(R.string.operation_failure),
							e
						)

						if (_binding != null) {
							showSnackbar(
								getString(R.string.failed_to_delete_report),
								Utils.SnackBarType.WARNING
							)
						}
					}
				}
			}
			.show()
	}

	override fun onDestroyView() {
		binding.recyclerview.adapter = null
		binding.chart.clear()
		renameReport = null
		_binding = null

		super.onDestroyView()
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
