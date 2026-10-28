package com.unnebulous.consultapronta.recyclerview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.database.Professional
import com.unnebulous.consultapronta.databinding.CardProfessionalMedicalPermissionsBinding
import kotlin.jvm.Throws

class MedicalPermissionsAdapter : ListAdapter<Professional, MedicalPermissionsAdapter.MedicalPermissionsViewHolder>(MedicalPermissionComparator()) {

	lateinit var onPermissionChangeClick: (String) -> Unit
	lateinit var checkStatus: (String) -> Boolean?
	lateinit var getPermissionCount: (String) -> Int

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicalPermissionsViewHolder {
		val binding = CardProfessionalMedicalPermissionsBinding.inflate(
			LayoutInflater.from(parent.context),
			parent,
			false
		)

		return MedicalPermissionsViewHolder(binding)
	}

	override fun onBindViewHolder(holder: MedicalPermissionsViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	class MedicalPermissionComparator : DiffUtil.ItemCallback<Professional>() {
		override fun areItemsTheSame(old: Professional, new: Professional): Boolean = old.id == new.id

		override fun areContentsTheSame(old: Professional, new: Professional): Boolean = old == new
	}

	inner class MedicalPermissionsViewHolder(private val binding: CardProfessionalMedicalPermissionsBinding): RecyclerView.ViewHolder(binding.root) {
		fun bind(professional: Professional) {
			binding.apply {
				professionalName.text = professional.name
				val crm = "${professional.crm}/${professional.uf}"
				professionalCrm.text = crm
				professionalEmail.text = professional.email
				professionalPhoneNumber.text = professional.phoneNumber
				professionalPlaceAction.text = professional.placeAction

				changePermissionsButton.setOnClickListener {
					onPermissionChangeClick(professional.id)
				}

				activePermissionsCount.text = ContextCompat.getString(binding.root.context,R.string.number_active_permissions).format(getPermissionCount(professional.id))

				val status = checkStatus(professional.id)
				/* true -> ACTIVE
				* false -> REVOKED
				* null -> PENDING
				* */

				// 0 -> BACKGROUND, 1 -> TEXT
				val colors = Array(2) {0}
				colors.apply {
					set(0, when (status) {
						true -> R.color.success
						false -> R.color.error
						null -> R.color.primaryLight
					})

					set(1, when (status) {
						true, null -> R.color.textDark
						false -> R.color.textLight
					})
				}

				statusPermissionHeader.backgroundTintList = ContextCompat.getColorStateList(binding.root.context, colors[0])
				statusPermission.setTextColor(ContextCompat.getColor(binding.root.context, colors[1]))
				statusPermission.text = ContextCompat.getString(
					binding.root.context,
					when(status) {
						true -> R.string.enabled_permissions
						false -> R.string.disabled_permissions
						null -> R.string.pending_solicitation
					}
				)
			}
		}
	}
}