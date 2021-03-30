package com.suleyman.tobooks.ui.fragment.books.viewmodel

import android.os.Environment
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.suleyman.tobooks.R
import com.suleyman.tobooks.utils.Common
import com.suleyman.tobooks.utils.NetworkHelper
import com.suleyman.tobooks.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject

@HiltViewModel
class BookCreateDirectoryViewModel @Inject constructor(
    val utils: Utils,
    val networkHelper: NetworkHelper
) : ViewModel() {

    private val _booksUiState = MutableStateFlow<CreateDirectoryState>(CreateDirectoryState.Empty)
    var bookUiState: StateFlow<CreateDirectoryState> = _booksUiState

    fun createNewDirectory(
        bucket: String? = "",
        name: String,
        currentDirectory: String,
        storageReference: StorageReference
    ) {
        if (name.isNotEmpty()) {
            val file =
                File("${Environment.getExternalStorageDirectory().absolutePath}/manifest${Common.extConfig}")
            if (file.createNewFile()) {
                if (networkHelper.isNetworkConnected()) {
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

                } else {
                    _booksUiState.value = CreateDirectoryState.Error(utils.getString(R.string.not_connected))
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