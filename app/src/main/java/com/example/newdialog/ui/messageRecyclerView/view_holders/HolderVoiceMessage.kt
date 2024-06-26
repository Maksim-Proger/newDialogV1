package com.example.newdialog.ui.messageRecyclerView.view_holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.newdialog.R
import com.example.newdialog.database.CURRENT_UID
import com.example.newdialog.ui.messageRecyclerView.views.MessageView
import com.example.newdialog.utilits.AppVoicePlayer
import com.example.newdialog.utilits.asTime

class HolderVoiceMessage(view:View): RecyclerView.ViewHolder(view), MessageHolder {

    private val mAppVoicePlayer = AppVoicePlayer()

    // Поля
    private val blockUserVoiceMassage: ConstraintLayout = view.findViewById(R.id.bloc_user_voice_message)
    private val chatUserVoiceMessageTime: TextView = view.findViewById(R.id.chat_user_voice_message_time)

    private val blocReceivedVoiceMessage: ConstraintLayout = view.findViewById(R.id.bloc_received_voice_message)
    private val chatReceivedVoiceMessageTime: TextView = view.findViewById(R.id.chat_received_voice_message_time)

    // Кнопки
    private val chatReceivedBtnPlay: ImageView = view.findViewById(R.id.chat_received_btn_play)
    private val chatReceivedBtnStop: ImageView = view.findViewById(R.id.chat_received_btn_stop)

    private val chatUserBtnPlay: ImageView = view.findViewById(R.id.chat_user_btn_play)
    private val chatUserBtnStop: ImageView = view.findViewById(R.id.chat_user_btn_stop)


    override fun drawMessage(view: MessageView) {
        if (view.from == CURRENT_UID) {
            blockUserVoiceMassage.visibility = View.VISIBLE
            blocReceivedVoiceMessage.visibility = View.GONE
            chatUserVoiceMessageTime.text = view.timeStamp.asTime()
        } else {
            blockUserVoiceMassage.visibility = View.GONE
            blocReceivedVoiceMessage.visibility = View.VISIBLE
            chatReceivedVoiceMessageTime.text = view.timeStamp.asTime()
        }
    }

    override fun onAttach(view: MessageView) {

        mAppVoicePlayer.init()

        // проверяем от кого этот holder
        if (view.from == CURRENT_UID) {
            chatUserBtnPlay.setOnClickListener {
                chatUserBtnPlay.visibility = View.GONE
                chatUserBtnStop.visibility = View.VISIBLE
                chatUserBtnStop.setOnClickListener {
                    stop {
                        chatUserBtnStop.setOnClickListener(null)
                        chatUserBtnPlay.visibility = View.VISIBLE
                        chatUserBtnStop.visibility = View.GONE
                    }
                }

                play(view) { // заканчивается проигрывание
                    chatUserBtnPlay.visibility = View.VISIBLE
                    chatUserBtnStop.visibility = View.GONE
                }
            }
        } else {
            chatReceivedBtnPlay.setOnClickListener {
                chatReceivedBtnPlay.visibility = View.GONE
                chatReceivedBtnStop.visibility = View.VISIBLE
                chatReceivedBtnStop.setOnClickListener {
                    stop {
                        chatReceivedBtnStop.setOnClickListener(null)
                        chatReceivedBtnPlay.visibility = View.VISIBLE
                        chatReceivedBtnStop.visibility = View.GONE
                    }
                }

                play(view) { // заканчивается проигрывание
                    chatReceivedBtnPlay.visibility = View.VISIBLE
                    chatReceivedBtnStop.visibility = View.GONE
                }
            }
        }
    }

    private fun stop(function: () -> Unit) {
        mAppVoicePlayer.stop {
            function()
        }
    }

    private fun play(view: MessageView, function: () -> Unit) {
        mAppVoicePlayer.play(view.id, view.fileUrl) {
            function()
        }
    }

    override fun onDetach() {
        // Удаляем слушатели
        chatUserBtnPlay.setOnClickListener(null)
        chatReceivedBtnPlay.setOnClickListener(null)
        mAppVoicePlayer.release()
    }
}