package com.suleyman.tobooks.base

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import com.suleyman.tobooks.R

@SuppressLint("NonConstantResourceId")
abstract class BaseActivity: AppCompatActivity() {

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view())

        ButterKnife.bind(this)
        setSupportActionBar(toolbar)

        onActivityCreate(savedInstanceState)
    }

    abstract fun onActivityCreate(savedInstanceState: Bundle?)
    abstract fun view(): Int
}








