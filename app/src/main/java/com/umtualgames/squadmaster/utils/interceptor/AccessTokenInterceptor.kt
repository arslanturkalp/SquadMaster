package com.umtualgames.squadmaster.utils.interceptor

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.SessionManager.getToken
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject

class AccessTokenInterceptor @Inject constructor(context: Context) : Interceptor {

    private val applicationContext = context.applicationContext

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request().newBuilder().addHeaders(getToken()).build()

        return when {
            isInternetAvailable() -> chain.proceed(request)
            else -> {
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(599)
                    .message(applicationContext.getString(R.string.internet_connection_problem))
                    .body(applicationContext.getString(R.string.internet_connection_problem).toResponseBody(null))
                    .build()
            }
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    private fun Request.Builder.addHeaders(token: String) = this.apply { header("Authorization", "Bearer $token") }
}