package com.umtualgames.squadmaster.di

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.umtualgames.squadmaster.application.Constants.BASE_URL
import com.umtualgames.squadmaster.application.SessionManager.getToken
import com.umtualgames.squadmaster.data.repository.RepositoryImpl
import com.umtualgames.squadmaster.domain.repository.RepositoryNew
import com.umtualgames.squadmaster.data.api.ApiService
import com.umtualgames.squadmaster.data.api.ForTokenApiService
import com.umtualgames.squadmaster.utils.interceptor.DefaultInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
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
    fun provideApiService(@Named("Normal") retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    fun provideApiServiceForToken(@Named("ForToken") retrofit: Retrofit): ForTokenApiService = retrofit.create(ForTokenApiService::class.java)

    @Provides
    @Singleton
    fun providesRepository(apiService: ApiService, forTokenApiService: ForTokenApiService) = Repository(apiService, forTokenApiService)

    @Provides
    @Singleton
    fun providesRepositoryNew(repository: RepositoryImpl): RepositoryNew = repository

    @Provides
    @Singleton
    fun providesOkHttpClient(@ApplicationContext applicationContext: Context): OkHttpClient {

        val cacheSize = (5 * 1024 * 1024).toLong()
        val mCache = Cache(applicationContext.cacheDir, cacheSize)
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()
            .cache(mCache)
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addNetworkInterceptor(interceptor)
            .addInterceptor { chain ->
                var request = chain.request()
                request = if (provideIsNetworkAvailable(applicationContext)) request.newBuilder().addHeaders(getToken())
                    .header("Cache-Control", "public, max-age=" + 5).build()
                else request.newBuilder().header(
                    "Cache-Control",
                    "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7
                ).build()
                chain.proceed(request)
            }
        return client.build()
    }

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    @Provides
    @Singleton
    fun provideIsNetworkAvailable(@ApplicationContext context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
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

    private fun Request.Builder.addHeaders(token: String) = this.apply { header("Authorization", "Bearer $token") }

}