package com.suleyman.tobooks.ui.activity.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.suleyman.tobooks.ui.activity.books.BooksActivity
import com.suleyman.tobooks.ui.activity.splash.LoginSplashActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var user: FirebaseUser? = null

    @Inject
    lateinit var auth: FirebaseAuth

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
        val intent = Intent(this@MainActivity, LoginSplashActivity::class.java)
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