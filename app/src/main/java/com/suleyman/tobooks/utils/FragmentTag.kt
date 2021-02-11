package com.suleyman.tobooks.utils

enum class FragmentTag(private val tag: String) {
    BOOKS("BOOKS"),
    AUDIOS("AUDIOS");
    fun value() = tag
}