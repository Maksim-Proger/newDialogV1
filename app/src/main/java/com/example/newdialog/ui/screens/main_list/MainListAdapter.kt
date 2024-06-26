package com.example.newdialog.ui.screens.main_list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.newdialog.R
import com.example.newdialog.models.CommonModel
import com.example.newdialog.ui.screens.single_chat.SingleChatFragment
import com.example.newdialog.utilits.replaceFragment

class MainListAdapter : RecyclerView.Adapter<MainListAdapter.MainListHolder>() {

    private var listItems = mutableListOf<CommonModel>()

    class MainListHolder(view: View): RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.main_list_item_name)
        val itemLastMessage: TextView = view.findViewById(R.id.main_list_item_last_message)
        val itemPhoto: ImageView = view.findViewById(R.id.main_list_item_photo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainListHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.main_list_item, parent, false)

        // Находим наш holder
        val holder = MainListHolder(view)
        // Подключаем слушатель к holder
        holder.itemView.setOnClickListener {
            // Передаем CommonModel в SingleChatFragment через listItems по позиции holder
            replaceFragment(SingleChatFragment(listItems[holder.adapterPosition]))
        }

        return holder
    }

    override fun getItemCount(): Int = listItems.size

    override fun onBindViewHolder(holder: MainListHolder, position: Int) {
        holder.itemName.text = listItems[position].fullname
        holder.itemLastMessage.text = listItems[position].lastMessage
    }

    fun updateListItems(item: CommonModel) {
        listItems.add(item)
        notifyItemInserted(listItems.size) // чтобы у нас добавился самый низ
    }

}