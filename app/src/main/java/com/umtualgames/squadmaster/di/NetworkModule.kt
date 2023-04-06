package com.umtualgames.squadmaster.di

import com.umtualgames.squadmaster.application.Constants.BASE_URL
import com.umtualgames.squadmaster.network.services.ApiService
import com.umtualgames.squadmaster.network.services.ForTokenApiService
import com.umtualgames.squadmaster.utils.interceptor.AccessTokenInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    @Named("Normal")
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .client(providesOkHttpClient())
            .build()
    }

    @Singleton
    @Provides
    @Named("ForToken")
    fun provideRetrofitIsLogin(): Retrofit {
        return Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .client(providesOkHttpClientForToken())
            .build()
    }

    @Provides
    fun provideApiService(@Named("Normal") retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    fun provideApiServiceForToken(@Named("ForToken") retrofit: Retrofit): ForTokenApiService = retrofit.create(ForTokenApiService::class.java)

    @Singleton
    @Provides
    fun providesRepository(apiService: ApiService, forTokenApiService: ForTokenApiService) = Repository(apiService, forTokenApiService)

    @Singleton
    @Provides
    fun providesOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AccessTokenInterceptor())
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .retryOnConnectionFailure(false)
            .build()
    }

    @Singleton
    @Provides
    fun providesOkHttpClientForToken(): OkHttpClient {
        return OkHttpClient.Builder()
            .retryOnConnectionFailure(false)
            .build()
    }
}