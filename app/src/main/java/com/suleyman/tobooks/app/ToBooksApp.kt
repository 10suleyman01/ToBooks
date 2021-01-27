package com.suleyman.tobooks.app

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class ToBooksApp: Application() {

    override fun onCreate() {
        super.onCreate()

        mAuth = FirebaseAuth.getInstance()
        context = applicationContext
        res = resources
    }

    companion object {

        private lateinit var res: Resources

        private lateinit var mAuth: FirebaseAuth
        private lateinit var context: Context

        fun authInstance(): FirebaseAuth {
            return mAuth
        }

        fun toast(message: Int) {
            toast(res.getString(message))
        }

        fun toast(message: String) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }



}