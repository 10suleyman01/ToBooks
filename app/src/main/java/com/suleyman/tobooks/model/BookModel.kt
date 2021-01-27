package com.suleyman.tobooks.model

data class BookModel(
    val title: String? = null,
    val downloadUrl: String? = null,
    val parent: String? = null,
    val type: Type? = Type.CATEGORY,
) {
    enum class Type {
        BOOK,
        CATEGORY,
    }

    override fun toString(): String {
        return title.toString()
    }
}