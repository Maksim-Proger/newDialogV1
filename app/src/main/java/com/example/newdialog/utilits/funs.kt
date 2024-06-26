package com.example.newdialog.utilits

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.provider.OpenableColumns
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.newdialog.MainActivity
import com.example.newdialog.R
import com.example.newdialog.database.updatePhonesToDatabase
import com.example.newdialog.models.CommonModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun showToast(message: String) {
    Toast.makeText(APP_ACTIVITY, message, Toast.LENGTH_SHORT).show()
}

fun restartActivity() {
    val intent = Intent(APP_ACTIVITY, MainActivity::class.java)
    APP_ACTIVITY.startActivity(intent)
    APP_ACTIVITY.finish()
}

fun replaceFragment(fragment: Fragment) {
    APP_ACTIVITY.supportFragmentManager.beginTransaction()
        .addToBackStack(null)
        .replace(R.id.data_container, fragment)
        .commit()
}

// Метод скрытия клавиатуры
fun hideKeyboard() {
    val imm: InputMethodManager = APP_ACTIVITY.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(APP_ACTIVITY.window.decorView.windowToken, 0)
}

fun initContacts() {
    if (checkPermission(READ_CONTACTS)) {
        var arrayContacts = arrayListOf<CommonModel>()
        val cursor = APP_ACTIVITY.contentResolver.query(
            // Cursor - это интерфейс, который используется для чтения и записи данных результата
            // запроса баз данных, в данном случае, данных контактов.

            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.let {
            // let - это функция в Kotlin, которая позволяет выполнить блок кода, если объект,
            // чей метод вызывается, не равен null.

            // Получаем индексы колонок DISPLAY_NAME и NUMBER заранее и проверяем их на -1
            val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                // Если индекс не равен -1, то мы извлекаем значение из курсора, иначе присваиваем null.
                val fullname = if (nameIndex != -1) it.getString(nameIndex) else null
                val phone = if (phoneIndex != -1) it.getString(phoneIndex) else null

                // Проверяем, чтобы fullname и phone не были пустыми перед добавлением модели в arrayContacts.
                if (!fullname.isNullOrEmpty() && !phone.isNullOrEmpty()) {
                    val newModel = CommonModel()
                    newModel.fullname = fullname
                    newModel.phone = phone.replace(Regex("[\\s,-]"), "")
                    arrayContacts.add(newModel)
                }
            }
        }
        cursor?.close()

        updatePhonesToDatabase(arrayContacts)
    }
}

fun String.asTime(): String {
    val time = Date(this.toLong())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return timeFormat.format(time)
}

// Получаем название файла, который собираемся отправить
fun getFileNameFromUri(uri: Uri): String {
    var result = ""
    val cursor = APP_ACTIVITY.contentResolver.query(uri, null, null, null, null)
    cursor?.use { // use блок для автоматического закрытия курсора
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) {
                result = it.getString(nameIndex)
            } else {
                showToast("Column not found")
            }
        }
    } ?: showToast("Cursor is null")
    return result
}

