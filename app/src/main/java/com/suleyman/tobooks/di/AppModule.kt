package com.suleyman.tobooks.di

import android.content.Context
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.suleyman.tobooks.ui.fragment.books.BooksAdapter
import com.suleyman.tobooks.utils.NetworkHelper
import com.suleyman.tobooks.utils.Utils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUtils(@ApplicationContext context: Context) = Utils(context)

    @Provides
    @Singleton
    fun provideNetworkHelper(@ApplicationContext context: Context) = NetworkHelper(context)


    @Provides
    @Singleton
    fun provideAdapter(
        @ApplicationContext context: Context,
        utils: Utils,
        firestore: FirebaseFirestore,
        databaseReference: DatabaseReference
    ) = BooksAdapter(context, utils, firestore, databaseReference)

}