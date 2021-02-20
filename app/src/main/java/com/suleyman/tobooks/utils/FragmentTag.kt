package com.suleyman.tobooks.utils

enum class FragmentTag(private val tag: String) {
    BOOKS("BOOKS"),
    AUDIOS("AUDIOS"),
    MARKET("MARKET");
    fun value() = tag
}