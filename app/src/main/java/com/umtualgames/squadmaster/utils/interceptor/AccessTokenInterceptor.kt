package com.umtualgames.squadmaster.utils.interceptor

import android.annotation.SuppressLint
import com.umtualgames.squadmaster.application.SessionManager.getToken
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AccessTokenInterceptor : Interceptor {

    @SuppressLint("CheckResult")
    override fun intercept(chain: Interceptor.Chain): Response {

        return chain.proceed(
            chain.request()
                .newBuilder()
                .addHeaders(getToken())
                .build()
        )
    }

    private fun Request.Builder.addHeaders(token: String) = this.apply { header("Authorization", "Bearer $token") }
}