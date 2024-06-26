package com.example.newdialog

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.newdialog.database.AUTH
import com.example.newdialog.database.initFirebase
import com.example.newdialog.database.initUser
import com.example.newdialog.databinding.ActivityMainBinding
import com.example.newdialog.ui.screens.main_list.MainListFragment
import com.example.newdialog.ui.screens.register.EnterPhoneNumberFragment
import com.example.newdialog.ui.objects.AppDrawer
import com.example.newdialog.utilits.APP_ACTIVITY
import com.example.newdialog.utilits.AppStates
import com.example.newdialog.utilits.READ_CONTACTS
import com.example.newdialog.utilits.initContacts
import com.example.newdialog.utilits.replaceFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private var _binding : ActivityMainBinding? = null
    private val binding get() = _binding!!
    lateinit var appDrawer: AppDrawer
    lateinit var mToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        APP_ACTIVITY = this

        // метод инициализации базы данных
        initFirebase()

        // метод заполнения модели из базы (работать будет единожды при запуске приложения)
        initUser {
            // Запускаем корутину на главном потоке для чтения контактов
            CoroutineScope(Dispatchers.IO).launch {
                initContacts()
            }

            initFields()
            initFunc()
        }
    }

    override fun onStart() {
        super.onStart()
        AppStates.updateState(AppStates.ONLINE)
    }

    override fun onStop() {
        super.onStop()
        AppStates.updateState(AppStates.OFFLINE)
    }

    private fun initFunc() {
        setSupportActionBar(mToolbar)
        if (AUTH.currentUser != null) {
            appDrawer.create()
            replaceFragment(MainListFragment())
        } else {
            replaceFragment(EnterPhoneNumberFragment())
        }
    }

    private fun initFields() {
        mToolbar = binding.mainToolbar
        appDrawer = AppDrawer()
    }


    //  Метод выводит окно с запросом на предоставление разрешения
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (
            ContextCompat.checkSelfPermission(APP_ACTIVITY, READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED
            ) {
            initContacts()
        }
    }
}