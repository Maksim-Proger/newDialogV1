package com.example.newdialog.ui.screens.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.newdialog.R
import com.example.newdialog.database.USER
import com.example.newdialog.database.setNameToDatabase
import com.example.newdialog.databinding.FragmentChangeNameBinding
import com.example.newdialog.ui.screens.BaseChangeFragment
import com.example.newdialog.utilits.showToast

class ChangeNameFragment : BaseChangeFragment() {

    private var _binding: FragmentChangeNameBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChangeNameBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFullnameList()
    }

    private fun initFullnameList() {
        val fullNameList = USER.fullname.split(" ")
        if (fullNameList.size > 1) {
            binding.settingsInputName.setText(fullNameList[0])
            binding.settingsInputSecondName.setText(fullNameList[1])
        } else binding.settingsInputName.setText(fullNameList[0])
    }

    override fun change() {
        val name = binding.settingsInputName.text.toString()
        val secondName = binding.settingsInputSecondName.text.toString()
        if (name.isEmpty()) {
            showToast(getString(R.string.settings_toast_name_is_empty))
        } else {
            val fullName = "$name $secondName"
            setNameToDatabase(fullName)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}