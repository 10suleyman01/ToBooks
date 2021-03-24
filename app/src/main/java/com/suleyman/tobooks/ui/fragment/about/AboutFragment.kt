package com.suleyman.tobooks.ui.fragment.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.suleyman.tobooks.databinding.FragmentAboutBinding
import com.suleyman.tobooks.ui.activity.books.BooksActivity
import com.suleyman.tobooks.utils.FragmentTag
import com.suleyman.tobooks.utils.IBackPressed

class AboutFragment: Fragment(), IBackPressed {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as BooksActivity).setListener(object : IBackPressed {
            override fun onBackPressed(): Boolean {
                return this@AboutFragment.onBackPressed()
            }
        })
    }

    override fun onBackPressed(): Boolean {

        if (binding.tvNumber.isSelected) {
            binding.tvNumber.isSelected = false
            return true
        }

        if (fragmentManager?.backStackEntryCount != 0) {
            return true
        }

        return false
    }
}