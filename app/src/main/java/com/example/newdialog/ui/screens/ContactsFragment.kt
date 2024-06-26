package com.example.newdialog.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.newdialog.R
import com.example.newdialog.databinding.FragmentContactsBinding
import com.example.newdialog.models.CommonModel
import com.example.newdialog.ui.screens.single_chat.SingleChatFragment
import com.example.newdialog.utilits.APP_ACTIVITY
import com.example.newdialog.utilits.AppValueEventListener
import com.example.newdialog.database.CURRENT_UID
import com.example.newdialog.database.NODE_PHONES_CONTACTS
import com.example.newdialog.database.NODE_USERS
import com.example.newdialog.database.REF_DATABASE_ROOT
import com.example.newdialog.database.getCommonModel
import com.example.newdialog.utilits.replaceFragment
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference

class ContactsFragment : BaseFragment() {

    private var _binding : FragmentContactsBinding? = null
    private val binding get() = _binding!!

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: FirebaseRecyclerAdapter<CommonModel, ContactsHolder>
    private lateinit var mRefContacts: DatabaseReference
    private lateinit var mRefUsers: DatabaseReference

    // Специальные переменные для закрытия слушателя (чтобы не было уточки памяти)
    private lateinit var mRefUsersListener: AppValueEventListener
    private var mapListeners = hashMapOf<DatabaseReference, AppValueEventListener>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentContactsBinding.inflate(inflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initRecyclerView()
        APP_ACTIVITY.title = "Контакты"
    }

    private fun initRecyclerView() {
        mRecyclerView = binding.contactsRecyclerView
        mRefContacts = REF_DATABASE_ROOT.child(NODE_PHONES_CONTACTS).child(CURRENT_UID)

        val options = FirebaseRecyclerOptions.Builder<CommonModel>()
            .setQuery(mRefContacts, CommonModel::class.java)
            .build()
        mAdapter = object : FirebaseRecyclerAdapter<CommonModel, ContactsHolder>(options){

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
                return ContactsHolder(view)
            }

            override fun onBindViewHolder(holder: ContactsHolder, position: Int, model: CommonModel) {

                mRefUsers = REF_DATABASE_ROOT.child(NODE_USERS).child(model.id)

                mRefUsersListener = AppValueEventListener {
                    val contact = it.getCommonModel()

                    if (contact.fullname.isEmpty()) {
                        holder.name.text = model.fullname
                    } else holder.name.text = contact.fullname

                    holder.status.text = contact.state
                    holder.itemView.setOnClickListener { replaceFragment(SingleChatFragment(model)) }
                }

                mRefUsers.addValueEventListener(mRefUsersListener)
                mapListeners[mRefUsers] = mRefUsersListener
            }
        }

        mRecyclerView.adapter = mAdapter
        mAdapter.startListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class ContactsHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.contact_fullname)
        val status: TextView = view.findViewById(R.id.contact_status)
    }

    override fun onPause() {
        super.onPause()
        mAdapter.stopListening()

        // Останавливаем слушатель и спасаем себя от утечки памяти
        mapListeners.forEach {
            it.key.removeEventListener(it.value)
        }
    }
}

