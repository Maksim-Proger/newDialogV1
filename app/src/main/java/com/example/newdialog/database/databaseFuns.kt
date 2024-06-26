package com.example.newdialog.database

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.newdialog.R
import com.example.newdialog.models.CommonModel
import com.example.newdialog.models.User
import com.example.newdialog.services.api.FCMMessageSender
import com.example.newdialog.utilits.APP_ACTIVITY
import com.example.newdialog.utilits.AppValueEventListener
import com.example.newdialog.utilits.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

fun initFirebase() {
    AUTH = FirebaseAuth.getInstance()
    REF_DATABASE_ROOT = FirebaseDatabase.getInstance().reference
    USER = User()
    CURRENT_UID = AUTH.currentUser?.uid.toString()
    REF_STORAGE_ROOT = FirebaseStorage.getInstance().reference
}

// Функция высшего порядка (TODO прочитать что это такое)
// Что такое экстеншен функция (TODO прочитать что это такое)
// Запускаем инициализацию данный пользователя в приоритете, чтобы сначала прогрузились данные,
// а потом уже все остальное.
// TODO прочитать про inline методы и crossinline аргументы
inline fun initUser(crossinline function: () -> Unit) {
    REF_DATABASE_ROOT.child(NODE_USERS).child(CURRENT_UID)
        /**
         * addListenerForSingleValueEvent сработает только один раз при запуске и все
         * если вместо него использовать addValueEventListener то модель USer будет обновляться каждый раз,
         * когда в базу данных будут вноситься какие-то изменения
         * еще есть addChildEventListener этот слушатель следит за изменением какого-то конкретного поля, например UID
         */
        .addListenerForSingleValueEvent(AppValueEventListener {
            USER = it.getValue(User::class.java) ?: User()
            if (USER.username.isEmpty()) {
                USER.username = CURRENT_UID
            }
            function()
        })
}

// TODO читать про лямбды
// TODO разобраться с этим методом, что мы тут делаем?
fun updatePhonesToDatabase(arrayContacts: ArrayList<CommonModel>) {
    // Функция добавляет номер телефона с id в базу данных.
    if (AUTH.currentUser != null) {
        REF_DATABASE_ROOT.child(NODE_PHONES).addListenerForSingleValueEvent(AppValueEventListener {
            it.children.forEach { snapshot ->
                arrayContacts.forEach { contact ->
                    if (snapshot.key == contact.phone) {
                        REF_DATABASE_ROOT.child(NODE_PHONES_CONTACTS).child(CURRENT_UID)
                            .child(snapshot.value.toString()).child(CHILD_ID)
                            .setValue(snapshot.value.toString())
                            .addOnFailureListener {
                                showToast(it.message.toString())
                            }

                        REF_DATABASE_ROOT.child(NODE_PHONES_CONTACTS).child(CURRENT_UID)
                            .child(snapshot.value.toString()).child(CHILD_FULLNAME)
                            .setValue(contact.fullname)
                            .addOnFailureListener {
                                showToast(it.message.toString())
                            }
                    }
                }
            }
        })
    }
}

fun DataSnapshot.getCommonModel(): CommonModel =
    this.getValue(CommonModel::class.java) ?: CommonModel()

fun DataSnapshot.getUserModel(): User =
    this.getValue(User::class.java) ?: User()

