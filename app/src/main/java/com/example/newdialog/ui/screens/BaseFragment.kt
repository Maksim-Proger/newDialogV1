package com.example.newdialog.ui.screens

import androidx.fragment.app.Fragment
import com.example.newdialog.utilits.APP_ACTIVITY

open class BaseFragment : Fragment() {
    override fun onStart() {
        super.onStart()
        APP_ACTIVITY.appDrawer.disableDrawer()
    }
}