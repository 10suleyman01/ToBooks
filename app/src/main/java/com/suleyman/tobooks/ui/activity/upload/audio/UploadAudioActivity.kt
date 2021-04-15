package com.suleyman.tobooks.ui.activity.upload.audio

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.lifecycle.lifecycleScope
import coil.load
import com.suleyman.tobooks.R
import com.suleyman.tobooks.databinding.ActivityUploadAudioBinding
import com.suleyman.tobooks.ui.activity.upload.UploadBookViewModel
import com.suleyman.tobooks.ui.fragment.books.BooksFragment

class UploadAudioActivity : AppCompatActivity(), View.OnClickListener {

    private var _binding: ActivityUploadAudioBinding? = null
    private val binding get() = _binding!!

    private val getImageFromStorage =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            binding.imgAudio.load(it)
        }

    private val getAudioFromStorage =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            val file = it.toFile()
            binding.tvSelectedFile.text = file.name
        }

    private val uploadViewModel: UploadBookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val category = intent.extras?.getString(BooksFragment.EXTRA_CATEGORY)!!

        _binding = ActivityUploadAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            fabSelectImageBook.setOnClickListener(this@UploadAudioActivity)

            btnSelectFile.setOnClickListener(this@UploadAudioActivity)
        }

        lifecycleScope.launchWhenStarted {
            // TODO 1 load audio to server

//            uploadViewModel.uploadFile(
//                AUDIOS_BUCKET,
//                category,
//
//            )

        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fabSelectImageBook -> {
                getImageFromStorage.launch("image/*")
            }

            R.id.btnSelectFile -> {
                getAudioFromStorage.launch("audio/*")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }

}