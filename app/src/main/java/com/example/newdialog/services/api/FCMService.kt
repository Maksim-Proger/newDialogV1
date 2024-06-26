package com.example.newdialog.services.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url


interface FCMService {
    @Headers(
        "Content-Type: application/json")

    @POST
    fun sendMessage(@Url url: String, @Body body: FCMMessage): Call<Void>
}

data class FCMMessage(
    val message: Message
)

data class Message(
    val token: String,
    val notification: Notification
)

data class Notification(
    val title: String,
    val body: String
)