package com.example.newdialog.utilits

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

const val READ_CONTACTS = Manifest.permission.READ_CONTACTS
const val RECORD_AUDIO = Manifest.permission.RECORD_AUDIO
const val WRITE_FILES = Manifest.permission.WRITE_EXTERNAL_STORAGE
const val POST_NOTIFICATIONS = Manifest.permission.POST_NOTIFICATIONS

const val PERMISSION_REQUEST = 200

fun checkPermission(permission: String) : Boolean {
    return if (ContextCompat.checkSelfPermission(APP_ACTIVITY, permission) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(APP_ACTIVITY, arrayOf(permission), PERMISSION_REQUEST)
        false
    } else true
}