package com.example.newdialog.ui.screens.single_chat

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.newdialog.R
import com.example.newdialog.database.CURRENT_UID
import com.example.newdialog.database.NODE_MESSAGES
import com.example.newdialog.database.NODE_USERS
import com.example.newdialog.database.REF_DATABASE_ROOT
import com.example.newdialog.database.TYPE_TEXT
import com.example.newdialog.database.clearChat
import com.example.newdialog.database.deleteChat
import com.example.newdialog.database.getCommonModel
import com.example.newdialog.database.getMessageKey
import com.example.newdialog.database.getUserModel
import com.example.newdialog.database.saveToMainList
import com.example.newdialog.database.sendMessage
import com.example.newdialog.database.uploadFileToStorage
import com.example.newdialog.databinding.FragmentSingleChatBinding
import com.example.newdialog.models.CommonModel
import com.example.newdialog.models.User
import com.example.newdialog.ui.messageRecyclerView.views.AppViewFactory
import com.example.newdialog.ui.screens.BaseFragment
import com.example.newdialog.ui.screens.main_list.MainListFragment
import com.example.newdialog.utilits.APP_ACTIVITY
import com.example.newdialog.utilits.AppChildEventListener
import com.example.newdialog.utilits.AppTextWatcher
import com.example.newdialog.utilits.AppValueEventListener
import com.example.newdialog.utilits.AppVoiceRecorder
import com.example.newdialog.utilits.PICK_FILE_REQUEST_CODE
import com.example.newdialog.utilits.RECORD_AUDIO
import com.example.newdialog.utilits.TYPE_CHAT
import com.example.newdialog.utilits.TYPE_MESSAGE_FILE
import com.example.newdialog.utilits.TYPE_MESSAGE_VOICE
import com.example.newdialog.utilits.checkPermission
import com.example.newdialog.utilits.getFileNameFromUri
import com.example.newdialog.utilits.replaceFragment
import com.example.newdialog.utilits.showToast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SingleChatFragment(private val contact: CommonModel) : BaseFragment() {

    // region start
    private var _binding : FragmentSingleChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var toolbarInfo: View

    // слушатель изменений для последующей передачи в Toolbar
    private lateinit var mListenerInfoToolbar: AppValueEventListener

    // user который мы постоянно обновляем и потом отправляем в Toolbar
    private lateinit var mReceivingUser: User

    private lateinit var mRefUser: DatabaseReference
    private lateinit var mRefMessages: DatabaseReference
    private lateinit var mAdapter: SingleChatAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mMessagesListener: AppChildEventListener
    private var mCountMessages = 15
    private var mIsScrolling = false
    private var mSmoothScrollToPosition = true
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var mAppVoiceRecorder: AppVoiceRecorder
    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var btnAttachFile: ImageView
    // endregion

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSingleChatBinding.inflate(inflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initFields()
        initToolbar(requireContext())
        initRecyclerView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Устанавливаем наш BottomSheet
        val bottomSheet: LinearLayout = view.findViewById(R.id.bottom_sheet_choice)
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        // Находим кнопку загрузки файла
        btnAttachFile = view.findViewById(R.id.btn_attach_file)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initFields() {

        setHasOptionsMenu(true)

        mAppVoiceRecorder = AppVoiceRecorder()
        mSwipeRefreshLayout = binding.chatSwipeRefresh
        mLayoutManager = LinearLayoutManager(this.context)

        // Настраиваем слушатель состояния поля для ввода текста, и меняем видимость кнопок
        binding.chatInputMessage.addTextChangedListener ( AppTextWatcher {
            with(binding) {
                val string = chatInputMessage.text.toString()
                if (string.isEmpty() || string == "Запись") {
                    chatBtnSendMessage.visibility = View.GONE
                    chatBtnAttach.visibility = View.VISIBLE
                    chatBtnVoice.visibility = View.VISIBLE
                } else {
                    chatBtnSendMessage.visibility = View.VISIBLE
                    chatBtnAttach.visibility = View.GONE
                    chatBtnVoice.visibility = View.GONE
                }
            }
        })

        binding.chatBtnAttach.setOnClickListener { attach() }

        // Настраиваем слушатель нажатия на кнопку запись голоса в отдельной Coroutine
        CoroutineScope(Dispatchers.IO).launch {
            binding.chatBtnVoice.setOnTouchListener { _, event ->
                if (checkPermission(RECORD_AUDIO)) {
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        binding.chatInputMessage.setText("Запись...")
                        binding.chatBtnVoice.setColorFilter(ContextCompat.getColor(APP_ACTIVITY, R.color.main_color_background_app))
                        val messageKey = getMessageKey(contact.id)
                        mAppVoiceRecorder.startRecord(messageKey)

                    } else if (event.action == MotionEvent.ACTION_UP) {
                        binding.chatInputMessage.setText("")
                        binding.chatBtnVoice.colorFilter = null
                        mAppVoiceRecorder.stopRecord { file, messageKey ->
                            uploadFileToStorage(Uri.fromFile(file), messageKey, contact.id, TYPE_MESSAGE_VOICE)
                            mSmoothScrollToPosition = true
                        }
                    }
                }
                true
            }
        }
    }

    private fun attach() {
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        btnAttachFile.setOnClickListener {
            attachFile()
        }
    }

    private fun attachFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*" // показывать все файлы
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data!=null){
            when(requestCode){
                PICK_FILE_REQUEST_CODE -> {
                    val uri = data.data
                    val messageKey = getMessageKey(contact.id)
                    val filename = getFileNameFromUri(uri!!)
                    uploadFileToStorage(uri,messageKey,contact.id, TYPE_MESSAGE_FILE, filename)
                    mSmoothScrollToPosition = true
                }
            }
        }
    }

    private fun initRecyclerView() {
        mRecyclerView = binding.chatRecyclerView
        mAdapter = SingleChatAdapter()
        mRefMessages = REF_DATABASE_ROOT.child(NODE_MESSAGES).child(CURRENT_UID).child(contact.id)
        mRecyclerView.adapter = mAdapter

        // Используем ускоритель в котором указываем, что все наши ViewHolders одинакового размера
        mRecyclerView.setHasFixedSize(true)

        // Еще один укоритель. Тут мы указываем что не используем NestedScrolling
        mRecyclerView.isNestedScrollingEnabled = false

        mRecyclerView.layoutManager = mLayoutManager

        mMessagesListener = AppChildEventListener {
            val message = it.getCommonModel() // получаем сообщение

            // Опускаем приложение вниз
            if (mSmoothScrollToPosition) {
                mAdapter.addItemToBottom(AppViewFactory.getView(message)) {
                    mRecyclerView.smoothScrollToPosition(mAdapter.itemCount) // даем команду нашей RecyclerView прокрутится в самый низ
                }
            } else {
                mAdapter.addItemToTop(AppViewFactory.getView(message)) {
                    mSwipeRefreshLayout.isRefreshing = false // отключаем SwipeRefreshLayout
                }
            }
        }

        // limitToLast(mCountMessages) позволяет ограничить загрузку нужным кол-вом элементов
        mRefMessages.limitToLast(mCountMessages).addChildEventListener(mMessagesListener)

        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            // Фиксирует изменение скроллинга
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (mIsScrolling  && dy < 0 && mLayoutManager.findFirstVisibleItemPosition() <= 3) {
                    updateData()
                }
            }

            // Проверяем состояние скроллинга в RecyclerView
            // Тут мы можем проверить есть ли какое-то движение или нет
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    mIsScrolling = true
                }
            }
        })

        mSwipeRefreshLayout.setOnRefreshListener { updateData() }
    }

    private fun updateData() {
        mSmoothScrollToPosition = false
        mIsScrolling = false
        mCountMessages += 10
        mRefMessages.removeEventListener(mMessagesListener)
        mRefMessages.limitToLast(mCountMessages).addChildEventListener(mMessagesListener)
    }

    private fun initToolbar(context: Context) {
        toolbarInfo = APP_ACTIVITY.findViewById(R.id.toolbar_info)
        toolbarInfo.visibility = View.VISIBLE

        mListenerInfoToolbar = AppValueEventListener {
            mReceivingUser = it.getUserModel()
            // полученные данные из базы передаем в Toolbar
            initInfoToolbar()
        }

        mRefUser = REF_DATABASE_ROOT.child(NODE_USERS).child(contact.id)
        mRefUser.addValueEventListener(mListenerInfoToolbar)

        binding.chatBtnSendMessage.setOnClickListener {
            mSmoothScrollToPosition = true
            val message = binding.chatInputMessage.text.toString()
            if (message.isEmpty()) {
                showToast("Введите сообщение")
            } else {
                sendMessage(context, message, contact.id, TYPE_TEXT) {
                    saveToMainList(contact.id, TYPE_CHAT) // TODO что делает этот за метод??
                    binding.chatInputMessage.setText("")
                }
            }
        }
    }

    private fun initInfoToolbar() {
        val toolbarChatFullname = toolbarInfo.findViewById<TextView>(R.id.toolbar_chat_fullname)
        val toolbarChatStatus = toolbarInfo.findViewById<TextView>(R.id.toolbar_chat_status)

        if (mReceivingUser.fullname.isEmpty()) {
            toolbarChatFullname.text = contact.fullname
        } else toolbarChatFullname.text = mReceivingUser.fullname

        toolbarChatStatus.text = mReceivingUser.state
    }

    // создаем меню настроек
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity?.menuInflater?.inflate(R.menu.single_chat_action_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // обработчик кнопок меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_clear_chat -> clearChat(contact.id){
                showToast("Чат очищен")
                replaceFragment(MainListFragment())
            }
            R.id.menu_delete_chat -> deleteChat(contact.id) {
                showToast("Чат удален")
                replaceFragment(MainListFragment())
            }
        }
        return true
    }

    override fun onPause() {
        super.onPause()
        toolbarInfo.visibility = View.GONE
        mRefUser.removeEventListener(mListenerInfoToolbar)
        mRefMessages.removeEventListener(mMessagesListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mAppVoiceRecorder.releaseRecorder()
        mAdapter.onDestroy()
    }
}
