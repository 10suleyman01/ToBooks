package com.suleyman.tobooks.ui.fragment.books

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.suleyman.tobooks.R
import com.suleyman.tobooks.databinding.ActivityBooksBinding
import com.suleyman.tobooks.databinding.EnterTheNameSubcategoryBinding
import com.suleyman.tobooks.model.BookModel
import com.suleyman.tobooks.ui.activity.books.BooksActivity
import com.suleyman.tobooks.ui.activity.upload.UploadFileActivity
import com.suleyman.tobooks.ui.activity.upload.UploadFileViewModel
import com.suleyman.tobooks.ui.fragment.books.viewmodel.BookCreateDirectoryViewModel
import com.suleyman.tobooks.ui.fragment.books.viewmodel.BookLoadDirectoriesViewModel
import com.suleyman.tobooks.utils.Common
import com.suleyman.tobooks.utils.Common.rotate
import com.suleyman.tobooks.utils.IBackPressed
import com.suleyman.tobooks.utils.StorageWalker
import com.suleyman.tobooks.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BooksFragment : Fragment(), IBackPressed, SearchView.OnQueryTextListener {

    companion object {
        private const val TAG = "BooksFragment"
        const val EXTRA_CATEGORY = "CATEGORY"

        const val REQUEST_CHECK_PERMISSIONS = 1263
        const val REQUEST_UPLOAD_FILE = 1663
        val PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

    }

    private lateinit var searchView: SearchView

    lateinit var rvAdapter: BooksAdapter

    @Inject
    lateinit var utils: Utils

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var storage: StorageWalker

    @Inject
    lateinit var storageReference: StorageReference

    private val books = mutableListOf<BookModel>()
    private var booksFiltered = mutableListOf<BookModel>()

    private var isFabOpenActions = false

    private var _binding: ActivityBooksBinding? = null
    private val binding get() = _binding!!

    private val bookLoadDirectoriesViewModel: BookLoadDirectoriesViewModel by viewModels()
    private val bookCreateDirectoryViewModel: BookCreateDirectoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().title = getString(R.string.books)
    }

    private fun checkFabIsUse() {
        binding.fabOpenActions.isVisible = storage.pathsIsNotEmpty()
        binding.fabAddNewFile.isVisible = storage.pathsIsNotEmpty()
        binding.fabAddNewCategory.isVisible = storage.pathsIsNotEmpty()
    }

    private fun handleFabClick() {
        binding.fabAddNewFile.setOnClickListener {
            Intent(requireActivity(), UploadFileActivity::class.java)
                .apply {
                    putExtra(EXTRA_CATEGORY, storage.currentPath())
                }
                .also {
                    startActivityForResult(it, REQUEST_UPLOAD_FILE)
                }
        }

        binding.fabAddNewCategory.setOnClickListener {
            createNewSubCategory()
        }

        if (!isFabOpenActions) {
            isFabOpenActions = true
            binding.fabAddNewFile.animate().translationY(-resources.getDimension(R.dimen.mg65dp))
            binding.fabAddNewCategory.animate()
                .translationY(-resources.getDimension(R.dimen.mg130dp))
            rotate(binding.fabOpenActions, 135.0f)
        } else {
            isFabOpenActions = false
            rotate(binding.fabOpenActions, 0f)
            binding.fabAddNewFile.animate().translationY(0f)
            binding.fabAddNewCategory.animate().translationY(0f)
        }
    }

    private fun handleOnClickOnCategory(book: BookModel) {
        if (book.type == BookModel.Type.CATEGORY) {
            val parent = book.path.toString()
            val bookTitle = book.title.toString()
            requireActivity().title = bookTitle
            booksFiltered.clear()

            showProgressLoadingList(true, withClear = true)

            storage.goTo("", parent) { result ->
                loadBooks(result)
                checkFabIsUse()
            }
        }
    }

    private fun createNewSubCategory() {

        val enterTheNameSubcategory = EnterTheNameSubcategoryBinding.inflate(layoutInflater)
        val etSubcategoryName = enterTheNameSubcategory.etSubcategoryName

        Common.showAlertDialog(
            context = requireContext(),
            title = getString(R.string.create_subcategory),
            message = getString(R.string.enter_the_name),
            view = enterTheNameSubcategory.root
        )
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
            .setPositiveButton(R.string.create) { _, _ ->
                val name = etSubcategoryName.text.toString()
                bookCreateDirectoryViewModel.createNewDirectory(
                    "",
                    name,
                    currentDirectory = storage.currentPath(),
                    storageReference
                )
                lifecycleScope.launch {
                    bookCreateDirectoryViewModel.bookUiState.collect { createState ->
                        when (createState) {
                            is BookCreateDirectoryViewModel.CreateDirectoryState.Success -> {
                                rvAdapter.clearWithNotify()
                                loadBooks(createState.result)
                            }
                            is BookCreateDirectoryViewModel.CreateDirectoryState.Loading -> {
                                showProgressLoadingList(createState.loading, withClear = false)
                            }
                            is BookCreateDirectoryViewModel.CreateDirectoryState.Error -> {
                                utils.toast(createState.message)
                            }
                            else -> UploadFileViewModel.UploadState.Empty
                        }
                    }
                }
            }.show()

    }

    private fun loadRootDirectories() {
        bookLoadDirectoriesViewModel.loadStorage("", storageReference)

        lifecycleScope.launchWhenStarted {
            bookLoadDirectoriesViewModel.bookUiState.collect {
                when (it) {
                    is BookLoadDirectoriesViewModel.LoadDirectoriesState.Success -> {
                        loadBooks(it.result)
                        storage.setIsRoot(true)
                    }
                    is BookLoadDirectoriesViewModel.LoadDirectoriesState.Error -> {
                        utils.toast(it.message)
                    }
                    is BookLoadDirectoriesViewModel.LoadDirectoriesState.Loading -> {
                        binding.progressBarUploadTask.isVisible = it.isLoading
                    }
                    else -> UploadFileViewModel.UploadState.Empty
                }
            }
        }
    }

    private fun loadBooks(result: ListResult, withClear: Boolean? = false) {
        Common.loadDataNew(books, result,
            onCompleted = { booksResult ->
                withClear?.let {
                    showProgressLoadingList(isShow = false, it)
                }
                rvAdapter.setBooks(booksResult)
            })
    }

    private fun showProgressLoadingList(isShow: Boolean, withClear: Boolean = false) {
        if (withClear)
            rvAdapter.clearWithNotify()
        binding.progressBarUploadTask.isVisible = isShow
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityBooksBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as BooksActivity).setListener(object : IBackPressed {
            override fun onBackPressed(): Boolean {
                return this@BooksFragment.onBackPressed()
            }
        })

        rvAdapter = BooksAdapter(this, requireContext(), utils, firestore)

        binding.rvBooksView.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvBooksView.setHasFixedSize(true)

        loadRootDirectories()

        rvAdapter.addListener(object : BooksAdapter.OnClickListener {
            override fun onClick(book: BookModel, position: Int) {
                handleOnClickOnCategory(book)
            }
        })

        binding.rvBooksView.adapter = rvAdapter
        binding.fabOpenActions.setOnClickListener {
            handleFabClick()
        }
        checkFabIsUse()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null) {
            if (requestCode == REQUEST_UPLOAD_FILE && resultCode == RESULT_OK) {
                rvAdapter.clearWithNotify()
                Common.loadDataFromCategory(
                    "",
                    books,
                    storageReference,
                    data.getStringExtra(EXTRA_CATEGORY)!!,
                    onCompleted = {
                        rvAdapter.setBooks(it)
                    }
                )
            }

        } else super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let {
            if (newText.isNotEmpty()) {
                books.filter { book -> isBookTitleContainsIsNewText(book.title!!, newText) }
                    .apply {
                        rvAdapter.setBooks(this as MutableList<BookModel>)
                    }
            } else {
                storage.backToCurrent { loadBooks(it) }
            }
        }
        return false
    }

    private fun isBookTitleContainsIsNewText(title: String, newText: String): Boolean {
        return title.toLowerCase(Locale.ROOT).contains(newText.toLowerCase(Locale.ROOT))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_books, menu)

        val searchItem = menu.findItem(R.id.search)
        searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return true
    }

    override fun onBackPressed(): Boolean {
        if (searchView.isActivated) {
            searchView.dispatchSetActivated(false)
            return true
        }

        if (storage.isRootPath()) {
            requireActivity().title = storage.categoryTitle()
            return false
        }

        showProgressLoadingList(true, withClear = true)
        storage.backTo { result ->
            if (storage.isRootPath()) {
                toRootConfig()
                checkFabIsUse()
            }
            requireActivity().title = storage.categoryTitle()
            loadBooks(result)
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun toRootConfig() {
        isFabOpenActions = false
        requireActivity().title = getString(R.string.books)
        binding.fabAddNewFile.animate().translationY(0f)
        binding.fabAddNewCategory.animate().translationY(0f)
        rotate(binding.fabOpenActions, 0f)
    }
}