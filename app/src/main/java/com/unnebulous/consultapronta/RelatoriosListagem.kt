package com.unnebulous.consultapronta

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ListenerRegistration
import com.unnebulous.consultapronta.database.AuthManager
import com.unnebulous.consultapronta.database.DatabaseManager
import com.unnebulous.consultapronta.database.Report
import com.unnebulous.consultapronta.databinding.FragmentRelatoriosListagemBinding
import com.unnebulous.consultapronta.recyclerview.adapter.ReportListAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RelatoriosListagem : Fragment() {

	private var _binding: FragmentRelatoriosListagemBinding? = null
	private val binding get() = _binding!!

	private var reportListener: ListenerRegistration? = null
	private var isPatient = false

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentRelatoriosListagemBinding.inflate(
			inflater,
			container,
			false
		)
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

		viewLifecycleOwner.lifecycleScope.launch {
			try {
				val user = AuthManager.getUserData()
					?: throw Exception(getString(R.string.user_not_found))

				isPatient = user.user_type == Utils.UserType.PACIENTE

				binding.addNewReport.visibility =
					if (isPatient) View.VISIBLE else View.GONE

				binding.recyclerview.adapter = ReportListAdapter().apply {
					onClick = { report ->
						changeFragmentWithBackStack(
							VisualizarRelatorio.newInstance(
								report.id,
								report.owner_id
							)
						)
					}

					renameReport = { report ->
						if (isPatient) {
							showRenameDialog(report)
						}
					}

					deleteReport = { report ->
						if (isPatient) {
							showConfirmationDialog(
								getString(R.string.wish_to_delete_report),
								getString(R.string.on_confirm_delete_report)
							) {
								DatabaseManager.userDocument(
									Report.COLLECTION_NAME,
									report.id,
									report.owner_id
								).delete().await()
							}
						}
					}

					allowedProfessionals = { report ->
						showAllowedProfessionals(report)
					}

					userIsProfessional = !isPatient
				}

				binding.recyclerview.addItemDecoration(recyclerviewSpacing)

				binding.addNewReport.setOnClickListener {
					if (isPatient) {
						changeFragmentWithBackStack(GerarRelatorio())
					}
				}

				loadReports()

			} catch (e: Exception) {
				Log.e("report", getString(R.string.onviewcreated_failure), e)

				showSnackbar(
					getString(R.string.error_to_load_reports),
					Utils.SnackBarType.WARNING
				)
			}
		}
	}


	private fun loadReports() {
		viewLifecycleOwner.lifecycleScope.launch {
			try {
				val user = AuthManager.getUserData()
					?: throw Exception(getString(R.string.user_not_found))

				val uid = AuthManager.uid
					?: throw Exception(getString(R.string.user_not_authenticated))

				isPatient = user.user_type == Utils.UserType.PACIENTE

				binding.addNewReport.visibility =
					if (isPatient) View.VISIBLE else View.GONE

				reportListener?.remove()

				val query = if (isPatient) {
					DatabaseManager.userCollection(
						Report.COLLECTION_NAME,
						uid
					)
				} else {
					DatabaseManager.db
						.collectionGroup(Report.COLLECTION_NAME)
						.whereArrayContains("professionals", uid)
				}

				reportListener = query.addSnapshotListener { snapshots, error ->

					if (error != null) {
						Log.e(
							"report",
							getString(R.string.getreportslistener_failure),
							error
						)
						return@addSnapshotListener
					}

					if (snapshots == null) return@addSnapshotListener

					val reports = snapshots.documents.mapNotNull {
						Report.fromDocument(it)
					}

					(binding.recyclerview.adapter as? ReportListAdapter)
						?.submitList(
							reports.sortedByDescending {
								it.created_at?.toDate()?.time ?: 0L
							}
						)
				}

			} catch (e: Exception) {
				Log.e("report", getString(R.string.loadreports_failure), e)

				if (isAdded) {
					showSnackbar(
						getString(R.string.error_to_load_reports),
						Utils.SnackBarType.WARNING
					)
				}
			}
		}
	}

	private fun showRenameDialog(report: Report) {
		configBottomSheet { dialogBinding, dialog ->

			dialogBinding.icon.visibility = View.GONE
			dialogBinding.title.text = getString(R.string.rename_report)

			val input = EditText(requireContext()).apply {
				layoutParams = ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT
				)

				inputType = InputType.TYPE_CLASS_TEXT
				background = ContextCompat.getDrawable(
					context,
					R.drawable.shape_input
				)

				setCompoundDrawablesRelativeWithIntrinsicBounds(
					ContextCompat.getDrawable(
						context,
						R.drawable.ic_document
					),
					null,
					null,
					null
				)

				compoundDrawablePadding = 20
				hint = getString(R.string.type_new_name)
				setText(report.title)

				setTextColor(ContextCompat.getColor(context, R.color.textLight))

				setHintTextColor(ContextCompat.getColor(context, R.color.textLight60))

				compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.textLight))
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
							DatabaseManager.userDocument(
								Report.COLLECTION_NAME,
								report.id,
								report.owner_id
							).update("title", newTitle).await()

							dialog.dismiss()

						} catch (e: Exception) {
							Log.e(
								"report",
								context.getString(R.string.renamereport_failure),
								e
							)

							showSnackbar(
								context.getString(R.string.failed_to_rename_report),
								Utils.SnackBarType.WARNING
							)
						}
					}
				}
			}
		}
	}

	private fun showConfirmationDialog(
		title: String,
		message: String,
		onConfirm: suspend () -> Unit
	) {
		MaterialAlertDialogBuilder(requireContext())
			.setTitle(title)
			.setMessage(message)
			.setNegativeButton(getString(R.string.cancel), null)
			.setPositiveButton(getString(R.string.delete)) { _, _ ->

				viewLifecycleOwner.lifecycleScope.launch {
					try {
						onConfirm()

						showSnackbar(
							getString(R.string.report_deleted),
							Utils.SnackBarType.SUCCESS
						)

					} catch (e: Exception) {
						Log.e(
							"report",
							getString(R.string.deletereport_failure),
							e
						)

						showSnackbar(
							getString(R.string.failed_to_delete_report),
							Utils.SnackBarType.WARNING
						)
					}
				}
			}
			.show()
	}

	private fun showAllowedProfessionals(report: Report) {
		viewLifecycleOwner.lifecycleScope.launch {
			try {
				if (report.professionals.isEmpty()) {
					showSnackbar(
						getString(R.string.no_professional_allowed),
						Utils.SnackBarType.INFO
					)
					return@launch
				}

				val professionals = report.professionals.mapNotNull { professionalId ->
					val document = DatabaseManager.db
						.collection("users")
						.document(professionalId)
						.get()
						.await()

					if (!document.exists()) {
						null
					} else {
						document.getString("name")
							?.ifBlank { getString(R.string.nameless_professional) }
							?: getString(R.string.nameless_professional)
					}
				}

				if (professionals.isEmpty()) {
					showSnackbar(
						getString(R.string.failed_to_find_professionals),
						Utils.SnackBarType.INFO
					)
					return@launch
				}

				MaterialAlertDialogBuilder(requireContext())
					.setTitle(getString(R.string.allowed_professionals))
					.setItems(professionals.toTypedArray(), null)
					.setPositiveButton(getString(R.string.close), null)
					.show()

			} catch (e: Exception) {
				Log.e(
					"MedicalPermission",
					getString(R.string.getallowedprofessionals_failure),
					e
				)

				showSnackbar(
					getString(R.string.failed_to_load_allowed_professionals),
					Utils.SnackBarType.WARNING
				)
			}
		}
	}

	override fun onDestroyView() {
		reportListener?.remove()
		reportListener = null
		_binding = null
		super.onDestroyView()
	}
}