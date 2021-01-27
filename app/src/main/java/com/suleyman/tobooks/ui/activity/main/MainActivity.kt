package com.suleyman.tobooks.ui.activity.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.suleyman.tobooks.R
import com.suleyman.tobooks.app.ToBooksApp
import com.suleyman.tobooks.ui.activity.books.BooksActivity
import com.suleyman.tobooks.ui.activity.login.LoginActivity
import com.suleyman.tobooks.ui.activity.profile.ProfileActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()

        ToBooksApp.authInstance().addAuthStateListener {
            val user = it.currentUser
            if (user != null) {
                toBooksActivity()
            } else {
                toLogin()
            }
        }
    }

    private fun toLogin() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    private fun toProfile() {
        val intent = Intent(this@MainActivity, ProfileActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    private fun toBooksActivity() {
        val intent = Intent(this@MainActivity, BooksActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }
}