package com.example.newdialog.ui.screens

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.example.newdialog.R
import com.example.newdialog.utilits.APP_ACTIVITY
import com.example.newdialog.utilits.hideKeyboard

open class BaseChangeFragment : Fragment() {
    override fun onStart() {
        super.onStart()
        setHasOptionsMenu(true) // TODO разобраться что это
        APP_ACTIVITY.appDrawer.disableDrawer()
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        APP_ACTIVITY.menuInflater.inflate(R.menu.settings_menu_confirm, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.settings_confirm_change -> change()
        }
        return true
    }

    open fun change() {}
}