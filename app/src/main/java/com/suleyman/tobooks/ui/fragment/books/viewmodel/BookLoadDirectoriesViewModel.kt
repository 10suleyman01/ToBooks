package com.suleyman.tobooks.ui.fragment.books.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.suleyman.tobooks.R
import com.suleyman.tobooks.utils.NetworkHelper
import com.suleyman.tobooks.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class BookLoadDirectoriesViewModel @Inject constructor(
    val utils: Utils,
    val networkHelper: NetworkHelper
): ViewModel() {

    private val _booksUiState = MutableStateFlow<LoadDirectoriesState>(LoadDirectoriesState.Empty)
    var bookUiState: StateFlow<LoadDirectoriesState> = _booksUiState

    fun loadRootStorage(storageReference: StorageReference) {
        _booksUiState.value = LoadDirectoriesState.Loading
        if (networkHelper.isNetworkConnected()) {
            storageReference.listAll()
                .addOnSuccessListener { result ->
                    _booksUiState.value = LoadDirectoriesState.Success(result)
                }
                .addOnFailureListener {
                    _booksUiState.value = LoadDirectoriesState.Error(it.message!!)
                }
        } else {
            _booksUiState.value = LoadDirectoriesState.Error(utils.getString(R.string.not_connected))
        }
    }

    sealed class LoadDirectoriesState {
        data class Success(val result: ListResult): LoadDirectoriesState()
        data class Error(val message: String): LoadDirectoriesState()
        object Loading: LoadDirectoriesState()
        object Empty: LoadDirectoriesState()
    }

}