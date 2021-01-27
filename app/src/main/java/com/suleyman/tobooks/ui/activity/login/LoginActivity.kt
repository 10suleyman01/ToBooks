package com.suleyman.tobooks.ui.activity.login

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import com.google.firebase.auth.FirebaseAuth
import com.suleyman.tobooks.R
import com.suleyman.tobooks.app.ToBooksApp

@SuppressLint("NonConstantResourceId")
class LoginActivity : AppCompatActivity(), View.OnClickListener {

    @BindView(R.id.etLogin)
    lateinit var etLogin: EditText

    @BindView(R.id.etPassword)
    lateinit var etPassword: EditText

    @BindView(R.id.btnSignIn)
    lateinit var btnSignIn: Button

    @BindView(R.id.btnSignUp)
    lateinit var btnSignUp: Button

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ButterKnife.bind(this)

        setSupportActionBar(toolbar)

        btnSignIn.setOnClickListener(this)
        btnSignUp.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnSignIn -> {
                val login = etLogin.text.toString()
                val password = etPassword.text.toString()

                if (login.isNotEmpty() && password.isNotEmpty()) {
                    ToBooksApp.authInstance().signInWithEmailAndPassword(login, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                ToBooksApp.toast(R.string.sign_in_successful)
                            } else {
                                ToBooksApp.toast(R.string.sign_in_error)
                            }
                        }
                }

            }
            R.id.btnSignUp -> {

                val login = etLogin.text.toString()
                val password = etPassword.text.toString()

                if (login.isNotEmpty() && password.isNotEmpty()) {
                    ToBooksApp.authInstance().createUserWithEmailAndPassword(login, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                ToBooksApp.toast(R.string.sign_up_successful)
                            } else {
                                ToBooksApp.toast( R.string.sign_up_error)
                            }
                        }
                }
            }
        }
    }
}