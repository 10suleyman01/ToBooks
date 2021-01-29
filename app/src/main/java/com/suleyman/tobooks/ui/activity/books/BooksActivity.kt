package com.suleyman.tobooks.ui.activity.books

import abhishekti7.unicorn.filepicker.UnicornFilePicker
import abhishekti7.unicorn.filepicker.utils.Constants
import android.Manifest
import android.annotation.SuppressLint
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
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.arellomobile.mvp.presenter.InjectPresenter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.suleyman.tobooks.R
import com.suleyman.tobooks.app.Common
import com.suleyman.tobooks.app.ToBooksApp
import com.suleyman.tobooks.base.BaseActivity
import com.suleyman.tobooks.model.BookModel
import java.io.File
import java.io.InputStream

@SuppressLint("NonConstantResourceId")
class BooksActivity : BaseActivity(), BookView, View.OnClickListener {

    private val TAG = "BooksActivity"
    private val REQUEST_CHECK_PERMISSIONS = 1263

    @BindView(R.id.rvBooksView)
    lateinit var rvBooksView: RecyclerView

    @BindView(R.id.progressBarBooks)
    lateinit var progressBarBooks: ProgressBar

    @BindView(R.id.progressBarUploadTask)
    lateinit var progressBarUploadTask: ProgressBar

    @BindView(R.id.fabAddNewFile)
    lateinit var fabAddNewFile: FloatingActionButton

    lateinit var rvAdapter: BooksAdapter

    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private val books = mutableListOf<BookModel>()
    private var currentCategoryStack = mutableListOf<String>()

    private lateinit var tvFilePath: TextView

    private var isRoot = false
    private var uploadFile: File? = null

    @InjectPresenter
    lateinit var booksPresenter: BooksPresenter

    override fun onActivityCreate(savedInstanceState: Bundle?) {

        booksPresenter.loadRoot()

        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage.reference

        rvBooksView.layoutManager = LinearLayoutManager(this)
        rvBooksView.setHasFixedSize(true)

        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), REQUEST_CHECK_PERMISSIONS
        )

        rvAdapter = BooksAdapter(this)
        rvAdapter.addListener(object : BooksAdapter.OnClickListener {
            override fun onClick(book: BookModel) {
                handleOnClickOnFile(book)
            }
        })
        rvBooksView.adapter = rvAdapter

        fabAddNewFile.setOnClickListener {
            handleFabClick()
        }

        checkFabIsUse()

    }

    override fun view(): Int = R.layout.activity_books

    private fun handleFabClick() {
        val selectAction = Common.inflateView(this, R.layout.select_action_view)

        val btnCreateSubCategory = selectAction.findViewById<Button>(R.id.btnCreateSubCategory)
        val btnUploadNewFile = selectAction.findViewById<Button>(R.id.btnUploadNewFile)

        btnCreateSubCategory.setOnClickListener(this)
        btnUploadNewFile.setOnClickListener(this)

        Common.showAlertDialog(
            this,
            title = getString(R.string.select_action),
            view = selectAction,
        ).setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }.show()
    }

    private fun handleOnClickOnFile(book: BookModel) {
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

    override fun loadRootDirectories() {
        rootStorage().listAll()
            .addOnSuccessListener { result ->
                loadData(result)
            }
    }

    private fun selectFileInStorage(category: String) {
        val selectBookView = Common.inflateView(this, R.layout.select_book_view)

        tvFilePath = selectBookView.findViewById(R.id.tvFilePath)
        val btnSelect = selectBookView.findViewById<Button>(R.id.btnSelectBook)

        btnSelect.setOnClickListener {
            Common.startFilePickerActivity(this@BooksActivity)
        }

        Common.showAlertDialog(
            this,
            title = getString(R.string.upload),
            message = getString(R.string.set_upload_file),
            view = selectBookView
        ).setPositiveButton(R.string.upload_btn_text) { _, _ ->
            if (uploadFile != null) uploadFile(category)
        }.show()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnCreateSubCategory -> {
                createNewSubCategory()
            }
            R.id.btnUploadNewFile -> {
                selectFileInStorage(currentCategoryStack.last())
            }
        }
    }

    private fun createNewSubCategory() {

        val enterTheNameSubcategory = Common.inflateView(this, R.layout.enter_the_name_subcategory)

        val etSubcategoryName =
            enterTheNameSubcategory.findViewById<EditText>(R.id.etSubcategoryName)

        Common.showAlertDialog(
            this,
            title = getString(R.string.create_subcategory),
            view = enterTheNameSubcategory
        ).setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
            .setPositiveButton(R.string.create) { _, _ ->
                val name = etSubcategoryName.text.toString()
                if (name.isNotEmpty()) {
                    rootStorage().child(
                        currentCategoryStack.last()
                    )
                }
            }.show()
    }

    private fun uploadFile(category: String) {
        progressBarUploadTask.visibility = View.VISIBLE
        progressBarUploadTask.max = 100
        rootStorage().child(category).child(uploadFile!!.name)
            .putFile(uploadFile!!.toUri())
            .addOnProgressListener { uploadingTask ->
                val progress =
                    (100 * uploadingTask.bytesTransferred) / uploadingTask.totalByteCount
                progressBarUploadTask.progress = progress.toInt()
            }
            .addOnFailureListener {
                ToBooksApp.toast(R.string.error_uploading)
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showProgressLoadingList(isShow = true, withClear = true)
                    rootStorage().child(category)
                        .listAll()
                        .addOnSuccessListener { result ->
                            progressBarUploadTask.visibility = View.GONE
                            loadData(result)
                        }
                }
            }
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
                    downloadUrl = file.downloadUrl,
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