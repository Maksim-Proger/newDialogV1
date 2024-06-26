package com.example.newdialog.ui.messageRecyclerView.views

interface MessageView {
    val id: String
    val from: String
    val timeStamp: String
    val fileUrl: String
    val text: String

    fun getTypeView() : Int

    companion object {
        // Переменные отображающие тип View
        val MESSAGE_IMAGE: Int
            get() = 0
        val MESSAGE_TEXT:Int
            get() = 1
        val MESSAGE_VOICE:Int
            get() = 2
        val MESSAGE_FILE:Int
            get() = 3
    }
}