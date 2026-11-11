package com.unnebulous.consultapronta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.unnebulous.consultapronta.database.Notification
import com.unnebulous.consultapronta.databinding.FragmentNotificationsBinding
import com.unnebulous.consultapronta.recyclerview.adapter.NotificationsAdapter
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

        val adapter = NotificationsAdapter()
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}