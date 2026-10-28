package com.unnebulous.consultapronta

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.unnebulous.consultapronta.database.DatabaseManager
import com.unnebulous.consultapronta.database.MedicalPermission
import com.unnebulous.consultapronta.database.Professional
import com.unnebulous.consultapronta.database.User
import com.unnebulous.consultapronta.databinding.BottomSheetBinding
import com.unnebulous.consultapronta.databinding.FragmentPermissoesMedicasBinding
import com.unnebulous.consultapronta.recyclerview.SpacingItemDecoration
import com.unnebulous.consultapronta.recyclerview.adapter.MedicalPermissionsAdapter
import com.unnebulous.consultapronta.showSnackbar
import com.unnebulous.consultapronta.views.DualToggleSelectorView
import com.unnebulous.consultapronta.views.SelectOptionItemView
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.sequences.forEach


const val PERMISSION_MEDICATIONS = "prescribe_medications"
const val PERMISSION_EXAMS = "request_exam"
const val PERMISSION_APPOINTMENTS = "request_appointments"
class PermissoesMedicas : Fragment() {

	private var _binding: FragmentPermissoesMedicasBinding? = null
	private val binding get() = _binding!!

	private val professionalMedicalPermissions = mutableMapOf<String, Map<String, Boolean>>()
	private val professionalMedicalAccessStatus = mutableMapOf<String, Utils.MedicalAccessStatus?>()
	private val medicalPermissions = mutableMapOf<String, MedicalPermission>()
	private lateinit var medicalPermissionsListener: ListenerRegistration

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentPermissoesMedicasBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		updateHeader {
			changeHeaderType(Utils.HeaderType.TITLED, true)
			setScreenTitle(getString(R.string.medical_permissions_title))
			setGoBackButtonOnClickListener {
				popBackStack()
			}
		}
		resetNavbarEntryActive()

		val adapter = MedicalPermissionsAdapter().apply {
			onPermissionChangeClick = { professionalId ->
				configBottomSheet { dialogBinding, dialog ->
					dialogBinding.icon.setImageResource(R.drawable.ic_shield_switch)
					dialogBinding.title.visibility = View.GONE

					dialogBinding.body.apply {
						var isPending = false;

						addView(TextView(requireContext()).apply {
							text = getString(R.string.access_status)
							textSize = 16f
							setTypeface(null, android.graphics.Typeface.BOLD)
							setTextColor(ContextCompat.getColor(requireContext(), R.color.textDark))
							layoutParams = LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.WRAP_CONTENT,
								LinearLayout.LayoutParams.WRAP_CONTENT
							)
						})

						addView(DualToggleSelectorView(requireContext()).apply {
							option = professionalMedicalAccessStatus[professionalId] ?: Utils.MedicalAccessStatus.REVOKED

							layoutParams = LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.WRAP_CONTENT,
								LinearLayout.LayoutParams.WRAP_CONTENT
							).apply {
								setHorizontalGravity(Gravity.CENTER_HORIZONTAL)
							}

							setFirstOptionText(getString(R.string.access_status_active))
							setSecondOptionText(getString(R.string.access_status_revoked))
							recalcUi()
							setOnClickListener {
								professionalMedicalAccessStatus[professionalId] = switch<Utils.MedicalAccessStatus>()
							}

							if (professionalMedicalAccessStatus[professionalId] == null) {
								blockClick()
								isPending = true
							}
						})

						if (isPending) {
							addView(TextView(requireContext()).apply {
								text = getString(R.string.pending_status_message)
								textSize = 16f
								setTypeface(null, android.graphics.Typeface.BOLD)
								setTextColor(ContextCompat.getColor(requireContext(), R.color.error))
								layoutParams = LinearLayout.LayoutParams(
									LinearLayout.LayoutParams.WRAP_CONTENT,
									LinearLayout.LayoutParams.WRAP_CONTENT
								)
							})
						}

						addView(TextView(requireContext()).apply {
							text = getString(R.string.permissions_label)
							textSize = 16f
							setTypeface(null, android.graphics.Typeface.BOLD)
							setTextColor(ContextCompat.getColor(requireContext(), R.color.textDark))
							layoutParams = LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.WRAP_CONTENT,
								LinearLayout.LayoutParams.WRAP_CONTENT
							)
						})

						val permissions = mapOf(
							PERMISSION_MEDICATIONS to getString(R.string.permission_prescribe_medications),
							PERMISSION_EXAMS to getString(R.string.permission_request_exams),
							PERMISSION_APPOINTMENTS to getString(R.string.permission_request_consultations)
						)

						for ((key, title) in permissions) {
							val option = SelectOptionItemView(
								requireContext(),
								type = Utils.SelectOptionItemType.CHECKBOX
							).apply {
								itemId = key
								setTitle(title)
								checked = professionalMedicalPermissions[professionalId]?.get(key) ?: false
							}
							addView(option)
						}
					}

