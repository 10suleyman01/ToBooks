package com.suleyman.tobooks.ui.activity.books

import abhishekti7.unicorn.filepicker.utils.Constants
import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.suleyman.tobooks.R
import com.suleyman.tobooks.app.Common
import com.suleyman.tobooks.app.Common.loadData
import com.suleyman.tobooks.app.ToBooksApp
import com.suleyman.tobooks.databinding.ActivityBooksBinding
import com.suleyman.tobooks.databinding.EnterTheNameSubcategoryBinding
import com.suleyman.tobooks.databinding.SelectBookViewBinding
import com.suleyman.tobooks.model.BookModel
import kotlinx.coroutines.flow.collect
import me.aflak.libraries.BookModelFilter
import java.io.File
import java.util.*

class BooksActivity : AppCompatActivity(), View.OnClickListener,
    SearchView.OnQueryTextListener {

    companion object {
        private const val REQUEST_CHECK_PERMISSIONS = 1263
    }

    private lateinit var searchView: SearchView
    private lateinit var rvAdapter: BooksAdapter

    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private val books = mutableListOf<BookModel>()
    private var booksFiltered = mutableListOf<BookModel>()
    private var currentCategoryStack = mutableListOf<String>()

    private lateinit var tvFilePath: TextView

    private var isRoot = false
    private var uploadFile: File? = null
    private var isFabOpenActions = false

    private var _binding: ActivityBooksBinding? = null
    private val binding get() = _binding!!

    private val bookUploadViewModel: BookUploadViewModel by viewModels()
    private val bookLoadDirectoriesViewModel: BookLoadDirectoriesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.include.toolbar)

        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage.reference

        loadRootDirectories()

        binding.rvBooksView.layoutManager = LinearLayoutManager(this)
        binding.rvBooksView.setHasFixedSize(true)

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
        binding.rvBooksView.adapter = rvAdapter

        binding.fabOpenActions.setOnClickListener {
            handleFabClick()
        }

        checkFabIsUse()

    }

    private fun handleFabClick() {

        binding.fabAddNewFile.setOnClickListener {
            selectFileInStorage(currentCategoryStack.last())
        }

        binding.fabAddNewCategory.setOnClickListener {
            createNewSubCategory()
        }

        if (!isFabOpenActions) {
            isFabOpenActions = true
            binding.fabAddNewFile.animate().translationY(-resources.getDimension(R.dimen.mg65dp))
            binding.fabAddNewCategory.animate().translationY(-resources.getDimension(R.dimen.mg130dp))
            rotate(135.0f)
        } else {
            isFabOpenActions = false
            rotate(0f)
            binding.fabAddNewFile.animate().translationY(0f)
            binding.fabAddNewCategory.animate().translationY(0f)
        }

    }

    private fun rotate(value: Float) {
        ViewCompat.animate(binding.fabOpenActions)
            .rotation(value)
            .withLayer()
            .setDuration(300L)
            .setInterpolator(OvershootInterpolator(10.0f))
            .start()
    }

    private fun handleOnClickOnFile(book: BookModel) {
        if (book.type == BookModel.Type.CATEGORY) {
            val parent = book.parent.toString()
            val bookTitle = book.title.toString()
            title = bookTitle
            booksFiltered.clear()
            showProgressLoadingList(true, withClear = true)
            currentCategoryStack.add(parent)
            rootStorage().child(parent)
                .listAll()
                .addOnSuccessListener { result ->
                    loadBooks(result)
                }.addOnCompleteListener {
                    checkFabIsUse()
                }

        }
    }

    private fun loadRootDirectories() {

        bookLoadDirectoriesViewModel.loadRootStorage(rootStorage())

        lifecycleScope.launchWhenStarted {
            bookLoadDirectoriesViewModel.bookUiState.collect {
                when (it) {
                    is BookLoadDirectoriesViewModel.LoadDirectoriesState.Success -> {
                        loadBooks(it.result)
                    }
                    is BookLoadDirectoriesViewModel.LoadDirectoriesState.Error -> {
                        ToBooksApp.toast(it.message)
                    }
                    is BookLoadDirectoriesViewModel.LoadDirectoriesState.Loading -> {
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun loadBooks(result: ListResult) {
        loadData(books, result, onCompleted = { booksResult ->
            rvAdapter.setBooks(booksResult)
            showProgressLoadingList(isShow = false, withClear = false)
        })
    }

    private fun selectFileInStorage(category: String) {
        val selectBookView = SelectBookViewBinding.inflate(layoutInflater)

        tvFilePath = selectBookView.tvFilePath
        val btnSelect = selectBookView.btnSelectBook

        btnSelect.setOnClickListener {
            Common.startFilePickerActivity(this@BooksActivity)
        }

        Common.showAlertDialog(
            this,
            title = getString(R.string.upload),
            message = getString(R.string.set_upload_file),
            view = selectBookView.root
        ).setPositiveButton(R.string.upload_btn_text) { _, _ ->
            if (uploadFile != null) uploadFile(category)
        }.show()
    }

    override fun onClick(v: View?) {
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText.toString().isEmpty()) {
            booksFiltered.clear()
            rvAdapter.setBooks(books)
        } else {
            booksFiltered = BookModelFilter.builder()
                .title()
                .contains(newText.toString())
                .on(books)
            rvAdapter.setBooks(booksFiltered)
        }
        return false
    }

    private fun createNewSubCategory() {

        val enterTheNameSubcategory = EnterTheNameSubcategoryBinding.inflate(layoutInflater)

        val etSubcategoryName =
            enterTheNameSubcategory.etSubcategoryName

        Common.showAlertDialog(
            this,
            title = getString(R.string.create_subcategory),
            view = enterTheNameSubcategory.root
        ).setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
            .setPositiveButton(R.string.create) { _, _ ->
                val name = etSubcategoryName.text.toString()
                if (name.isNotEmpty()) {
                    // create new folder on storage
                    val file = File("test.txt")
                    rootStorage().child(
                        currentCategoryStack.last()
                    ).child(name).putFile(
                        file.toUri()
                    )
                }
            }.show()
    }

    private fun uploadFile(category: String) {

        bookUploadViewModel.uploadFile(
            rootStorage(),
            category,
            uploadFile!!
        )

        lifecycleScope.launchWhenStarted {
            bookUploadViewModel.bookUiState.collect {
                when (it) {
                    is BookUploadViewModel.BookUploadFileState.Success -> {
                        binding.progressBarUploadTask.isVisible = false
                        loadBooks(it.result)
                    }
                    is BookUploadViewModel.BookUploadFileState.Error -> {
                        ToBooksApp.toast(it.message)
                    }
                    is BookUploadViewModel.BookUploadFileState.Progress -> {
                        binding.progressBarUploadTask.isVisible = true
                        binding.progressBarUploadTask.max = 100
                        binding.progressBarUploadTask.progress = it.progress
                    }
                    is BookUploadViewModel.BookUploadFileState.Loading -> {
                        showProgressLoadingList(isShow = true, withClear = true)
                    }
                    else -> Unit
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null) {
            if (requestCode == Constants.REQ_UNICORN_FILE) {
                val files = data.getStringArrayListExtra("filePaths")
                if (!files.isNullOrEmpty()) {
                    uploadFile = File(files.first())
                    if (uploadFile != null) {
                        tvFilePath.text = uploadFile!!.name
                    }
                }
            }
        } else super.onActivityResult(requestCode, resultCode, data)
    }


    private fun showProgressLoadingList(isShow: Boolean, withClear: Boolean) {
        if (withClear) {
            rvAdapter.clear()
        }
        binding.progressBarBooks.isVisible = isShow
    }

    private fun checkFabIsUse() {
        binding.fabOpenActions.isVisible = currentCategoryStack.isNotEmpty()
        binding.fabAddNewFile.isVisible = currentCategoryStack.isNotEmpty()
        binding.fabAddNewCategory.isVisible = currentCategoryStack.isNotEmpty()
    }

    override fun onBackPressed() {

        if (searchView.isActivated) {
            searchView.dispatchSetActivated(false)
        }

        showProgressLoadingList(true, withClear = true)
        if (currentCategoryStack.isNotEmpty() && currentCategoryStack.size > 1) {
            currentCategoryStack.removeLast()
            val lastCategory = currentCategoryStack.last()
            val lastIndex = lastCategory.lastIndexOf("/")
            title = lastCategory.substring(lastIndex + 1)
            rootStorage().child(lastCategory)
                .listAll()
                .addOnSuccessListener { result ->
                    loadBooks(result)
                }
        } else if (!isRoot && currentCategoryStack.size == 1) {
            currentCategoryStack.removeLast()
            isRoot = true
            title = getString(R.string.app_name)
            rootStorage().listAll()
                .addOnSuccessListener { result ->
                    loadBooks(result)
                    isRoot = false
                    binding.fabAddNewFile.animate().translationY(0f)
                    binding.fabAddNewCategory.animate().translationY(0f)
                    rotate(0f)
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

        val searchItem = menu?.findItem(R.id.search)
        searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(this)

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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}