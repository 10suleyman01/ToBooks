package com.suleyman.tobooks.ui.fragment.books.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BookLoadDirectoriesViewModel: ViewModel() {

    private val _booksUiState = MutableStateFlow<LoadDirectoriesState>(LoadDirectoriesState.Empty)
    var bookUiState: StateFlow<LoadDirectoriesState> = _booksUiState

    fun loadRootStorage(storageReference: StorageReference) {
        _booksUiState.value = LoadDirectoriesState.Loading
        storageReference.listAll()
            .addOnSuccessListener { result ->
                _booksUiState.value = LoadDirectoriesState.Success(result)
            }
            .addOnFailureListener {
                _booksUiState.value = LoadDirectoriesState.Error(it.message!!)
            }
    }

    sealed class LoadDirectoriesState {
        data class Success(val result: ListResult): LoadDirectoriesState()
        data class Error(val message: String): LoadDirectoriesState()
        object Loading: LoadDirectoriesState()
        object Empty: LoadDirectoriesState()
    }

}