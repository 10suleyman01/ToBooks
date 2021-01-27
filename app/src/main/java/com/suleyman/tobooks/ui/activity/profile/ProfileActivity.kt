package com.suleyman.tobooks.ui.activity.profile

import abhishekti7.unicorn.filepicker.UnicornFilePicker
import abhishekti7.unicorn.filepicker.utils.Constants
import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import butterknife.BindView
import butterknife.ButterKnife
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.suleyman.tobooks.R
import com.suleyman.tobooks.app.ToBooksApp
import com.suleyman.tobooks.ui.activity.books.BooksActivity
import java.io.File


@SuppressLint("NonConstantResourceId")
class ProfileActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val TAG = "ProfileActivity"
        private const val REQUEST_CHECK_PERMISSIONS = 1263
    }

    private lateinit var storageReference: StorageReference

    @BindView(R.id.btnUpload)
    lateinit var btnUpload: Button

    @BindView(R.id.btnDownload)
    lateinit var btnDownload: Button

    @BindView(R.id.progressBar)
    lateinit var progressBar: ProgressBar

    lateinit var tvFilePath: TextView

    private var file: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        ButterKnife.bind(this)

        btnUpload.setOnClickListener(this)
        btnDownload.setOnClickListener(this)

        storageReference = FirebaseStorage.getInstance().reference

        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), REQUEST_CHECK_PERMISSIONS
        )

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnUpload -> {
                uploadBook()
            }
            R.id.btnDownload -> {
                toDownloadBookActivity()
            }
        }
    }

    private fun uploadBook() {
        val selectBookView = LayoutInflater.from(this).inflate(R.layout.select_book_view, null)

        tvFilePath = selectBookView.findViewById(R.id.tvFilePath)
        val btnSelect = selectBookView.findViewById<Button>(R.id.btnSelectBook)

        btnSelect.setOnClickListener {
            UnicornFilePicker.from(this@ProfileActivity)
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

        val dialogSelectBook = Dialog(this)
        dialogSelectBook.setContentView(R.layout.select_book_view)
        dialogSelectBook.setTitle(R.string.upload)
        dialogSelectBook.create()
        dialogSelectBook.show()

        val alertDialog = AlertDialog.Builder(this)
            .setTitle(R.string.upload)
            .setMessage(R.string.set_upload_file)
            .setView(selectBookView)
            .setPositiveButton(R.string.upload_btn_text) { _, _ ->
                if (file != null) {
                    storageReference.child("books/" + file!!.name)
                        .putFile(file!!.toUri())
                        .addOnProgressListener { uploadingTask ->
                            val progress = (100.0 * uploadingTask.bytesTransferred) / uploadingTask.totalByteCount
                            progressBar.visibility = View.VISIBLE
                        }
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                progressBar.visibility = View.GONE
                            }
                        }
                }
            }
        alertDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null) {
            if (requestCode == Constants.REQ_UNICORN_FILE) {
                val files = data.getStringArrayListExtra("filePaths")
                if (files != null && files.size > 0) {
                    file = File(files[0])
                    if (file != null) {
                        tvFilePath.text = file!!.name
                    }
                }
            }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    private fun toDownloadBookActivity() {
        val intent = Intent(this@ProfileActivity, BooksActivity::class.java)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sign_out -> {
                ToBooksApp.authInstance().signOut()
                finish()
            }
        }
        return true
    }
}