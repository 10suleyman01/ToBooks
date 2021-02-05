package com.suleyman.tobooks.model

import android.net.Uri
import com.google.android.gms.tasks.Task
import me.aflak.filter_annotation.Filterable

@Filterable
data class BookModel(
    val title: String? = null,
    val downloadUrl: Task<Uri>? = null,
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