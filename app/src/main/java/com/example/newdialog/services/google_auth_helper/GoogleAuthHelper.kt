package com.example.newdialog.services.google_auth_helper

import android.content.Context
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object GoogleAuthHelper {
    private const val SCOPES = "https://www.googleapis.com/auth/firebase.messaging"

    // Используем suspend функцию для работы с корутинами
    @Throws(IOException::class)
    suspend fun getAccessToken(context: Context): String {
        return withContext(Dispatchers.IO) {
            val assetManager = context.assets
            val inputStream = assetManager.open("serviceAccount.json")
            val googleCredentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(listOf(SCOPES))
            googleCredentials.refreshIfExpired()
            googleCredentials.accessToken.tokenValue
        }
    }
}

