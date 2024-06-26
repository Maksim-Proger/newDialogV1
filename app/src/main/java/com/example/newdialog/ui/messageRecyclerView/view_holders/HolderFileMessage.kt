package com.example.newdialog.ui.messageRecyclerView.view_holders

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.newdialog.R
import com.example.newdialog.database.CURRENT_UID
import com.example.newdialog.database.getFileFromStorage
import com.example.newdialog.ui.messageRecyclerView.views.MessageView
import com.example.newdialog.utilits.WRITE_FILES
import com.example.newdialog.utilits.asTime
import com.example.newdialog.utilits.checkPermission
import com.example.newdialog.utilits.showToast
import java.io.File

// TODO надо детально разобраться как у нас это получилось

class HolderFileMessage(view: View) : RecyclerView.ViewHolder(view), MessageHolder {
    // Поля
    private val blocUserFileMessage: ConstraintLayout = view.findViewById(R.id.bloc_user_file_message)
    private val chatUserFileMessageTime: TextView = view.findViewById(R.id.chat_user_file_message_time)
    private val chatUserFilename: TextView = view.findViewById(R.id.chat_user_file_name)
    private val chatUserProgressBar: ProgressBar = view.findViewById(R.id.chat_user_progress_bar)

    private val blocReceivedFileMessage: ConstraintLayout = view.findViewById(R.id.bloc_received_file_message)
    private val chatReceivedFileMessageTime: TextView = view.findViewById(R.id.chat_received_file_message_time)
    private val chatReceivedFilename: TextView = view.findViewById(R.id.chat_received_file_name)
    private val chatReceivedProgressBar: ProgressBar = view.findViewById(R.id.chat_received_progress_bar)

    // Кнопки
    private val chatUserBtnDownload: ImageView = view.findViewById(R.id.chat_user_btn_download)
    private val chatReceivedBtnDownload: ImageView = view.findViewById(R.id.chat_received_btn_download)

    override fun drawMessage(view: MessageView) {
        if (view.from == CURRENT_UID) {
            blocUserFileMessage.visibility = View.VISIBLE
            blocReceivedFileMessage.visibility = View.GONE
            chatUserFileMessageTime.text = view.timeStamp.asTime()
            chatUserFilename.text = view.text
        } else {
            blocUserFileMessage.visibility = View.GONE
            blocReceivedFileMessage.visibility = View.VISIBLE
            chatReceivedFileMessageTime.text = view.timeStamp.asTime()
            chatReceivedFilename.text = view.text
        }
    }

    override fun onAttach(view: MessageView) {
        if (view.from == CURRENT_UID) {
            chatUserBtnDownload.setOnClickListener {
                clickToBtnFile(view)
            }
        } else {
            chatReceivedBtnDownload.setOnClickListener {
                clickToBtnFile(view)
            }
        }
    }

    private fun clickToBtnFile(view: MessageView) {
        if (view.from == CURRENT_UID) {
            chatUserBtnDownload.visibility = View.INVISIBLE
            chatUserProgressBar.visibility = View.VISIBLE
        } else {
            chatReceivedBtnDownload.visibility = View.INVISIBLE
            chatReceivedProgressBar.visibility = View.VISIBLE
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || checkPermission(WRITE_FILES)) {
            // Создаем файл через SAF
            createFile(view.text)
        } else {
            // Обновляем UI обратно, если разрешение не было предоставлено
            updateUIAfterPermissionDenied(view)
        }
    }

    private fun createFile(fileName: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        (itemView.context as Activity).startActivityForResult(intent, CREATE_FILE_REQUEST_CODE)
    }

    private fun updateUIAfterPermissionDenied(view: MessageView) {
        if (view.from == CURRENT_UID) {
            chatUserBtnDownload.visibility = View.VISIBLE
            chatUserProgressBar.visibility = View.INVISIBLE
        } else {
            chatReceivedBtnDownload.visibility = View.VISIBLE
            chatReceivedProgressBar.visibility = View.INVISIBLE
        }
    }

    override fun onDetach() {
        chatUserBtnDownload.setOnClickListener(null)
        chatReceivedBtnDownload.setOnClickListener(null)
    }

    companion object {
        private const val CREATE_FILE_REQUEST_CODE = 1002
    }
}