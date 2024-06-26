package com.example.newdialog.services.api

import android.content.Context
import com.example.newdialog.services.google_auth_helper.GoogleAuthHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FCMMessageSender(private val context: Context) {

    private val projectId = "new-dialog-6fac4"

    fun sendMessage(fcmToken: String, title: String, body: String) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val authToken = GoogleAuthHelper.getAccessToken(context)

                val service = RetrofitClient.getRetrofitInstance(authToken).create(FCMService::class.java)

                val fcmMessage = FCMMessage(
                    message = Message(
                        token = fcmToken,
                        notification = Notification(
                            title = title,
                            body = body
                        )
                    )
                )

                val url = "https://fcm.googleapis.com/v1/projects/$projectId/messages:send"

                val call = service.sendMessage(url, fcmMessage)
                call.enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            println("FCM Message sent successfully")
                        } else {
                            println("FCM Message failed to send: ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        t.printStackTrace()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}