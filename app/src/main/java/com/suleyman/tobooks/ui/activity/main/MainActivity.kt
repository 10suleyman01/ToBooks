package com.suleyman.tobooks.ui.activity.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.suleyman.tobooks.app.ToBooksApp
import com.suleyman.tobooks.ui.activity.books.BooksActivity
import com.suleyman.tobooks.ui.activity.login.LoginActivity

class MainActivity : AppCompatActivity() {

    private var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        ToBooksApp.authInstance().addAuthStateListener {
            user = it.currentUser
            if (user != null) {
                toBooksActivity()
            } else {
                toLogin()
            }
        }
    }

    override fun onBackPressed() {
        if (user == null) {
            finish()
        } else super.onBackPressed()
    }

    private fun toLogin() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    private fun toBooksActivity() {
        val intent = Intent(this@MainActivity, BooksActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }
}