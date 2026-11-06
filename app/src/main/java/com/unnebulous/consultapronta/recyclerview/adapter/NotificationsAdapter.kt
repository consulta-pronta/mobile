package com.unnebulous.consultapronta.recyclerview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unnebulous.consultapronta.database.Notification
import com.unnebulous.consultapronta.databinding.CardNotificationsBinding

class NotificationsAdapter: ListAdapter<Notification, NotificationsAdapter.NotificationsViewHolder>(NotificationComparator()) {
	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int
	): NotificationsViewHolder {
		val binding = CardNotificationsBinding.inflate(
			LayoutInflater.from(parent.context),
			parent,
			false
		)
		return NotificationsViewHolder(binding)
	}

	override fun onBindViewHolder(holder: NotificationsViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	inner class NotificationsViewHolder(
		private val binding: CardNotificationsBinding
	): RecyclerView.ViewHolder(binding.root){
		fun bind(notification: Notification){
			binding.apply {}
		}
	}

	class NotificationComparator : DiffUtil.ItemCallback<Notification>() {
		override fun areItemsTheSame(old: Notification, new: Notification) = old.id == new.id
		override fun areContentsTheSame(old: Notification, new: Notification) = old == new
	}
}