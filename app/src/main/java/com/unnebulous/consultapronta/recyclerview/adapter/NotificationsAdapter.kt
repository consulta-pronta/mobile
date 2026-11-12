package com.unnebulous.consultapronta.recyclerview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unnebulous.consultapronta.R
import com.unnebulous.consultapronta.database.Notification
import com.unnebulous.consultapronta.databinding.CardNotificationsBinding
import com.unnebulous.consultapronta.toBrazillianFullDateAndTime
import com.unnebulous.consultapronta.toLocalDateTime

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
		holder.bind(getItem(position),)
	}

	inner class NotificationsViewHolder(
		private val binding: CardNotificationsBinding
	): RecyclerView.ViewHolder(binding.root){
		fun bind(notification: Notification){
			binding.apply {
				val textColor = if (notification.accessed){
					R.color.primaryLight
				} else {
					R.color.primaryDark
				}
				val cardColor = if (notification.accessed){
					R.color.primary
				} else {
					R.color.surface
				}
				notificationSubject.text = notification.title
				notificationContent.text = notification.description
				notificationDateText.text = notification.created_at?.toLocalDateTime()?.toBrazillianFullDateAndTime()

				notificationDateText.apply {
					setTextColor(ContextCompat.getColor(root.context, textColor))
					compoundDrawableTintList = (ContextCompat.getColorStateList(root.context, textColor))
				}
				notificationContent.setTextColor(ContextCompat.getColor(root.context, textColor))
				notificationSubject.setTextColor(ContextCompat.getColor(root.context, textColor))
				deleteNotification.imageTintList = ContextCompat.getColorStateList(root.context, textColor)
				root.backgroundTintList = ContextCompat.getColorStateList(root.context, cardColor)
			}
		}
	}

	class NotificationComparator : DiffUtil.ItemCallback<Notification>() {
		override fun areItemsTheSame(old: Notification, new: Notification) = old.id == new.id
		override fun areContentsTheSame(old: Notification, new: Notification) = old == new
	}
}