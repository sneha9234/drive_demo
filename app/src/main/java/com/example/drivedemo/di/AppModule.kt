package com.example.drivedemo.di

import com.example.drivedemo.data.repository.HomeRepositoryImpl
import com.example.drivedemo.domain.repository.HomeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesHomeRepository(): HomeRepository = HomeRepositoryImpl()

}