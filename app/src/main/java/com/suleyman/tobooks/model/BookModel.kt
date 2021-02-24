package com.suleyman.tobooks.model

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageMetadata

data class BookModel(
    val title: String? = null,
    val downloadUrl: Task<Uri>? = null,
    val path: String? = null,
    var childList: Task<ListResult>? = null,
    var metadata: Task<StorageMetadata>? = null,
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