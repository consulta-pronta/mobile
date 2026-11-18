package com.unnebulous.consultapronta

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.unnebulous.consultapronta.database.AuthManager
import com.unnebulous.consultapronta.database.ExamModel
import com.unnebulous.consultapronta.database.Notification
import com.unnebulous.consultapronta.databinding.FragmentNotificationsBinding
import com.unnebulous.consultapronta.recyclerview.adapter.NotificationsAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime

class Notifications : Fragment() {

	private var _binding: FragmentNotificationsBinding? = null
	private val binding get() = _binding!!

	private lateinit var firestoreListener: ListenerRegistration

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentNotificationsBinding.inflate(
			inflater,
			container,
			false
		)

		return binding.root
	}

	override fun onViewCreated(
		view: View,
		savedInstanceState: Bundle?
	) {
		super.onViewCreated(view, savedInstanceState)

		updateHeader {
			changeHeaderType(Utils.HeaderType.TITLED, true)
			setScreenTitle(getString(R.string.title_notifications_page))
			setGoBackButtonOnClickListener {
				popBackStack()
			}
		}
		resetNavbarEntryActive()

		binding.allAsRead.setOnClickListener { onReadAllClicked() }
		binding.deleteAll.setOnClickListener { onDeleteAllClicked() }

		val adapter = NotificationsAdapter().apply {
			deleteNotification = { notificationid ->
				showConfirmationDialog("Deseja deletar esta notificação?", "Ao confirmar, a notificação será apagada."){
					lifecycleScope.launch {
						Notification.collection
							.document(notificationid)
							.delete()
					}
				}
			}
		}

		firestoreListener = Notification.collection
			.addSnapshotListener { snapshots, exception ->
				if (exception != null) {
					Log.e(Notification.COLLECTION_NAME, "getNotifications", exception)
					return@addSnapshotListener
				}

				if (snapshots != null) {
					val notificationList = snapshots.documents.mapNotNull { Notification.fromDocument(it) }
					if (notificationList.isEmpty()){
						return@addSnapshotListener
					}

					binding.emptyStateContainer.isVisible = false
					binding.cardList.isVisible = true

					adapter.submitList(notificationList)
				} else {
					binding.emptyStateContainer.isVisible = true
					binding.cardList.isVisible = false

					return@addSnapshotListener
				}

			}

		binding.cardList.adapter = adapter
		binding.cardList.addItemDecoration(recyclerviewSpacing)
	}

	private fun onReadAllClicked() {

		showSnackbar(getString(R.string.concluded))

		lifecycleScope.launch {
			Notification.collection.get().await().documents.forEach { documentSnapshot ->
				if (!(documentSnapshot.getBoolean("accessed") ?: true)) {
					documentSnapshot.reference.update("accessed", true)
				}
			}
		}
	}

	private fun onDeleteAllClicked() {
		showConfirmationDialog(
			"Deseja mesmo deletar todas as notificações?",
			"Ao clicar em confirmar, todas as notificações serão apagadas."
		) {
			lifecycleScope.launch {
				Notification.collection.get().await().documents.forEach { documentSnapshot ->
					documentSnapshot.reference.delete()
				}
			}
		}
	}

	private fun showConfirmationDialog(title: String, message: String, onConfirm: () -> Unit) {
		MaterialAlertDialogBuilder(requireContext())
			.setTitle(title)
			.setMessage(message)
			.setNegativeButton(getString(R.string.cancel), null)
			.setPositiveButton(getString(R.string.delete)) { _, _ ->
				onConfirm()
				showSnackbar(getString(R.string.deletion_completed))
			}.show()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
		firestoreListener.remove()
	}
}