					dialogBinding.positiveButton.apply {
						val permissionsGranted = mutableMapOf(
							PERMISSION_MEDICATIONS to false,
							PERMISSION_EXAMS to false,
							PERMISSION_APPOINTMENTS to false
						)

						text = getString(R.string.confirm_changes)
						setOnClickListener {
							for ((optionId, isChecked) in catchOptionsSelected(dialogBinding)) {
								permissionsGranted[optionId] = isChecked
							}

							lifecycleScope.launch {
								dialogBinding.positiveButton.isEnabled = false

								try {
									val medicalPermission = medicalPermissions[professionalId]!!
									val updatedMedicalPermission =
										MedicalPermission.Companion.FormData(
											professionalMedicalAccessStatus[professionalId],
											permissionsGranted,
											medicalPermission.professional_id,
											medicalPermission.created_at
									).apply {
										val actualMedicalAccessStatus = this.status
										if (actualMedicalAccessStatus != medicalPermission.status) {
											when (actualMedicalAccessStatus) {
												Utils.MedicalAccessStatus.ACTIVE -> {
													this.granted_at = FieldValue.serverTimestamp()
													this.revoked_at = null
												}

												Utils.MedicalAccessStatus.REVOKED -> {
													this.granted_at = null
													this.revoked_at = FieldValue.serverTimestamp()
												}

												else -> {
													this.granted_at = null
													this.revoked_at = null
												}
											}
										} else {
											this.granted_at = medicalPermission.granted_at
											this.revoked_at = medicalPermission.revoked_at
										}
									}

									MedicalPermission
										.collection
										.document(medicalPermission.id)
										.set(updatedMedicalPermission.toMap())

									dialog.dismiss()
									showSnackbar(getString(R.string.success_updating_medical_permissions), Utils.SnackBarType.SUCCESS)
								} catch (e: Exception) {
									Log.e(MedicalPermission.COLLECTION_NAME, "submitMedicalPermission:failure", e)
									showSnackbar(getString(R.string.error_not_updating_medical_permissions), Utils.SnackBarType.DANGER)
								}

								dialogBinding.positiveButton.isEnabled = true
							}
						}
					}
				}
			}

			getPermissionCount = { professionalId ->
				professionalMedicalPermissions[professionalId]?.filter {
					it.value
				}?.count() ?: 0
			}

			checkStatus = { professionalId ->
				when (professionalMedicalAccessStatus[professionalId]) {
					Utils.MedicalAccessStatus.ACTIVE -> true
					Utils.MedicalAccessStatus.REVOKED -> false
					null -> null
				}
			}
		}

		binding.recyclerview.adapter = adapter
		binding.recyclerview.addItemDecoration(SpacingItemDecoration(recyclerviewSpacing))

		medicalPermissionsListener = MedicalPermission
			.collection
			.addSnapshotListener { snapshots, exception ->
				if (exception != null) {
					Log.e(MedicalPermission.COLLECTION_NAME, "getMedicalPermissionsListener:failure", exception)
					return@addSnapshotListener
				}
				if (snapshots == null) {
					return@addSnapshotListener
				}

				lifecycleScope.launch {
					adapter.submitList(getProfessionals(snapshots))
				}
			}
	}

	private fun catchOptionsSelected(dialog: BottomSheetBinding): Map<String, Boolean> {
		val values = mutableMapOf<String, Boolean>()

		dialog.body.children.forEach { view ->
			if (view is SelectOptionItemView) {
				values[view.itemId] = view.checked
			}
		}

		return values
	}

	private suspend fun getProfessionals(medicalPermissionCollection: QuerySnapshot): List<Professional> {
		val professionals = mutableListOf<Professional>()

		medicalPermissionCollection.forEach { doc ->
			val professional = DatabaseManager
				.db
				.collection(User.COLLECTION_NAME)
				.document(doc.getString("professional_id")!!)
				.get()
				.await()

			professionalMedicalPermissions[professional.id] = mapOf(
				PERMISSION_MEDICATIONS to (doc.getBoolean("permissions.$PERMISSION_MEDICATIONS") ?: false),
				PERMISSION_EXAMS to (doc.getBoolean("permissions.$PERMISSION_EXAMS") ?: false),
				PERMISSION_APPOINTMENTS to (doc.getBoolean("permissions.$PERMISSION_APPOINTMENTS") ?: false)
			)

			professionalMedicalAccessStatus[professional.id] = doc.getEnum<Utils.MedicalAccessStatus>("status")
			medicalPermissions[professional.id] = MedicalPermission.fromDocument(doc)

			professionals.add(
				Professional(
					doc.id,
					professional.getString("name"),
					professional.getString("professional_data.crm"),
					professional.getString("professional_data.uf"),
					professional.getString("email"),
					professional.getString("phone"),
					professional.getString("professional_data.local_atuacao"),
				)
			)
		}

		return professionals
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
		medicalPermissionsListener.remove()
	}
}