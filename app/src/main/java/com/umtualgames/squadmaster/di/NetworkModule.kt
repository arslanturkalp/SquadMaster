package com.umtualgames.squadmaster.di

import android.content.Context
import com.umtualgames.squadmaster.application.Constants.BASE_URL
import com.umtualgames.squadmaster.application.Constants.WEBSOCKET_URL
import com.umtualgames.squadmaster.network.services.ApiService
import com.umtualgames.squadmaster.network.services.ForTokenApiService
import com.umtualgames.squadmaster.network.services.WebsocketService
import com.umtualgames.squadmaster.utils.interceptor.AccessTokenInterceptor
import com.umtualgames.squadmaster.utils.interceptor.DefaultInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    @Named("Normal")
    fun provideRetrofit(@ApplicationContext applicationContext: Context): Retrofit {
        return Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .client(providesOkHttpClient(applicationContext))
            .build()
    }

    @Provides
    @Singleton
    @Named("ForToken")
    fun provideRetrofitIsLogin(@ApplicationContext applicationContext: Context): Retrofit {
        return Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .client(providesOkHttpClientForToken(applicationContext))
            .build()
    }

    @Provides
    @Singleton
    @Named("WebSocket")
    fun provideRetrofitWebsocket(@ApplicationContext applicationContext: Context): Retrofit {
        return Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(WEBSOCKET_URL)
            .client(providesOkHttpClientForToken(applicationContext))
            .build()
    }

    @Provides
    fun provideApiService(@Named("Normal") retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    fun provideApiServiceForToken(@Named("ForToken") retrofit: Retrofit): ForTokenApiService = retrofit.create(ForTokenApiService::class.java)

    @Provides
    fun provideWebsocketApiService(@Named("WebSocket") retrofit: Retrofit): WebsocketService = retrofit.create(WebsocketService::class.java)

    @Provides
    @Singleton
    fun providesRepository(apiService: ApiService, forTokenApiService: ForTokenApiService, websocketService: WebsocketService) = Repository(apiService, forTokenApiService, websocketService)

    @Provides
    @Singleton
    fun providesOkHttpClient(@ApplicationContext applicationContext: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AccessTokenInterceptor(applicationContext))
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()
    }

    @Provides
    @Singleton
    fun providesOkHttpClientForToken(@ApplicationContext applicationContext: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(DefaultInterceptor(applicationContext))
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()
    }
}