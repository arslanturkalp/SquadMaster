package com.umtualgames.squadmaster.utils

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import com.umtualgames.squadmaster.R

class BackgroundSoundService : Service() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.sound)
        mediaPlayer.apply {
            start()
            setVolume(100f, 100f)
            isLooping = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.apply {
            pause()
            release()
        }
    }
}