package com.suleyman.tobooks.ui.fragment.audios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.suleyman.tobooks.databinding.FragmentAudioBooksBinding

class AudioBooksFragment: Fragment() {

    private var _binding: FragmentAudioBooksBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAudioBooksBinding.inflate(inflater, container, false)

        return binding.root
    }

}