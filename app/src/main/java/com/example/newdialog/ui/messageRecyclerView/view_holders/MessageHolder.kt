package com.example.newdialog.ui.messageRecyclerView.view_holders

import com.example.newdialog.ui.messageRecyclerView.views.MessageView

interface MessageHolder {
    fun drawMessage(view: MessageView)
    fun onAttach(view: MessageView)
    fun onDetach()
}