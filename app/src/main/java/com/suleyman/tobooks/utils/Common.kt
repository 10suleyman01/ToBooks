package com.suleyman.tobooks.utils

import abhishekti7.unicorn.filepicker.UnicornFilePicker
import abhishekti7.unicorn.filepicker.utils.Constants
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.view.ViewCompat
import com.google.firebase.storage.ListResult
import com.suleyman.tobooks.R
import com.suleyman.tobooks.model.BookModel
import com.google.firebase.storage.StorageReference
import com.suleyman.tobooks.ui.activity.upload.UploadFileActivity
import java.io.File


object Common {

    const val APP_EMAIL = "tobooks.net@gmail.com"
    private const val TAG = "Common"
    const val extConfig: String = ".fconfig"
    const val filePaths: String = "filePaths"

    val GENRES = arrayListOf(
        "Бизнес",
        "Военное дело",
        "Деловая литература",
        "Детективы и триллер",
        "Детские",
        "Документальная литература",
        "Дом и дача",
        "Дом и семья",
        "Драматургия",
        "Зарубежная литература",
        "Знания и навыки",
        "История",
        "Компьютеры и интернет",
        "Любовные романы",
        "Лёгкое чтение",
        "Научно-образовательная",
        "Поэзия и драматургия",
        "Приключения",
        "Проза",
        "Прочее",
        "Психология и мотивация",
        "Публицистика и периодическое издание",
        "Религия и духовность",
        "Родителям",
        "Серьёзное чтение",
        "Спорт, здоровье и красота",
        "Справочная литература",
        "Старинная литература",
        "Техника",
        "Фантастика и фэнтези",
        "Фольклор",
        "Хобби и досуг",
        "Юмор")

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
            .setFilters(arrayOf("pdf", "docx", "djvu", "epub", "fb2", "pptx", "doc"))
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

    fun loadDataNew(
        books: MutableList<BookModel>,
        result: ListResult,
        onCompleted: (MutableList<BookModel>) -> Unit?
    ) {
        if (books.isNotEmpty()) books.clear()
        val map: HashMap<BookModel.Type, MutableList<StorageReference>> = hashMapOf(
            BookModel.Type.CATEGORY to result.prefixes,
            BookModel.Type.BOOK to result.items
        )
        for ((type, storage) in map) {
            when (type) {
                BookModel.Type.CATEGORY -> {
                    storage.forEach { folder ->
                        val data = BookModel(
                            title = folder.name,
                            path = folder.path,
                            type = BookModel.Type.CATEGORY
                        )
                        books.add(data)
                    }
                }
                BookModel.Type.BOOK -> {
                    storage.forEach { book ->
                        if (!book.name.endsWith(extConfig)) {
                            val data = BookModel(
                                title = book.name,
                                downloadUrl = book.downloadUrl,
                                metadata = book.metadata,
                                type = BookModel.Type.BOOK
                            )
                            books.add(data)
                        }
                    }
                }
            }
            onCompleted(books)
        }
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

     fun rotate(view: View, value: Float) {
        ViewCompat.animate(view)
            .rotation(value)
            .withLayer()
            .setDuration(300L)
            .setInterpolator(OvershootInterpolator(10.0f))
            .start()
    }

}