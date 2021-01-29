package com.suleyman.tobooks.ui.activity.books

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter

@InjectViewState
class BooksPresenter: MvpPresenter<BookView>() {

    fun loadRoot() = viewState.loadRootDirectories()

}