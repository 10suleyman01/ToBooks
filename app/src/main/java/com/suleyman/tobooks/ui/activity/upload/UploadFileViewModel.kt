package com.suleyman.tobooks.ui.activity.upload

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.suleyman.tobooks.R
import com.suleyman.tobooks.utils.FirestoreConfig
import com.suleyman.tobooks.utils.NetworkHelper
import com.suleyman.tobooks.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.HashMap
import javax.inject.Inject

@HiltViewModel
class UploadFileViewModel @Inject constructor(
    val utils: Utils,
    val networkHelper: NetworkHelper,
    val firestore: FirebaseFirestore,
    val storageReference: StorageReference
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Empty)
    val uploadState: StateFlow<UploadState> = _uploadState

    fun uploadFile(
        category: String,
        file: File,
        data: HashMap<String, String>
    ) {
        if (networkHelper.isNetworkConnected()) {
            storageReference.child(category).child(data["name"] ?: "")
                .putFile(file.toUri())
                .addOnProgressListener { uploadingTask ->
                    val progress =
                        (100 * uploadingTask.bytesTransferred) / uploadingTask.totalByteCount
//                    _uploadState.value = UploadState.Progress(progress.toInt())
                    _uploadState.value = UploadState.Loading
                }
                .addOnFailureListener {
                    _uploadState.value =
                        UploadState.Error(utils.getString(R.string.error_uploading))
                }
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        firestore.collection(FirestoreConfig.COLLECTION)
                            .document(data[FirestoreConfig.NAME] ?: "")
                            .set(data)
                            .addOnSuccessListener {
                                utils.toast("Success")
                                _uploadState.value = UploadState.Success
                            }
                            .addOnFailureListener {
                                utils.toastLong("Error ${it.localizedMessage}")
                            }
                    }
                }
        } else {
            _uploadState.value = UploadState.Error(utils.getString(R.string.not_connected))
        }
    }

    sealed class UploadState {
        object Success : UploadState()
        data class Error(val message: String) : UploadState()
        data class Progress(val progress: Int) : UploadState()
        object Loading : UploadState()
        object Empty : UploadState()

    }

}