package com.suleyman.tobooks.ui.activity.upload

import abhishekti7.unicorn.filepicker.utils.Constants
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.suleyman.tobooks.databinding.ActivityUploadBinding
import com.suleyman.tobooks.ui.fragment.books.BooksFragment
import com.suleyman.tobooks.utils.Common
import com.suleyman.tobooks.utils.FirestoreConfig
import com.suleyman.tobooks.utils.Utils
import com.suleyman.tobooks.utils.textString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class UploadFileActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    companion object {
        const val REQUEST_GET_IMAGE = 2221
    }

    private var _binding: ActivityUploadBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var utils: Utils

    private val TAG = "UploadFileActivity"

    private val uploadFileViewModel: UploadFileViewModel by viewModels()

    private var uploadFile: File? = null
    private var isSelectedImage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val category = intent.extras?.getString(BooksFragment.EXTRA_CATEGORY)!!

        with(binding) {

            fabSelectImageBook.setOnClickListener {
                selectImageFromDevice()
            }

            btnSelectFile.setOnClickListener {
                selectFileInStorage()
            }

            binding.fabUpload.setOnClickListener {

                val bookName = etBookName.textString()
                val bookAuthor = etAuthorName.textString()

                val imageBytes = utils.fromBitmap(imgBook.drawable.toBitmap())

                Log.d(TAG, "onCreate: imgBytes = ${imageBytes.contentToString().length}")
                val base64 = Base64.encodeToString(imageBytes, Base64.DEFAULT)
                Log.d(TAG, "onCreate: base64 = ${base64.length}")

                val data = hashMapOf(
                    FirestoreConfig.NAME to bookName,
                    FirestoreConfig.AUTHOR to bookAuthor,
                    FirestoreConfig.PHOTO to base64
                )

                if (base64.length > 1_048_487) {
                    utils.toastLong("Выберите изображение низкого разрешения!")
                } else {
                    if (bookName.isNotEmpty() && bookAuthor.isNotEmpty() && isSelectedImage) {

                        uploadFile?.let { file ->
                            uploadFileViewModel.uploadFile(
                                category = category,
                                file = file,
                                data,
                            )
                        }
                        lifecycleScope.launchWhenStarted {

                            uploadFileViewModel.uploadState.collect {
                                when (it) {
                                    is UploadFileViewModel.UploadState.Success -> {
                                        isSelectedImage = false
                                        pbUploadLoading.isVisible = false
                                        val data = Intent()
                                        data.putExtra(BooksFragment.EXTRA_CATEGORY, category)
                                        setResult(RESULT_OK, data)
                                        finish()
                                    }
                                    is UploadFileViewModel.UploadState.Progress -> {

                                    }
                                    is UploadFileViewModel.UploadState.Loading -> {
                                        pbUploadLoading.isVisible = true
                                    }
                                    is UploadFileViewModel.UploadState.Error -> {
                                        pbUploadLoading.isVisible = false
                                        Log.d(TAG, "onCreate: ${it.message}")
                                    }
                                    else -> UploadFileViewModel.UploadState.Empty
                                }
                            }

                        }
                    }
                }
            }

        }

    }

    private fun selectFileInStorage() {
        if (EasyPermissions.hasPermissions(this, *BooksFragment.PERMISSIONS)) {
            Common.startFilePickerActivity(this)
        } else {
            requestPermissions("Дайте приложению доступ к памяти, чтобы загружать файлы")
        }
    }

    private fun selectImageFromDevice() {
        if (EasyPermissions.hasPermissions(this, *BooksFragment.PERMISSIONS)) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_GET_IMAGE)
        } else {
            requestPermissions("Дайте приложению доступ к памяти, чтобы загружать картинки")
        }
    }

    private fun requestPermissions(rationale: String) {
        EasyPermissions.requestPermissions(
            this,
            rationale,
            BooksFragment.REQUEST_CHECK_PERMISSIONS,
            *BooksFragment.PERMISSIONS
        )
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
                    uploadFile?.let { file ->
                        binding.tvSelectedFile.text = file.name
                    }
                }
            } else if (requestCode == REQUEST_GET_IMAGE) {
                val uri = data.data
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                Glide.with(this)
                    .load(bitmap)
                    .fitCenter()
                    .into(binding.imgBook)
                isSelectedImage = true
            }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}