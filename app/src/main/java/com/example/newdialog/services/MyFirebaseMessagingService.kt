package com.example.newdialog.services

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.newdialog.R
import com.example.newdialog.utilits.APP_ACTIVITY
import com.example.newdialog.utilits.AppMain
import com.example.newdialog.utilits.POST_NOTIFICATIONS
import com.example.newdialog.utilits.READ_CONTACTS
import com.example.newdialog.utilits.initContacts
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Обработка входящих сообщений
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
            // Разрешение не предоставлено
            return
        }

        // Обработка входящих сообщений
        val notification = NotificationCompat.Builder(this, AppMain.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_message)
            .setContentTitle(message.notification?.title ?: "Новое сообщение")
            .setContentText(message.notification?.body ?: "У вас новое сообщение")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(Random.nextInt(), notification)
    }

    // Метод обновления токена на сервере
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO сюда можно перенести логику получения токена
    }

}