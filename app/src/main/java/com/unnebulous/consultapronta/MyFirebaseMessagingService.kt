
package com.unnebulous.consultapronta

// PARA USO FUTURO/NO MOMENTO NÃO USADO
//class MyFirebaseMessagingService : FirebaseMessagingService() {
//
//	override fun onMessageReceived(message: RemoteMessage) {
//		super.onMessageReceived(message)
//
//		val title = message.notification?.title
//			?: message.data["title"]
//			?: "ConsultaPronta"
//
//		val description = message.notification?.body
//			?: message.data["description"]
//			?: "Você possui uma nova notificação."
//
//		showNotification(title, description)
//	}
//
//	private fun showNotification(title: String, description: String) {
//
//		val channelId = "consultapronta_notifications"
//
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//			val channel = NotificationChannel(
//				channelId,
//				"Notificações do ConsultaPronta",
//				NotificationManager.IMPORTANCE_DEFAULT
//			)
//
//			val manager = getSystemService(NotificationManager::class.java)
//			manager.createNotificationChannel(channel)
//		}
//
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
//			ContextCompat.checkSelfPermission(
//				this,
//				Manifest.permission.POST_NOTIFICATIONS
//			) != PackageManager.PERMISSION_GRANTED
//		) {
//			return
//		}
//
//		val notification = NotificationCompat.Builder(this, channelId)
//			.setSmallIcon(R.drawable.ic_notification)
//			.setContentTitle(title)
//			.setContentText(description)
//			.setStyle(
//				NotificationCompat.BigTextStyle()
//					.bigText(description)
//			)
//			.setPriority(NotificationCompat.PRIORITY_DEFAULT)
//			.setAutoCancel(true)
//			.build()
//
//		NotificationManagerCompat.from(this)
//			.notify(System.currentTimeMillis().toInt(), notification)
//	}
//
//	// Atualiza o token quando o Firebase gerar um novo
//	override fun onNewToken(token: String) {
//		super.onNewToken(token)
//
//		if (AuthManager.uid != null) {
//			DatabaseManager.userCollection("fcmTokens")
//				.document(token)
//				.set(mapOf("token" to token))
//		}
//	}
//}
