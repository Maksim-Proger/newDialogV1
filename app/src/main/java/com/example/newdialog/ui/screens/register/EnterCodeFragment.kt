package com.example.newdialog.ui.screens.register

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.newdialog.database.AUTH
import com.example.newdialog.database.CHILD_FCM_TOKEN
import com.example.newdialog.database.CHILD_ID
import com.example.newdialog.database.CHILD_PHONE
import com.example.newdialog.database.CHILD_USERNAME
import com.example.newdialog.database.NODE_PHONES
import com.example.newdialog.database.NODE_USERS
import com.example.newdialog.database.REF_DATABASE_ROOT
import com.example.newdialog.databinding.FragmentEnterCodeBinding
import com.example.newdialog.utilits.APP_ACTIVITY
import com.example.newdialog.utilits.AppTextWatcher
import com.example.newdialog.utilits.AppValueEventListener
import com.example.newdialog.utilits.restartActivity
import com.example.newdialog.utilits.showToast
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.messaging.FirebaseMessaging

class EnterCodeFragment(private val phoneNumber: String, val id: String) : Fragment() {
    private var _binding : FragmentEnterCodeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentEnterCodeBinding.inflate(inflater)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        APP_ACTIVITY.title = phoneNumber
        binding.registerInputCode.addTextChangedListener (AppTextWatcher {
            val string = binding.registerInputCode.text.toString()
            if (string.length == 6) {
                enterCode()
            }
        })
    }

    // Тестируем получение и сохранение FCM токена
    private var tokenString = ""
    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->

            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Получаем Token
            val token = task.result
            saveToken(token)
        }
    }

    private fun saveToken(token: String){
        tokenString = token
    }

    private fun enterCode() {
        getToken()
        val code = binding.registerInputCode.text.toString()
        val credential = PhoneAuthProvider.getCredential(id, code)

        AUTH.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uid = AUTH.currentUser?.uid.toString()
                val dateMap = mutableMapOf<String, Any>()
                dateMap[CHILD_ID] = uid
                dateMap[CHILD_PHONE] = phoneNumber
                dateMap[CHILD_FCM_TOKEN] = tokenString

                // Выполняем проверку имеется уже это username в ноде или нет
                REF_DATABASE_ROOT.child(NODE_USERS).child(uid)
                    .addListenerForSingleValueEvent(AppValueEventListener {

                        // Если нет, то только в этом случае будем присваивать id данному child
                        // без этой проверки username при каждом входе становился дефолтным
                        if (!it.hasChild(CHILD_USERNAME)) {
                            dateMap[CHILD_USERNAME] = uid
                        }

                        // Создаем новую ноду с номерами телефонов и связанными с ними id
                        REF_DATABASE_ROOT.child(NODE_PHONES).child(phoneNumber).setValue(uid)
                            .addOnFailureListener { showToast(it.message.toString()) }
                            .addOnSuccessListener {
                                REF_DATABASE_ROOT
                                    .child(NODE_USERS)
                                    .child(uid)
                                    .updateChildren(dateMap)
                                    .addOnSuccessListener {
                                        showToast("Добро пожаловать")
                                        restartActivity()
                                    }
                                    .addOnFailureListener { showToast(it.message.toString()) }
                            }
                    })
            } else {
                showToast(task.exception?.message.toString())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}