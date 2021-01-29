package com.suleyman.tobooks.ui.activity.books

import com.arellomobile.mvp.MvpView

interface BookView: MvpView {
    fun loadRootDirectories()
}