package com.moon.casaprestamo.data

import com.moon.casaprestamo.data.network.ApiService
import com.moon.casaprestamo.data.network.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideApiService(): ApiService {
        return RetrofitClient.apiService
    }
}