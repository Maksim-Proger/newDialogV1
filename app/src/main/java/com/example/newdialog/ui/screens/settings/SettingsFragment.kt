package com.example.newdialog.ui.screens.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.example.newdialog.R
import com.example.newdialog.databinding.FragmentSettingsBinding
import com.example.newdialog.database.AUTH
import com.example.newdialog.utilits.AppStates
import com.example.newdialog.database.USER
import com.example.newdialog.ui.screens.BaseFragment
import com.example.newdialog.utilits.replaceFragment
import com.example.newdialog.utilits.restartActivity

class SettingsFragment : BaseFragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // TODO разобраться что это
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFields()
    }

    // создаем меню настроек
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity?.menuInflater?.inflate(R.menu.settings_action_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // обработчик кнопок меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings_menu_btn_exit -> { // настраиваем кнопку выхода из аккаунта
                AppStates.updateState(AppStates.OFFLINE)
                AUTH.signOut()
                restartActivity()
            }

            R.id.settings_menu_btn_edit_name -> {
                replaceFragment(ChangeNameFragment())
            }
        }
        return true
    }

    private fun initFields() { // инициализируем поля настроек из базы
        with(binding) {
            settingsBio.text = USER.bio
            settingsTextViewFullName.text = USER.fullname
            settingsPhoneNumber.text = USER.phone
            settingsTextViewStatus.text = USER.state
            settingsUsername.text = USER.username

            settingsUsername.setOnClickListener {
                replaceFragment(ChangeUsernameFragment())
            }
            settingsBio.setOnClickListener {
                replaceFragment(ChangeBioFragment())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}