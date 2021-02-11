package com.suleyman.tobooks.di

import com.google.firebase.auth.FirebaseAuth
import com.suleyman.tobooks.utils.Utils
import org.koin.core.component.KoinApiExtension
import org.koin.dsl.module

@OptIn(KoinApiExtension::class)
val appModule = module {
    single { FirebaseAuth.getInstance() }

    single { Utils() }
}