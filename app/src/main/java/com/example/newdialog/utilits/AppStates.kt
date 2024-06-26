package com.example.newdialog.utilits

import android.widget.Toast
import com.example.newdialog.database.AUTH
import com.example.newdialog.database.CHILD_STATE
import com.example.newdialog.database.CURRENT_UID
import com.example.newdialog.database.NODE_USERS
import com.example.newdialog.database.REF_DATABASE_ROOT
import com.example.newdialog.database.USER

enum class AppStates(val state: String) {
    ONLINE("в сети"),
    OFFLINE("был недавно"),
    TYPING("печатает");

    // Для того чтобы мы в приложении могли обновлять эти состояния, но при этом не создавать
    // объекты этого класса. Для решения этой задачи будем использовать companion object
    companion object {

        // метод записывает в базу состояние, которое в нее будет передано
        fun updateState(appStates: AppStates) {
            // проверка нужна, чтобы запись в базу осуществлялась только тогда, когда пользователь авторизовался
            if (AUTH.currentUser != null) {
                REF_DATABASE_ROOT.child(NODE_USERS).child(CURRENT_UID).child(CHILD_STATE)
                    .setValue(appStates.state)
                    .addOnSuccessListener { // TODO прочитать про этот слушатель
                        USER.state = appStates.state
                    }
                    .addOnFailureListener { // TODO прочитать про этот слушатель
                        Toast.makeText(APP_ACTIVITY, it.message.toString(), Toast.LENGTH_SHORT).show() // Вроде правильно!
                    }
            }
        }
    }

}