package com.suleyman.tobooks.utils

import abhishekti7.unicorn.filepicker.UnicornFilePicker
import abhishekti7.unicorn.filepicker.utils.Constants
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.view.View
import com.google.firebase.storage.ListResult
import com.suleyman.tobooks.R
import com.suleyman.tobooks.model.BookModel
import com.google.firebase.storage.StorageReference
import com.suleyman.tobooks.ui.activity.upload.UploadFileActivity


object Common {

    const val APP_EMAIL = "tobooks.net@gmail.com"
    private const val TAG = "Common"
    const val extConfig: String = ".fconfig"
    const val filePaths: String = "filePaths"

    fun showAlertDialog(
        context: Context,
        title: String? = null,
        message: String? = null,
        view: View? = null
    ): AlertDialog.Builder {
        return AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setView(view)
    }

    fun startFilePickerActivity(activity: UploadFileActivity) {
        UnicornFilePicker.from(activity)
            .addConfigBuilder()
            .selectMultipleFiles(false)
            .setRootDirectory(Environment.getExternalStorageDirectory().absolutePath)
            .showHiddenFiles(false)
            .setFilters(arrayOf("pdf", "docx", "djvu", "epub", "fb2", "pptx"))
            .theme(R.style.UnicornFilePicker_Default)
            .addItemDivider(true)
            .build()
            .forResult(Constants.REQ_UNICORN_FILE)
    }

    fun downloadBook(context: Context, book: BookModel) {
        book.downloadUrl?.addOnSuccessListener {
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(it)
            request.setTitle(book.title)
            request.setDescription(context.getString(R.string.downloading))
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                it.lastPathSegment
            )
            downloadManager.enqueue(request)
        }
    }

    fun loadData(
        books: MutableList<BookModel>,
        result: ListResult,
        onCompleted: (MutableList<BookModel>) -> Unit?
    ) {

        result.prefixes.forEach { folder ->
            val folderItem = BookModel(
                title = folder.name,
                path = folder.path,
                type = BookModel.Type.CATEGORY
            )
            books.add(folderItem)
        }

        result.items.forEach { file ->
            if (!file.name.endsWith(extConfig)) {
                val book = BookModel(
                    title = file.name,
                    downloadUrl = file.downloadUrl,
                    metadata = file.metadata,
                    type = BookModel.Type.BOOK
                )
                books.add(book)
            }
        }
        onCompleted(books)
    }

    fun loadDataFromCategory(
        books: MutableList<BookModel>,
        storageReference: StorageReference,
        category: String,
        onCompleted: (MutableList<BookModel>) -> Unit?
    ) {

        storageReference.child(category)
            .listAll()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    it.result?.prefixes?.forEach { folder ->
                        books.add(
                            BookModel(
                                title = folder.name,
                                path = folder.path,
                                childList = folder.listAll(),
                                type = BookModel.Type.CATEGORY
                            )
                        )
                    }
                    it.result?.items?.forEach { file ->
                        if (!file.name.endsWith(extConfig)) {
                            books.add(
                                BookModel(
                                    title = file.name,
                                    downloadUrl = file.downloadUrl,
                                    metadata = file.metadata,
                                    type = BookModel.Type.BOOK
                                )
                            )
                        }
                    }
                }
                onCompleted(books)
            }

    }

}