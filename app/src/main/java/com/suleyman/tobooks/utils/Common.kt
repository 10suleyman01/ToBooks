package com.suleyman.tobooks.utils

import abhishekti7.unicorn.filepicker.UnicornFilePicker
import abhishekti7.unicorn.filepicker.utils.Constants
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import com.google.firebase.storage.ListResult
import com.suleyman.tobooks.R
import com.suleyman.tobooks.model.BookModel
import java.io.File
import android.R.attr.bitmap
import android.R.attr.bottom
import org.koin.java.KoinJavaComponent.inject


object Common {

    const val APP_EMAIL = "tobooks.net@gmail.com"
    private const val TAG = "Common"
    const val extConfig: String = ".fconfig"
    const val filePaths: String = "filePaths"

    private val context: Context by inject(Context::class.java)

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

    fun startFilePickerActivity(fragment: Fragment) {
        UnicornFilePicker.from(fragment)
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

    fun inflateView(
        context: Context,
        resId: Int
    ): View {
        return LayoutInflater.from(context).inflate(resId, null)
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
            books.add(
                BookModel(
                    title = folder.name,
                    path = folder.path,
                    type = BookModel.Type.CATEGORY
                )
            )
        }
        result.items.forEach { file ->
            if (!file.name.endsWith(extConfig)) {
                books.add(
                    BookModel(
                        title = file.name,
                        downloadUrl = file.downloadUrl,
                        type = BookModel.Type.BOOK
                    )
                )
            }
        }
        onCompleted(books)
    }

    fun getBitmapFromPdf(file: File): Bitmap {
        val renderer = PdfRenderer(
            ParcelFileDescriptor.open(
                file,
                ParcelFileDescriptor.MODE_READ_ONLY
            )
        )
        var bitmap: Bitmap? = null
        val pageCount = renderer.pageCount
        (0..1).forEach { i ->
            val page = renderer.openPage(i)
            val width: Int = context.resources.displayMetrics.densityDpi / 72 * page.width
            val height: Int = context.resources.displayMetrics.densityDpi / 72 * page.height
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap?.let {
                page.render(it, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            }
            page.close()
        }

        return bitmap!!
    }

}