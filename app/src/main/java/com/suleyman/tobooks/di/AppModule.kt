package com.suleyman.tobooks.di

import android.content.Context
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

}