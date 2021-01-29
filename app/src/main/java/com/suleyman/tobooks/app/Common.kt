package com.suleyman.tobooks.app

import abhishekti7.unicorn.filepicker.UnicornFilePicker
import abhishekti7.unicorn.filepicker.utils.Constants
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.suleyman.tobooks.R
import com.suleyman.tobooks.model.BookModel

object Common {

    private const val TAG = "Common"

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

    fun startFilePickerActivity(activity: Activity) {
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

    fun inflateView(
        context: Context,
        resId: Int
    ): View {
        return LayoutInflater.from(context).inflate(resId, null)
    }

    fun downloadBook(context: Context, book: BookModel) {
        book.downloadUrl?.addOnSuccessListener {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(it)
            request.setTitle(book.title)
            request.setDescription(context.getString(R.string.downloading))
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, it.lastPathSegment)
            downloadManager.enqueue(request)
        }
    }

}