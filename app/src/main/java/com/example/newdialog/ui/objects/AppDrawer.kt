package com.example.newdialog.ui.objects

import android.view.View
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.newdialog.R
import com.example.newdialog.database.USER
import com.example.newdialog.ui.screens.main_list.MainListFragment
import com.example.newdialog.ui.screens.ContactsFragment
import com.example.newdialog.ui.screens.settings.SettingsFragment
import com.example.newdialog.utilits.APP_ACTIVITY
import com.example.newdialog.utilits.replaceFragment
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem

class AppDrawer {

    private lateinit var drawer: Drawer
    private lateinit var accountHeader: AccountHeader
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var mCurrentProfile: ProfileDrawerItem

    fun create() {
        createHeader()
        setupNavigation()
        drawerLayout = drawer.drawerLayout
    }

    // Отключаем toggle и меняем его на кнопку назад
    fun disableDrawer() {
        drawer.actionBarDrawerToggle?.isDrawerIndicatorEnabled = false
        APP_ACTIVITY.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        APP_ACTIVITY.mToolbar.setNavigationOnClickListener {
            APP_ACTIVITY.supportFragmentManager.popBackStack()
        }
    }

    // Действие наоборот
    fun enableDrawer() {
        APP_ACTIVITY.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        drawer.actionBarDrawerToggle?.isDrawerIndicatorEnabled = true
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        APP_ACTIVITY.mToolbar.setNavigationOnClickListener {
            drawer.openDrawer()
        }
    }

    private fun setupNavigation() {
        // Создание Menu Items для Drawer
        val item1 = PrimaryDrawerItem()
            .withIdentifier(1).withName("Settings").withIcon(R.drawable.ic_btn_settings)
        val item2 = PrimaryDrawerItem()
            .withIdentifier(2).withName("Chats").withIcon(R.drawable.ic_btn_chats)
        val item3 = PrimaryDrawerItem()
            .withIdentifier(3).withName("Contacts").withIcon(R.drawable.ic_btn_contacts)

        // Создание Drawer
        drawer = DrawerBuilder()
            .withActivity(APP_ACTIVITY)
            .withToolbar(APP_ACTIVITY.mToolbar)
            .withActionBarDrawerToggle(true)
            .withSelectedItem(-1)
            .withAccountHeader(accountHeader)
            .addDrawerItems(item1, item2, item3)
            .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                override fun onItemClick(
                    view: View?,
                    position: Int,
                    drawerItem: IDrawerItem<*>
                ): Boolean {
                    when (drawerItem.identifier) {
                        1L -> replaceFragment(SettingsFragment())
                        2L -> replaceFragment(MainListFragment())
                        3L -> replaceFragment(ContactsFragment())
                    }
                    return false
                }
            }).build()

        APP_ACTIVITY.window.statusBarColor = ContextCompat.getColor(APP_ACTIVITY, R.color.main_color_background_app)
    }

    private fun createHeader() {
        // Создание профилей для AccountHeader
        mCurrentProfile = ProfileDrawerItem()
            .withName(USER.fullname)
            .withEmail(USER.phone)
//            .withIcon(USER.photoUrl) // пока не работает
            .withIcon(R.drawable.ic_launcher_foreground)
            .withIdentifier(200)

        // Создание AccountHeader
        accountHeader = AccountHeaderBuilder()
            .withActivity(APP_ACTIVITY)
            .withHeaderBackground(R.drawable.header_background)
            .addProfiles(mCurrentProfile)
            .build()
    }

    fun updateHeader() {
        mCurrentProfile
            .withName(USER.fullname)
            .withEmail(USER.phone)
//            .withIcon(USER.photoUrl) // пока не работает
            .withIcon(R.drawable.ic_launcher_foreground)

        accountHeader.updateProfile(mCurrentProfile)
    }

    private fun initLoader() { // Метод загрузки нашей иконки из базы (TODO пока не работает)
    // смотреть это видео
    // https://www.youtube.com/watch?v=smUy99sFZLo&list=PLY8G5DMG6TiOBq7OWFPWF2Um3FRB5s2ke&index=43
    }
}