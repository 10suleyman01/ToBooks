package com.suleyman.tobooks.ui.activity.books

import abhishekti7.unicorn.filepicker.UnicornFilePicker
import abhishekti7.unicorn.filepicker.utils.Constants
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.suleyman.tobooks.R
import com.suleyman.tobooks.app.ToBooksApp
import com.suleyman.tobooks.model.BookModel
import java.io.File

@SuppressLint("NonConstantResourceId")
class BooksActivity : AppCompatActivity() {

    private val TAG = "BooksActivity"

    @BindView(R.id.rvBooksView)
    lateinit var rvBooksView: RecyclerView

    @BindView(R.id.progressBarBooks)
    lateinit var progressBarBooks: ProgressBar

    @BindView(R.id.fabAddNewFile)
    lateinit var fabAddNewFile: FloatingActionButton

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    lateinit var rvAdapter: BooksAdapter

    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private val books = mutableListOf<BookModel>()
    private var currentCategoryStack = mutableListOf<String>()

    private lateinit var tvFilePath: TextView

    private var isRoot = false
    private var uploadFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_books)

        ButterKnife.bind(this)

        setSupportActionBar(toolbar)

        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage.reference

        rvBooksView.layoutManager = LinearLayoutManager(this)
        rvBooksView.setHasFixedSize(true)

        rvAdapter = BooksAdapter()
        rvAdapter.addListener(object : BooksAdapter.OnClickListener {
            override fun onClick(book: BookModel) {
                if (book.type == BookModel.Type.CATEGORY) {
                    val parent = book.parent.toString()
                    val bookTitle = book.title.toString()
                    title = bookTitle
                    showProgressLoadingList(true, withClear = true)
                    currentCategoryStack.add(parent)
                    rootStorage().child(parent)
                        .listAll()
                        .addOnSuccessListener { result ->
                            loadData(result)
                        }.addOnCompleteListener {
                            checkFabIsUse()
                        }

                }
            }
        })
        rvBooksView.adapter = rvAdapter

        fabAddNewFile.setOnClickListener {
            uploadFileOnStorage(currentCategoryStack.last())
        }

        checkFabIsUse()

        rootStorage().listAll()
            .addOnSuccessListener { result ->
                loadData(result)
            }
    }

    private fun uploadFileOnStorage(category: String) {
        val selectBookView = LayoutInflater.from(this).inflate(R.layout.select_book_view, null)

        tvFilePath = selectBookView.findViewById(R.id.tvFilePath)
        val btnSelect = selectBookView.findViewById<Button>(R.id.btnSelectBook)

        btnSelect.setOnClickListener {
            UnicornFilePicker.from(this@BooksActivity)
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
                if (uploadFile != null) {
                    rootStorage().child(category).child(uploadFile!!.name)
                        .putFile(uploadFile!!.toUri())
                        .addOnProgressListener { uploadingTask ->
                            val progress =
                                (100 * uploadingTask.bytesTransferred) / uploadingTask.totalByteCount
                            showProgressLoadingList(true, withClear = false)
                        }
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                showProgressLoadingList(true, withClear = true)
                                rootStorage().child(category)
                                    .listAll()
                                    .addOnSuccessListener { result ->
                                        loadData(result)
                                    }
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
                    uploadFile = File(files[0])
                    if (uploadFile != null) {
                        tvFilePath.text = uploadFile!!.name
                    }
                }
            }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    private fun loadData(result: ListResult) {
        result.prefixes.forEach { folder ->
            books.add(
                BookModel(
                    title = folder.name,
                    parent = folder.path,
                    type = BookModel.Type.CATEGORY
                )
            )
        }
        result.items.forEach { file ->
            books.add(
                BookModel(
                    title = file.name,
                    downloadUrl = file.downloadUrl.toString(),
                    type = BookModel.Type.BOOK
                )
            )
        }
        rvAdapter.setBooks(books)
        showProgressLoadingList(isShow = false, withClear = false)
    }

    private fun showProgressLoadingList(isShow: Boolean, withClear: Boolean) {
        if (withClear) {
            rvAdapter.clearAll()
        }
        if (isShow) progressBarBooks.visibility = View.VISIBLE
        else progressBarBooks.visibility = View.GONE
    }

    private fun checkFabIsUse() {
        if (currentCategoryStack.isNotEmpty()) {
            fabAddNewFile.visibility = View.VISIBLE
        } else fabAddNewFile.visibility = View.GONE
    }

    override fun onBackPressed() {
        showProgressLoadingList(true, withClear = true)
        if (currentCategoryStack.isNotEmpty() && currentCategoryStack.size > 1) {
            currentCategoryStack.removeLast()
            val lastCategory = currentCategoryStack.last()
            val lastIndex = lastCategory.lastIndexOf("/")
            title = lastCategory.substring(lastIndex + 1)
            rootStorage().child(lastCategory)
                .listAll()
                .addOnSuccessListener { result ->
                    loadData(result)
                }
        } else if (!isRoot && currentCategoryStack.size == 1) {
            currentCategoryStack.removeLast()
            isRoot = true
            title = getString(R.string.app_name)
            rootStorage().listAll()
                .addOnSuccessListener { result ->
                    loadData(result)
                    isRoot = false
                }.addOnCompleteListener {
                    checkFabIsUse()
                }
        } else {
            super.onBackPressed()
        }
    }

    private fun rootStorage() = storageReference.root

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_books, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {
            }
            R.id.sign_out -> {
                ToBooksApp.authInstance().signOut()
            }
        }
        return true
    }
}











