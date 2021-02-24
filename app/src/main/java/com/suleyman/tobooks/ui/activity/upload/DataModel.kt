package com.suleyman.tobooks.ui.activity.upload

import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
data class DataModel(
    val bookName: String? = null,
    val bookAuthorName: String? = null,
    val bookImageBytes: String? = null
)