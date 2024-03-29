package com.suleyman.tobooks.ui.activity.upload

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DatabaseReference
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
import java.util.*
import javax.inject.Inject

@HiltViewModel
class UploadBookViewModel @Inject constructor(
    val utils: Utils,
    val networkHelper: NetworkHelper,
    val firestore: FirebaseFirestore,
    val storageReference: StorageReference,
    val databaseReference: DatabaseReference
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Empty)
    val uploadState: StateFlow<UploadState> = _uploadState

    fun uploadFile(
        bucket: String? = "",
        category: String,
        file: File,
        data: HashMap<String, String>,
    ) {
        val storage = bucket?.let {
            if (it.isEmpty()) storageReference
            else storageReference.child(bucket)
        }

        if (networkHelper.isNetworkConnected()) {
            storage?.child(category)?.child(data["name"] ?: "")
                ?.putFile(file.toUri())
                ?.addOnProgressListener { uploadingTask ->
                    val progress =
                        (100 * uploadingTask.bytesTransferred) / uploadingTask.totalByteCount
//                    _uploadState.value = UploadState.Progress(progress.toInt())
                    _uploadState.value = UploadState.Loading
                }
                ?.addOnFailureListener {
                    _uploadState.value =
                        UploadState.Error(utils.getString(R.string.error_uploading))
                }
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        firestore.collection(FirestoreConfig.COLLECTION)
                            .document(data[FirestoreConfig.NAME] ?: "")
                            .set(data)
                            .addOnSuccessListener {
                                _uploadState.value = UploadState.Success
                            }
                            .addOnFailureListener {
                                it.printStackTrace()
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