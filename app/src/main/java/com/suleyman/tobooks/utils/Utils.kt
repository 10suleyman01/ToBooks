package com.suleyman.tobooks.utils

import android.content.Context
import android.widget.Toast
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class Utils: KoinComponent {
    private val context: Context by inject()

    fun toast(message: Int) {
        toast(context.getString(message))
    }

    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun toastLong(message: Int) {
        toastLong(context.getString(message))
    }

    fun toastLong(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }


    fun getString(resId: Int) = context.getString(resId)
}