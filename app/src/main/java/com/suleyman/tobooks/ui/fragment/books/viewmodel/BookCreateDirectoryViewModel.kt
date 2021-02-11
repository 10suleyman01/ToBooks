package com.suleyman.tobooks.ui.fragment.books.viewmodel

import android.os.Environment
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.suleyman.tobooks.utils.Common
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class BookCreateDirectoryViewModel : ViewModel() {

    private val _booksUiState = MutableStateFlow<CreateDirectoryState>(CreateDirectoryState.Empty)
    var bookUiState: StateFlow<CreateDirectoryState> = _booksUiState

    fun createNewDirectory(
        name: String,
        currentDirectory: String,
        storageReference: StorageReference
    ) {
        if (name.isNotEmpty()) {
            val file =
                File("${Environment.getExternalStorageDirectory().absolutePath}/manifest${Common.extConfig}")
            val isCreated = file.createNewFile()
            if (isCreated) {
                storageReference.child(currentDirectory).child(name).child(file.name)
                    .putFile(file.toUri())
                    .addOnSuccessListener {
                        file.delete()
                    }
                    .addOnProgressListener {
                        _booksUiState.value = CreateDirectoryState.Loading(true)
                    }
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            storageReference.child(currentDirectory)
                                .listAll()
                                .addOnCompleteListener { task ->
                                    _booksUiState.value =
                                        CreateDirectoryState.Success(task.result!!)
                                    _booksUiState.value = CreateDirectoryState.Loading(false)
                                }
                        }
                    }
                    .addOnFailureListener {
                        _booksUiState.value = CreateDirectoryState.Error(it.message!!)
                    }

            }
        }
    }

    sealed class CreateDirectoryState {
        data class Success(val result: ListResult) : CreateDirectoryState()
        data class Error(val message: String) : CreateDirectoryState()
        data class Loading(val loading: Boolean) : CreateDirectoryState()
        object Empty : CreateDirectoryState()
    }

}