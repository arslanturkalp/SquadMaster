package com.example.squadmaster.network

import com.example.squadmaster.application.Constants.BASE_URL
import com.example.squadmaster.utils.interceptor.AccessTokenInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ServiceProvider {

    fun getRetrofit(timeOut: Long = 30): Retrofit {
        return Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .client(okHttpClient(timeOut))
            .build()
    }

    fun getRetrofitIsLogin(timeOut: Long = 30): Retrofit {
        return Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .client(okHttpClientForToken(timeOut))
            .build()
    }

    private fun okHttpClient(timeOut: Long): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AccessTokenInterceptor())
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .connectTimeout(timeOut, TimeUnit.SECONDS)
            .readTimeout(timeOut, TimeUnit.SECONDS)
            .writeTimeout(timeOut, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()
    }

    private fun okHttpClientForToken(timeOut: Long): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(timeOut, TimeUnit.SECONDS)
            .readTimeout(timeOut, TimeUnit.SECONDS)
            .writeTimeout(timeOut, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()
    }
}