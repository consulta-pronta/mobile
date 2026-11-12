package com.unnebulous.consultapronta

import android.content.Intent
import android.os.Bundle
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
import com.unnebulous.consultapronta.database.AuthManager
import com.unnebulous.consultapronta.database.Notification
import com.unnebulous.consultapronta.databinding.FragmentNotificationsBinding
import com.unnebulous.consultapronta.recyclerview.adapter.NotificationsAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime

class Notifications : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

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

        binding.selectAll.setOnClickListener { onSelectAllClicked() }
        binding.reload.setOnClickListener {  }

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
        binding.cardList.adapter = adapter
        binding.cardList.addItemDecoration(recyclerviewSpacing)

        adapter.submitList(listOf(
            Notification(
                "Puta",
                "Maria",
                "Lindoma",
                LocalDateTime.now().toFirestoreTimestamp(),
                false,
                Utils.NotificationType.ALTERACAO,
                Utils.NotificationScope.MEDICAMENTO,
            ),
            Notification(
                "Puto",
                "Mario",
                "Lindomo",
                LocalDateTime.now().toFirestoreTimestamp(),
                true,
                Utils.NotificationType.ALTERACAO,
                Utils.NotificationScope.MEDICAMENTO,
            ),
            Notification(
                "Putin",
                "Marin",
                "Lindomin",
                LocalDateTime.now().toFirestoreTimestamp(),
                false,
                Utils.NotificationType.ALTERACAO,
                Utils.NotificationScope.MEDICAMENTO,
            ),

        ))
    }

    private fun onSelectAllClicked() {
        val parentActivitiy = requireActivity()

        configBottomSheet { dialogBinding, _ ->
            dialogBinding.apply {
                icon.setImageResource(R.drawable.ic_info)
                icon.visibility = View.GONE
                title.text = getString(R.string.select_option)

                body.apply {
                    val asRead = Button(parentActivitiy).apply {
                        text = getString(R.string.as_read_text)
                        textAlignment = View.TEXT_ALIGNMENT_CENTER
                        setTextColor(ContextCompat.getColor(context, R.color.textDark))
                        setCompoundDrawablesRelativeWithIntrinsicBounds(
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_email),
                            null,
                            null,
                            null
                        )
                    }
                    val deleteAll = Button(parentActivitiy).apply {
                        text = getString(R.string.delete)
                        textAlignment = View.TEXT_ALIGNMENT_CENTER
                        setTextColor(ContextCompat.getColor(context, R.color.textDark))
                        setCompoundDrawablesRelativeWithIntrinsicBounds(
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_bin),
                            null,
                            null,
                            null
                        )
                        setOnClickListener { showConfirmationDialog( "Deseja mesmo deletar todas as notificações?" , "Ao clicar em confirmar, todas as notificações serão apagadas." ) {
                            lifecycleScope.launch {
                                // todo: apagar a coleção
                            }
                        }
                        }
                    }
                    addView(asRead)
                    addView(deleteAll)
                }

                positiveButton.isVisible = false
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
                Snackbar.make(
                    binding.root,
                    getString(R.string.deletion_completed),
                    Snackbar.LENGTH_SHORT
                ).show()
            }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}