fun sendMessage(context: Context, message: String, receivingUserID: String, typeText: String, function: () -> Unit) {
    val refDialogUser = "$NODE_MESSAGES/$CURRENT_UID/$receivingUserID"
    val refDialogReceivingUser = "$NODE_MESSAGES/$receivingUserID/$CURRENT_UID"

    // Получаем ключ нашего уникального сообщения
    val messageKey = REF_DATABASE_ROOT.child(refDialogUser).push().key

    // Получаем fullname отправителя асинхронно
    REF_DATABASE_ROOT.child(NODE_USERS).child(CURRENT_UID).child(CHILD_FULLNAME)
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fullnameUser = snapshot.getValue(String::class.java)
                fullnameUser?.let { fullname ->
                    val mapMessage = hashMapOf<String, Any>()
                    mapMessage[CHILD_FROM] = CURRENT_UID
                    mapMessage[CHILD_TYPE] = typeText
                    mapMessage[CHILD_TEXT] = message
                    mapMessage[CHILD_ID] = messageKey.toString()

                    // Получаем время сервера на данный момент
                    mapMessage[CHILD_TIMESTAMP] = ServerValue.TIMESTAMP

                    val mapDialog = hashMapOf<String, Any>()
                    mapDialog["$refDialogUser/$messageKey"] = mapMessage
                    mapDialog["$refDialogReceivingUser/$messageKey"] = mapMessage

                    // Обновляем данные в базе данных
                    REF_DATABASE_ROOT
                        .updateChildren(mapDialog)
                        .addOnSuccessListener {
                            // Отправляем уведомление с fullname отправителя
                            fullnameUser?.let { fullname ->
                                sendPushNotification(context, receivingUserID, "От: $fullname", message)
                            }
                            function()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                APP_ACTIVITY,
                                "Ошибка отправки сообщения: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("sendMessage", "Failed to fetch fullnameUser: ${error.message}")
            }
        })
}

// Отправляем пуш уведомление
fun sendPushNotification(context: Context, receivingUserID: String, title: String, body: String) {
    getUserFCMToken(receivingUserID) { fcmToken ->
        if (fcmToken.isNotEmpty()) {
            val fcmMessageSender = FCMMessageSender(context)
            fcmMessageSender.sendMessage(fcmToken, title, body)
        }
    }
}


// Получаем FCM Token записанный в базу данных
fun getUserFCMToken(userID: String, callback: (String) -> Unit) {
    REF_DATABASE_ROOT.child(NODE_USERS).child(userID).child(CHILD_FCM_TOKEN).get().addOnSuccessListener {
        val token = it.value.toString()
        callback(token)
    }.addOnFailureListener {
        println("Error getting FCM token: ${it.message}")
        callback("")
    }
}

fun updateCurrentUsername(newUSerNAme: String) {
    REF_DATABASE_ROOT.child(NODE_USERS).child(CURRENT_UID).child(CHILD_USERNAME)
        .setValue(newUSerNAme)
        .addOnCompleteListener {
            if (it.isSuccessful) {
                showToast(APP_ACTIVITY.getString(R.string.toast_data_update))
                deleteOldUsername(newUSerNAme)
            } else {
                showToast(it.exception?.message.toString())
            }
        }
}

private fun deleteOldUsername(newUSerNAme: String) { // при изменении username автоматически удаляется старый
    REF_DATABASE_ROOT.child(NODE_USERNAMES).child(USER.username)
        .removeValue()
        .addOnSuccessListener {
            showToast(APP_ACTIVITY.getString(R.string.toast_data_update))
            APP_ACTIVITY.supportFragmentManager.popBackStack()
            USER.username = newUSerNAme
        }.addOnFailureListener { showToast(it.message.toString()) }
}

fun setBioToDatabase(newBio: String) {
    REF_DATABASE_ROOT.child(NODE_USERS).child(CURRENT_UID).child(CHILD_BIO)
        .setValue(newBio)
        .addOnSuccessListener {
            showToast(APP_ACTIVITY.getString(R.string.toast_data_update))
            USER.bio = newBio
            APP_ACTIVITY.supportFragmentManager.popBackStack()
        }.addOnFailureListener { showToast(it.message.toString()) }
}

fun setNameToDatabase(fullName: String) {
    REF_DATABASE_ROOT.child(NODE_USERS).child(CURRENT_UID).child(CHILD_FULLNAME)
        .setValue(fullName)
        .addOnSuccessListener {
            showToast(APP_ACTIVITY.getString(R.string.toast_data_update))
            USER.fullname = fullName
            APP_ACTIVITY.appDrawer.updateHeader()
            APP_ACTIVITY.supportFragmentManager.popBackStack()
        }.addOnFailureListener { showToast(it.message.toString()) }
}

fun getMessageKey(id: String) = REF_DATABASE_ROOT.child(NODE_MESSAGES).child(CURRENT_UID)
    .child(id).push().key.toString()

