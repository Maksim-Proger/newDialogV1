package com.example.newdialog.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Обработка входящих сообщений
        val notification = remoteMessage.notification
        notification?.let {
            // TODO доработать логику показа уведомлений
        }
    }

    // Метод обновления токена на сервере
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO сюда можно перенести логику получения токена
    }

}