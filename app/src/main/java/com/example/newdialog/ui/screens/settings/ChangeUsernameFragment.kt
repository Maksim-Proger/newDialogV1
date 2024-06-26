package com.example.newdialog.ui.screens.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.newdialog.R
import com.example.newdialog.database.CURRENT_UID
import com.example.newdialog.database.NODE_USERNAMES
import com.example.newdialog.database.REF_DATABASE_ROOT
import com.example.newdialog.database.USER
import com.example.newdialog.database.updateCurrentUsername
import com.example.newdialog.databinding.FragmentChangeUserNameBinding
import com.example.newdialog.ui.screens.BaseChangeFragment
import com.example.newdialog.utilits.AppValueEventListener
import com.example.newdialog.utilits.showToast
import java.util.Locale


class ChangeUsernameFragment : BaseChangeFragment() {

    private var _binding : FragmentChangeUserNameBinding? = null
    private val binding get() = _binding!!

    private lateinit var _newUsername : String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChangeUserNameBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.settingsInputUserName.setText(USER.username)
    }

    override fun change() {
        _newUsername = binding.settingsInputUserName.text.toString().toLowerCase(Locale.getDefault())
        if (_newUsername.isEmpty()) {
            showToast(getString(R.string.empty_field_toast))
        } else {
            REF_DATABASE_ROOT.child(NODE_USERNAMES)
                .addListenerForSingleValueEvent(AppValueEventListener {
                    if (it.hasChild(_newUsername)) {
                        showToast(getString(R.string.such_user_already_exists_toast))
                    } else {
                        changeUserName()
                    }
                })
        }
    }

    private fun changeUserName() {
        REF_DATABASE_ROOT.child(NODE_USERNAMES).child(_newUsername).setValue(CURRENT_UID)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    updateCurrentUsername(_newUsername)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}