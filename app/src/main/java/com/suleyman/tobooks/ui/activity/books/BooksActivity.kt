package com.suleyman.tobooks.ui.activity.books

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.suleyman.tobooks.R
import com.suleyman.tobooks.utils.Common
import com.suleyman.tobooks.utils.IBackPressed
import com.suleyman.tobooks.databinding.ActivityContainerBinding
import com.suleyman.tobooks.databinding.HeaderLayoutBinding
import com.suleyman.tobooks.ui.fragment.audios.AudioBooksFragment
import com.suleyman.tobooks.ui.fragment.books.BooksFragment
import com.suleyman.tobooks.utils.FragmentTag
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class BooksActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var _binding: ActivityContainerBinding? = null
    private val binding get() = _binding!!

    private lateinit var headerLayout: HeaderLayoutBinding

    private lateinit var fragmentManager: FragmentManager
    private var backPressedListener: IBackPressed? = null

    private lateinit var toggle: ActionBarDrawerToggle

    private val auth: FirebaseAuth by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        headerLayout = HeaderLayoutBinding.bind(binding.navigationView.getHeaderView(0))
        with(headerLayout) {
            with(auth.currentUser) {
                this?.let {
                    userTitle.text = email ?: phoneNumber ?: displayName
                }
            }
        }

        setSupportActionBar(binding.includeContainerToolbar.toolbar)

        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.navigationView.setNavigationItemSelectedListener(this)

        fragmentManager = supportFragmentManager
        addFragment(BooksFragment(), FragmentTag.BOOKS.value())
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (binding.drawerLayout.isDrawerOpen(Gravity.START)) {
            binding.drawerLayout.closeDrawer(Gravity.START)
        }
        when(item.itemId) {
            R.id.books -> {
                addFragment(BooksFragment(), FragmentTag.BOOKS.value())
            }

            R.id.audios -> {
                addFragment(AudioBooksFragment(), FragmentTag.AUDIOS.value())
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) return true
        when (item.itemId) {
            R.id.sign_out -> {
                auth.signOut()
            }
            R.id.sendHelpMessage -> {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.type = "text/plain"
                intent.data = Uri.parse("mailto:${Common.APP_EMAIL}")
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject_message))
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.text_message))
                startActivity(Intent.createChooser(intent, getString(R.string.send_with)))
            }
        }
        return true
    }

    private fun addFragment(fragment: Fragment, tag: String) {
        fragmentManager.beginTransaction()
            .replace(R.id.container, fragment, tag)
            .commit()
    }

    fun setListener(backPressedListener: IBackPressed) {
        this.backPressedListener = backPressedListener
    }

    override fun onBackPressed() {
        if (backPressedListener != null) {
            if (!backPressedListener!!.onBackPressed()) {
                super.onBackPressed()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}