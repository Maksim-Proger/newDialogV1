package com.example.newdialog.ui.screens.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.newdialog.databinding.FragmentEnterPhoneNumberBinding
import com.example.newdialog.utilits.APP_ACTIVITY
import com.example.newdialog.database.AUTH
import com.example.newdialog.utilits.replaceFragment
import com.example.newdialog.utilits.restartActivity
import com.example.newdialog.utilits.showToast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit


class EnterPhoneNumberFragment : Fragment() {
    private var _binding : FragmentEnterPhoneNumberBinding? = null
    private val binding get() = _binding!!

    private lateinit var mPhoneNumber: String
    private lateinit var mCallback: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentEnterPhoneNumberBinding.inflate(inflater)
        return binding.root
    }


    override fun onStart() {
        super.onStart()
        mCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                AUTH.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showToast("Добро пожаловать")
                        restartActivity()
                    } else {
                        showToast(task.exception?.message.toString())
                    }
                }
            }

            // вызывается в случае какой-то проблемы с верификацией
            override fun onVerificationFailed(p0: FirebaseException) {
                when (p0) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        // Неправильный формат номера телефона
                        Toast.makeText(APP_ACTIVITY, "Неправильный формат номера телефона", Toast.LENGTH_SHORT).show()
                    }
                    is FirebaseTooManyRequestsException -> {
                        // Превышен лимит запросов
                        Toast.makeText(APP_ACTIVITY, "Слишком много запросов. Попробуйте позже.", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // Общая ошибка
                        Toast.makeText(APP_ACTIVITY, "Ошибка проверки: ${p0.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                Toast.makeText(APP_ACTIVITY, "Код отправлен на номер: $mPhoneNumber", Toast.LENGTH_SHORT).show()
                replaceFragment(EnterCodeFragment(mPhoneNumber, id))
            }
        }
        binding.registerBtnNext.setOnClickListener { sendCode() }
    }

    private fun sendCode() {
        if (binding.registerInputPhoneNumber.text.toString().isEmpty()) {
            showToast("Введите номер телефона")
        } else {
            authUser()
        }
    }

    private fun authUser() {
        mPhoneNumber = binding.registerInputPhoneNumber.text.toString()
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            mPhoneNumber,
            60,
            TimeUnit.SECONDS,
            APP_ACTIVITY,
            mCallback
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}