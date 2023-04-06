package com.umtualgames.squadmaster.ui.online

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.umtualgames.squadmaster.application.SessionManager.getUserName
import com.umtualgames.squadmaster.databinding.ActivityOnlineBinding
import com.umtualgames.squadmaster.ui.base.BaseActivity
import okhttp3.OkHttpClient
import okhttp3.Request

class OnlineActivity: BaseActivity() {

    private val binding by lazy { ActivityOnlineBinding.inflate(layoutInflater) }

    private val okHttpClient = OkHttpClient()
    private val webSocketListener = OnlineListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val webSocket = okHttpClient.newWebSocket(createRequest(), webSocketListener)
        webSocket.send(getUserName())
    }

    private fun createRequest(): Request {
        val webSocketUrl = "ws://0.0.0.0:8585/chat"

        return Request.Builder()
            .url(webSocketUrl)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        okHttpClient.dispatcher.executorService.shutdown()
    }

    companion object {
        fun createIntent(context: Context?): Intent {
            return Intent(context, OnlineActivity::class.java)
        }
    }
}