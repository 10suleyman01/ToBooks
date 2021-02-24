package com.suleyman.tobooks.utils

import android.graphics.Bitmap
import android.util.Base64
import android.widget.EditText
import java.nio.ByteBuffer

fun EditText.textString(): String {
    return this.text.toString()
}

fun String.decode(): String {
    return Base64.decode(this, Base64.DEFAULT).toString(charset("UTF-8"))
}

fun String.encode(): String {
    return Base64.encodeToString(this.toByteArray(charset("UTF-8")), Base64.DEFAULT)
}
