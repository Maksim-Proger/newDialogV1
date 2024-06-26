package com.example.newdialog.ui.screens.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.newdialog.database.USER
import com.example.newdialog.database.setBioToDatabase
import com.example.newdialog.databinding.FragmentChangeBioBinding
import com.example.newdialog.ui.screens.BaseChangeFragment

class ChangeBioFragment : BaseChangeFragment() {

    private var _binding : FragmentChangeBioBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChangeBioBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.settingsInputBio.setText(USER.bio)
    }

    override fun change() {
        super.change()
        val newBio = binding.settingsInputBio.text.toString()
        setBioToDatabase(newBio)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}