package com.example.newdialog.ui.screens.main_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.newdialog.database.CURRENT_UID
import com.example.newdialog.database.NODE_MAIN_LIST
import com.example.newdialog.database.NODE_MESSAGES
import com.example.newdialog.database.NODE_USERS
import com.example.newdialog.database.REF_DATABASE_ROOT
import com.example.newdialog.database.getCommonModel
import com.example.newdialog.databinding.FragmentMainListBinding
import com.example.newdialog.models.CommonModel
import com.example.newdialog.utilits.APP_ACTIVITY
import com.example.newdialog.utilits.AppValueEventListener
import com.example.newdialog.utilits.hideKeyboard

class MainListFragment : Fragment() {
    private var _binding : FragmentMainListBinding? = null
    private val binding get() = _binding!!

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: MainListAdapter

    // Составляем запросы в базу
    private val mRefMainList = REF_DATABASE_ROOT.child(NODE_MAIN_LIST).child(CURRENT_UID)
    private val mRefUsers = REF_DATABASE_ROOT.child(NODE_USERS)
    private val mRefMessages = REF_DATABASE_ROOT.child(NODE_MESSAGES).child(CURRENT_UID)
    private var mListItems = listOf<CommonModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMainListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        APP_ACTIVITY.title = "New Dialog"
        APP_ACTIVITY.appDrawer.enableDrawer()
        hideKeyboard()
        initRecyclerView()
    }

    private fun initRecyclerView() {
        mRecyclerView = binding.mainListRecyclerView
        mAdapter = MainListAdapter()

        // 1-й запрос
        // Считали все содержимое диалога, которой содержится нашей ноды NODE_MAIN_LIST
        mRefMainList.addListenerForSingleValueEvent(AppValueEventListener {dataSnapshot ->
            // каждую запись в диалоге переводим в map, затем трансформируем из map в list
            // Если я правильно понял, то в mListItems будут содержаться модельки участников диалога
            mListItems = dataSnapshot.children.map { it.getCommonModel() }

            // Перебираем модельки и получаем информацию о каждой модельке
            mListItems.forEach { model ->

                // 2-й запрос
                mRefUsers.child(model.id).addListenerForSingleValueEvent(AppValueEventListener {dataSnapshot1 ->
                    val newModel = dataSnapshot1.getCommonModel()

                    // 3-й запрос
                    // командой limitToLast(1) мы берем последний элемент из списка
                    mRefMessages.child(model.id).limitToLast(1)
                        .addListenerForSingleValueEvent(AppValueEventListener {dataSnapshot2 ->
                            val tempList = dataSnapshot2.children.map { it.getCommonModel() }

                            if (tempList.isEmpty()) {
                                newModel.lastMessage = "Чат очищен"
                            } else {
                                newModel.lastMessage = tempList[0].text
                            }

                            // Если в модельке отсутствует fullname, то мы заменим его на номер телефона
                            if (newModel.fullname.isEmpty()) {
                                newModel.fullname = newModel.phone
                            }
                            mAdapter.updateListItems(newModel)
                    })
                })
            }
        })

        mRecyclerView.adapter = mAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}