inline fun getUrlFromStorage(path: StorageReference, crossinline function: (url: String) -> Unit) {
    /* Функция высшего порядка, получает URL файла из хранилища */
    path.downloadUrl
        .addOnSuccessListener { function(it.toString()) }
        .addOnFailureListener { showToast(it.message.toString()) }
}

inline fun putFileToStorage(uri: Uri, path: StorageReference, crossinline function: () -> Unit) {
    /* Функция высшего порядка, отправляет файл в хранилище */
    path.putFile(uri)
        .addOnSuccessListener { function() }
        .addOnFailureListener { showToast(it.message.toString()) }

}

fun sendMessageAsFile(receivingUserID: String, fileUrl: String, messageKey: String, typeMessage: String, filename: String) {

    val refDialogUser = "$NODE_MESSAGES/$CURRENT_UID/$receivingUserID"
    val refDialogReceivingUser = "$NODE_MESSAGES/$receivingUserID/$CURRENT_UID"

    val mapMessage = hashMapOf<String, Any>()
    mapMessage[CHILD_FROM] = CURRENT_UID
    mapMessage[CHILD_TYPE] = typeMessage
    mapMessage[CHILD_ID] = messageKey
    mapMessage[CHILD_TIMESTAMP] = ServerValue.TIMESTAMP
    mapMessage[CHILD_FILE_URL] = fileUrl
    mapMessage[CHILD_TEXT] = filename

    val mapDialog = hashMapOf<String, Any>()
    mapDialog["$refDialogUser/$messageKey"] = mapMessage
    mapDialog["$refDialogReceivingUser/$messageKey"] = mapMessage

    REF_DATABASE_ROOT
        .updateChildren(mapDialog)
        .addOnFailureListener { showToast(it.message.toString()) }
}

fun uploadFileToStorage(uri: Uri, messageKey: String, receivedID: String, typeMessage: String, filename: String = "") {
    val path = REF_STORAGE_ROOT.child(
        FOLDER_FILES
    ).child(messageKey)
    putFileToStorage(uri, path) {
        getUrlFromStorage(path) {
            sendMessageAsFile(
                receivedID,
                it,
                messageKey,
                typeMessage,
                filename
            )
        }
    }
}

fun getFileFromStorage(mFile: File, fileUrl: String, function: () -> Unit) {
    val path = REF_STORAGE_ROOT.storage.getReferenceFromUrl(fileUrl)
    path.getFile(mFile)
        .addOnSuccessListener { function() }
        .addOnFailureListener{ showToast(it.message.toString()) }
}

fun saveToMainList(id: String, type: String) {
    val refUser = "$NODE_MAIN_LIST/$CURRENT_UID/$id"
    val refReceived = "$NODE_MAIN_LIST/$id/$CURRENT_UID"

    val mapUser = hashMapOf<String, Any>()
    val mapReceived = hashMapOf<String, Any>()

    mapUser[CHILD_ID] = id
    mapUser[CHILD_TYPE] = type

    mapReceived[CHILD_ID] = CURRENT_UID
    mapReceived[CHILD_TYPE] = type

    val commonMap = hashMapOf<String, Any>()
    commonMap[refUser] = mapUser
    commonMap[refReceived] = mapReceived

    REF_DATABASE_ROOT.updateChildren(commonMap)
        .addOnFailureListener { showToast(it.message.toString()) }
}

fun deleteChat(id: String, function: () -> Unit) {
    REF_DATABASE_ROOT.child(NODE_MAIN_LIST).child(CURRENT_UID).child(id).removeValue()
        .addOnFailureListener { showToast(it.message.toString()) }
        .addOnSuccessListener { function() }
}

fun clearChat(id: String, function: () -> Unit) {
    REF_DATABASE_ROOT.child(NODE_MESSAGES).child(CURRENT_UID).child(id)
        .removeValue()
        .addOnFailureListener { showToast(it.message.toString()) }
        .addOnSuccessListener {
            REF_DATABASE_ROOT
                .child(NODE_MESSAGES)
                .child(id)
                .child(CURRENT_UID)
                .removeValue()
                .addOnSuccessListener { function() } // TODO разобраться как работают callback
        }
        .addOnFailureListener { showToast(it.message.toString()) }
}