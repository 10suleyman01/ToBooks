package com.suleyman.tobooks.ui.activity.books

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.suleyman.tobooks.R
import com.suleyman.tobooks.app.ToBooksApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class BookUploadViewModel : ViewModel() {

    private val _booksUiState = MutableStateFlow<BookUploadFileState>(BookUploadFileState.Empty)
    var bookUiState: StateFlow<BookUploadFileState> = _booksUiState

    fun uploadFile(storageReference: StorageReference, category: String, file: File) {
        storageReference.child(category).child(file.name)
            .putFile(file.toUri())
            .addOnProgressListener { uploadingTask ->
                val progress =
                    (100 * uploadingTask.bytesTransferred) / uploadingTask.totalByteCount
                _booksUiState.value = BookUploadFileState.Progress(progress.toInt())
            }
            .addOnFailureListener {
                _booksUiState.value = BookUploadFileState.Error(ToBooksApp.getString(R.string.error_uploading))
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _booksUiState.value = BookUploadFileState.Loading
                    storageReference.child(category)
                        .listAll()
                        .addOnSuccessListener { result ->
                            _booksUiState.value = BookUploadFileState.Success(result)
                        }
                }
            }
    }

    sealed class BookUploadFileState {
        data class Success(val result: ListResult) : BookUploadFileState()
        data class Error(val message: String) : BookUploadFileState()
        data class Progress(val progress: Int) : BookUploadFileState()
        object Loading : BookUploadFileState()
        object Empty : BookUploadFileState()
    }

}