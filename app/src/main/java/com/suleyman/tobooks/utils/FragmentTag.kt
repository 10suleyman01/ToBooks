package com.suleyman.tobooks.utils

enum class FragmentTag(private val tag: String) {
    BOOKS("BOOKS"),
    AUDIOS("AUDIOS"),
    ABOUT("ABOUT"),
    MARKET("MARKET");
    fun value() = tag
}