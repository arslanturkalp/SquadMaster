package com.umtualgames.squadmaster.application

import android.app.Application
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.onesignal.OneSignal
import com.orhanobut.hawk.Hawk
import com.unity3d.ads.UnityAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SquadMasterApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        Hawk.init(instance).build()

        MobileAds.initialize(this)
        UnityAds.initialize(this, UNITY_GAME_ID)
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    companion object {
        val TAG: String = Application::class.java.simpleName
        const val ONESIGNAL_APP_ID: String = "10fa2c41-18c2-4b7f-bc47-97f520036635"
        const val UNITY_GAME_ID: String = "5724713"

        @get:Synchronized
        var instance: SquadMasterApp? = null
            private set

    }

}