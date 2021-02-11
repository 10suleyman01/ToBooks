package com.suleyman.tobooks.ui.activity.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.suleyman.tobooks.ui.activity.books.BooksActivity
import com.suleyman.tobooks.ui.activity.login.LoginActivity
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class MainActivity : AppCompatActivity() {

    private var user: FirebaseUser? = null
    private val auth: FirebaseAuth by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener {
            user = it.currentUser
            if (user != null) {
                toBooksActivity()
            } else {
                toLogin()
            }
        }
    }

    private fun toLogin() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun toBooksActivity() {
        val intent = Intent(this@MainActivity, BooksActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}