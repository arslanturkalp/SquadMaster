package com.umtualgames.squadmaster.utils.interceptor

import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.SquadMasterApp
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.internal.http2.ConnectionShutdownException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class DefaultInterceptor : Interceptor {

    @Throws(Exception::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        try {
            val response = chain.proceed(request)

            val bodyString = response.body.string()

            return response.newBuilder()
                .body(bodyString.toResponseBody(response.body.contentType()))
                .build()
        } catch (e: Exception) {
            e.printStackTrace()
            val msg: String
            when (e) {
                is SocketTimeoutException -> {
                    msg = SquadMasterApp.instance?.getContext()?.getString(R.string.socket_timeout_exception).toString()
                }
                is UnknownHostException -> {
                    msg = SquadMasterApp.instance?.getContext()?.getString(R.string.unknown_host_exception).toString()
                }
                is ConnectionShutdownException -> {
                    msg = SquadMasterApp.instance?.getContext()?.getString(R.string.connection_shutdown_exception).toString()
                }
                is IOException -> {
                    msg = SquadMasterApp.instance?.getContext()?.getString(R.string.io_exception).toString()
                }
                is IllegalStateException -> {
                    msg = "${e.message}"
                }
                else -> {
                    msg = "${e.message}"
                }
            }

            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(999)
                .message(msg)
                .body("{${e}}".toResponseBody(null)).build()
        }
    }
}