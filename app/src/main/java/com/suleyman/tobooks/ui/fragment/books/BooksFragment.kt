package com.suleyman.tobooks.ui.fragment.books

import abhishekti7.unicorn.filepicker.utils.Constants
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.suleyman.tobooks.R
import com.suleyman.tobooks.utils.Common
import com.suleyman.tobooks.utils.IBackPressed
import com.suleyman.tobooks.utils.Utils
import com.suleyman.tobooks.databinding.ActivityBooksBinding
import com.suleyman.tobooks.databinding.EnterTheNameSubcategoryBinding
import com.suleyman.tobooks.databinding.SelectBookViewBinding
import com.suleyman.tobooks.model.BookModel
import com.suleyman.tobooks.ui.activity.books.BooksActivity
import com.suleyman.tobooks.ui.activity.books.BooksAdapter
import com.suleyman.tobooks.ui.fragment.books.viewmodel.BookCreateDirectoryViewModel
import com.suleyman.tobooks.ui.fragment.books.viewmodel.BookLoadDirectoriesViewModel
import com.suleyman.tobooks.ui.fragment.books.viewmodel.BookUploadViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.aflak.libraries.BookModelFilter
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinApiExtension
import pub.devrel.easypermissions.EasyPermissions
import java.io.File

@KoinApiExtension
class BooksFragment : Fragment(), IBackPressed, SearchView.OnQueryTextListener,
    EasyPermissions.PermissionCallbacks {

    companion object {
        private const val REQUEST_CHECK_PERMISSIONS = 1263
    }

    private lateinit var searchView: SearchView
    private lateinit var rvAdapter: BooksAdapter

    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private lateinit var tvFilePath: TextView

    private val TAG = "BooksFragment"

    private val books = mutableListOf<BookModel>()
    private var booksFiltered = mutableListOf<BookModel>()
    private var currentCategoryStack = mutableListOf<String>()

    private var isRoot = false
    private var uploadFile: File? = null
    private var isFabOpenActions = false

    private var _binding: ActivityBooksBinding? = null
    private val binding get() = _binding!!

    private val utils: Utils by inject()

    @SuppressLint("InlinedApi")
    private val PERMISSIONS = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val bookUploadViewModel: BookUploadViewModel by viewModels()
    private val bookLoadDirectoriesViewModel: BookLoadDirectoriesViewModel by viewModels()
    private val bookCreateDirectoryViewModel: BookCreateDirectoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage.reference
    }

    private fun checkFabIsUse() {
        binding.fabOpenActions.isVisible = currentCategoryStack.isNotEmpty()
        binding.fabAddNewFile.isVisible = currentCategoryStack.isNotEmpty()
        binding.fabAddNewCategory.isVisible = currentCategoryStack.isNotEmpty()
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
            binding.fabAddNewCategory.animate()
                .translationY(-resources.getDimension(R.dimen.mg130dp))
            rotate(135.0f)
        } else {
            isFabOpenActions = false
            rotate(0f)
            binding.fabAddNewFile.animate().translationY(0f)
            binding.fabAddNewCategory.animate().translationY(0f)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        utils.toast("Дайте приложению доступ к памяти")
    }

    private fun selectFileInStorage(category: String) {

        val selectBookView = SelectBookViewBinding.inflate(layoutInflater)

        tvFilePath = selectBookView.tvFilePath
        val btnSelect = selectBookView.btnSelectBook

        btnSelect.setOnClickListener {
            if (EasyPermissions.hasPermissions(requireContext(), *PERMISSIONS)) {
                Common.startFilePickerActivity(this)
            } else {
                EasyPermissions.requestPermissions(
                    this,
                    "Дайте приложению доступ к памяти, чтобы загружать файлы",
                    REQUEST_CHECK_PERMISSIONS,
                    *PERMISSIONS
                )
            }
        }
        Common.showAlertDialog(
            requireContext(),
            title = getString(R.string.upload),
            message = getString(R.string.set_upload_file),
            view = selectBookView.root
        ).setPositiveButton(R.string.upload_btn_text) { _, _ ->
            if (uploadFile != null)
                uploadFile(category)
        }.show()
    }

    private fun handleOnClickOnFile(book: BookModel, position: Int) {
        if (book.type == BookModel.Type.CATEGORY) {
            val parent = book.path.toString()
            val bookTitle = book.title.toString()
            requireActivity().title = bookTitle
            booksFiltered.clear()
            Log.d(TAG, "handleOnClickOnFile: ${book.downloadUrl.toString()}")
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
                    name,
                    currentDirectory = currentCategoryStack.last(),
                    rootStorage()
                )
                lifecycleScope.launch {
                    bookCreateDirectoryViewModel.bookUiState.collect { createState ->
                        when (createState) {
                            is BookCreateDirectoryViewModel.CreateDirectoryState.Success -> {
                                rvAdapter.clear()
                                loadBooks(createState.result)
                            }
                            is BookCreateDirectoryViewModel.CreateDirectoryState.Loading -> {
                                showProgressLoadingList(createState.loading, withClear = false)
                            }
                            is BookCreateDirectoryViewModel.CreateDirectoryState.Error -> {
                                utils.toast(createState.message)
                            }
                            else -> Unit
                        }
                    }
                }
            }.show()

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
                        utils.toast(it.message)
                    }
                    is BookLoadDirectoriesViewModel.LoadDirectoriesState.Loading -> {
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun uploadFile(category: String) {

        uploadFile?.let { file ->
            bookUploadViewModel.uploadFile(
                rootStorage(),
                category,
                file
            )
        }

        lifecycleScope.launchWhenStarted {
            bookUploadViewModel.bookUiState.collect {
                when (it) {
                    is BookUploadViewModel.BookUploadFileState.Success -> {
                        binding.progressBarUploadTask.isVisible = false
                        loadBooks(it.result)
                    }
                    is BookUploadViewModel.BookUploadFileState.Error -> {
                        utils.toast(it.message)
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

    private fun loadBooks(result: ListResult, withClear: Boolean? = false) {
        Common.loadData(books, result,
            onCompleted = { booksResult ->
                rvAdapter.setBooks(booksResult)
                withClear?.let {
                    showProgressLoadingList(isShow = false, it)
                }
            })
    }

    private fun showProgressLoadingList(isShow: Boolean, withClear: Boolean) {
        if (withClear) {
            rvAdapter.clear()
        }
        binding.progressBarBooks.isVisible = isShow
    }

    private fun rotate(value: Float) {
        ViewCompat.animate(binding.fabOpenActions)
            .rotation(value)
            .withLayer()
            .setDuration(300L)
            .setInterpolator(OvershootInterpolator(10.0f))
            .start()
    }

    private fun rootStorage() = storageReference.root

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityBooksBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null) {
            if (requestCode == Constants.REQ_UNICORN_FILE) {
                val files = data.getStringArrayListExtra(Common.filePaths)
                if (!files.isNullOrEmpty()) {
                    uploadFile = File(files.first())
                    uploadFile?.let {
                        tvFilePath.text = it.name
                    }
                }
            }
        } else super.onActivityResult(requestCode, resultCode, data)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as BooksActivity).setListener(object : IBackPressed {
            override fun onBackPressed(): Boolean {
                return this@BooksFragment.onBackPressed()
            
            
            
            
            
            }
        })

        binding.rvBooksView.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvBooksView.setHasFixedSize(true)

        loadRootDirectories()

        rvAdapter = BooksAdapter(requireActivity())
        rvAdapter.addListener(object : BooksAdapter.OnClickListener {
            override fun onClick(book: BookModel, position: Int) {
                handleOnClickOnFile(book, position)
            }
        })
        binding.rvBooksView.adapter = rvAdapter

        binding.fabOpenActions.setOnClickListener {
            handleFabClick()
        }

        checkFabIsUse()
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
        showProgressLoadingList(true, withClear = true)
        if (currentCategoryStack.isNotEmpty() && currentCategoryStack.size > 1) {
            currentCategoryStack.removeLast()
            val lastCategory = currentCategoryStack.last()
            val lastIndex = lastCategory.lastIndexOf("/")
            requireActivity().title = lastCategory.substring(lastIndex + 1)
            rootStorage().child(lastCategory)
                .listAll()
                .addOnSuccessListener { result ->
                    loadBooks(result)
                }
            return true
        } else if (!isRoot && currentCategoryStack.size == 1) {
            currentCategoryStack.removeLast()
            isRoot = true
            requireActivity().title = getString(R.string.app_name)
            rootStorage().listAll()
                .addOnSuccessListener { result ->
                    loadBooks(result)
                    toRootConfig()
                }.addOnCompleteListener {
                    checkFabIsUse()
                }
            return true
        } else return false
    }

    private fun toRootConfig() {
        isRoot = false
        isFabOpenActions = false
        binding.fabAddNewFile.animate().translationY(0f)
        binding.fabAddNewCategory.animate().translationY(0f)
        rotate(0f)
    }
}