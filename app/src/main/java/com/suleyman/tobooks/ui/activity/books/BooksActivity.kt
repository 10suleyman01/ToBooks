package com.suleyman.tobooks.ui.activity.books

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.suleyman.tobooks.R
import com.suleyman.tobooks.app.ToBooksApp
import com.suleyman.tobooks.model.BookModel

@SuppressLint("NonConstantResourceId")
class BooksActivity : AppCompatActivity() {

    private val TAG = "BooksActivity"

    @BindView(R.id.rvBooksView)
    lateinit var rvBooksView: RecyclerView

    @BindView(R.id.progressBarBooks)
    lateinit var progressBarBooks: ProgressBar

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    lateinit var rvAdapter: BooksAdapter

    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private val books = mutableListOf<BookModel>()
    private var currentCategoryStack = mutableListOf<String>()

    private var isRoot = false

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
                    rvAdapter.clearAll()
                    val parent = book.parent.toString()
                    val bookTitle = book.title.toString()
                    title = bookTitle
                    progressBarBooks.visibility = View.VISIBLE
                    currentCategoryStack.add(parent)
                    storageReference.root.child(parent)
                        .listAll()
                        .addOnSuccessListener { result ->
                            loadData(result)
                        }
                }
            }
        })
        rvBooksView.adapter = rvAdapter

        storageReference.root.listAll()
            .addOnSuccessListener { result ->
                loadData(result)
            }
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
        progressBarBooks.visibility = View.GONE
    }

    override fun onBackPressed() {
        if (currentCategoryStack.isNotEmpty() && currentCategoryStack.size > 1) {
            progressBarBooks.visibility = View.VISIBLE
            rvAdapter.clearAll()
            currentCategoryStack.removeLast()
            val lastCategory = currentCategoryStack.last()
            val lastIndex = lastCategory.lastIndexOf("/")
            title = lastCategory.substring(lastIndex + 1)
            storageReference.root.child(lastCategory)
                .listAll()
                .addOnSuccessListener { result ->
                    loadData(result)
                }
        } else if (!isRoot && currentCategoryStack.size == 1) {
            currentCategoryStack.removeLast()
            isRoot = true
            title = getString(R.string.app_name)
            rvAdapter.clearAll()
            storageReference.root.listAll()
                .addOnSuccessListener { result ->
                    loadData(result)
                    isRoot = false
                }
        } else {
            super.onBackPressed()
        }
    }

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











