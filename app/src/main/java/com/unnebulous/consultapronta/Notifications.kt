package com.unnebulous.consultapronta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.unnebulous.consultapronta.database.Notification
import com.unnebulous.consultapronta.databinding.FragmentNotificationsBinding
import com.unnebulous.consultapronta.recyclerview.adapter.NotificationsAdapter

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
            Notification("Puta"),
            Notification("Putas"),
            Notification("Putao"),
            Notification("Putaaa"),
            Notification("Putaa"),
            Notification("Putin")
        ))
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}