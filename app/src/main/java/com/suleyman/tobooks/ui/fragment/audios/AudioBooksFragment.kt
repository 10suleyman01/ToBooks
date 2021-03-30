package com.suleyman.tobooks.ui.fragment.audios

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.suleyman.tobooks.R
import com.suleyman.tobooks.databinding.EnterTheNameSubcategoryBinding
import com.suleyman.tobooks.databinding.FragmentAudioBooksBinding
import com.suleyman.tobooks.model.BookModel
import com.suleyman.tobooks.ui.activity.books.BooksActivity
import com.suleyman.tobooks.ui.activity.upload.UploadFileViewModel
import com.suleyman.tobooks.ui.fragment.books.BooksAdapter
import com.suleyman.tobooks.ui.fragment.books.viewmodel.BookCreateDirectoryViewModel
import com.suleyman.tobooks.ui.fragment.books.viewmodel.BookLoadDirectoriesViewModel
import com.suleyman.tobooks.utils.Common
import com.suleyman.tobooks.utils.Common.AUDIOS_BUCKET
import com.suleyman.tobooks.utils.IBackPressed
import com.suleyman.tobooks.utils.StorageWalker
import com.suleyman.tobooks.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AudioBooksFragment : Fragment(), IBackPressed, SearchView.OnQueryTextListener {

    @Inject
    lateinit var utils: Utils

    @Inject
    lateinit var storage: StorageWalker

    @Inject
    lateinit var storageReference: StorageReference

    private var _binding: FragmentAudioBooksBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchView: SearchView

    private lateinit var rvAdapter: AudioBooksAdapter

    private val books = mutableListOf<BookModel>()
    private var booksFiltered = mutableListOf<BookModel>()

    private var isFabOpenActions = false

    private val bookLoadDirectoriesViewModel: BookLoadDirectoriesViewModel by viewModels()
    private val bookCreateDirectoryViewModel: BookCreateDirectoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioBooksBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as BooksActivity).setListener(object : IBackPressed {
            override fun onBackPressed(): Boolean {
                val isBack = this@AudioBooksFragment.onBackPressed()
                return isBack
            }
        })

        requireActivity().title = getString(R.string.audio_books)

        rvAdapter = AudioBooksAdapter()
        rvAdapter.addListener(object : BooksAdapter.OnClickListener {
            override fun onClick(book: BookModel, position: Int) {
                handleOnClickOnCategory(book)
            }
        })

        binding.fabOpenActions.setOnClickListener {
            handleFabClick()
        }

        with(binding) {
            rvAudios.layoutManager = LinearLayoutManager(requireActivity())
            rvAudios.adapter = rvAdapter
        }

        lifecycleScope.launchWhenStarted {
            bookLoadDirectoriesViewModel.loadStorage(AUDIOS_BUCKET, storageReference)

            bookLoadDirectoriesViewModel.bookUiState.collect { states ->
                when (states) {
                    is BookLoadDirectoriesViewModel.LoadDirectoriesState.Success -> {
                        loadBooks(states.result)
                        storage.setIsRoot(true)
                    }
                    is BookLoadDirectoriesViewModel.LoadDirectoriesState.Error -> {
                        utils.toast(states.message)
                    }
                    is BookLoadDirectoriesViewModel.LoadDirectoriesState.Loading -> {
                        binding.loadingContent.isVisible = states.isLoading
                    }
                    else -> UploadFileViewModel.UploadState.Empty
                }
            }

        }

        checkFabIsUse()
    }

    private fun handleFabClick() {
        binding.fabAddNewFile.setOnClickListener {
//            Intent(requireActivity(), UploadFileActivity::class.java)
//                .apply {
//                    putExtra(BooksFragment.EXTRA_CATEGORY, storage.currentPath())
//                }
//                .also {
//                    startActivityForResult(it, BooksFragment.REQUEST_UPLOAD_FILE)
//                }
        }

        binding.fabAddNewCategory.setOnClickListener {
            createNewSubCategory()
        }

        if (!isFabOpenActions) {
            isFabOpenActions = true
            binding.fabAddNewFile.animate().translationY(-resources.getDimension(R.dimen.mg65dp))
            binding.fabAddNewCategory.animate()
                .translationY(-resources.getDimension(R.dimen.mg130dp))
            Common.rotate(binding.fabOpenActions, 135.0f)
        } else {
            isFabOpenActions = false
            Common.rotate(binding.fabOpenActions, 0f)
            binding.fabAddNewFile.animate().translationY(0f)
            binding.fabAddNewCategory.animate().translationY(0f)
        }
    }

    private fun checkFabIsUse() {
        binding.fabOpenActions.isVisible = storage.pathsIsNotEmpty()
        binding.fabAddNewFile.isVisible = storage.pathsIsNotEmpty()
        binding.fabAddNewCategory.isVisible = storage.pathsIsNotEmpty()
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
                    AUDIOS_BUCKET,
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

    private fun handleOnClickOnCategory(book: BookModel) {
        if (book.type == BookModel.Type.CATEGORY) {

            val parent = book.path.toString()
            val bookTitle = book.title.toString()
            requireActivity().title = bookTitle
            booksFiltered.clear()

            showProgressLoadingList(true, withClear = true)

            storage.goTo(AUDIOS_BUCKET, parent) { result ->
                loadBooks(result)
                checkFabIsUse()
            }
        }
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

    private fun loadBooks(result: ListResult, withClear: Boolean? = false) {
        Common.loadDataNew(books, result,
            onCompleted = { booksResult ->
                withClear?.let {
                    showProgressLoadingList(isShow = false, it)
                }
                rvAdapter.setAudios(booksResult)
            })
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let {
            if (newText.isNotEmpty()) {
                books.filter { book -> isBookTitleContainsIsNewText(book.title!!, newText) }
                    .apply {
                        rvAdapter.setAudios(this as MutableList<BookModel>)
                    }
            } else {
                storage.backToCurrent(AUDIOS_BUCKET) { loadBooks(it) }
            }
        }
        return false
    }

    private fun isBookTitleContainsIsNewText(title: String, newText: String): Boolean {
        return title.toLowerCase(Locale.ROOT).contains(newText.toLowerCase(Locale.ROOT))
    }

    private fun showProgressLoadingList(
        isShow: Boolean,
        withClear: Boolean = false
    ) {
        if (withClear)
            rvAdapter.clearWithNotify()
        binding.loadingContent.isVisible = isShow
    }

    override fun onBackPressed(): Boolean {
        if (searchView.isActivated) {
            searchView.dispatchSetActivated(false)
            return true
        }

        requireActivity().title = storage.categoryTitle()
        showProgressLoadingList(true, withClear = true)

        if (storage.isRootPath()) {
            return false
        }

        storage.backTo(AUDIOS_BUCKET) { result ->
            if (storage.isRootPath()) {
                toRootConfig()
                checkFabIsUse()
            }
            loadBooks(result)
        }

        return true
    }

    private fun toRootConfig() {
        isFabOpenActions = false
        requireActivity().title = getString(R.string.audio_books)
        binding.fabAddNewFile.animate().translationY(0f)
        binding.fabAddNewCategory.animate().translationY(0f)
        Common.rotate(binding.fabOpenActions, 0f)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}