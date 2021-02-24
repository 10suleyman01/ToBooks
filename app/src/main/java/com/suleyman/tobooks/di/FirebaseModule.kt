package com.suleyman.tobooks.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.suleyman.tobooks.utils.StorageWalker
import com.suleyman.tobooks.utils.Utils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth() = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStore() = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage() = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideStorageReference(
        firebaseStorage: FirebaseStorage
    ) = firebaseStorage.reference

    @Provides
    @Singleton
    fun provideFirebaseDatabase() = FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideDatabaseReference(
        firebaseDatabase: FirebaseDatabase
    ) = firebaseDatabase.reference

    @Provides
    @Singleton
    fun provideStorageWalker(
        storageReference: StorageReference,
        utils: Utils
    ) = StorageWalker(storageReference, utils)
}