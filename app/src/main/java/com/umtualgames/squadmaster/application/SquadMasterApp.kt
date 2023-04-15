package com.umtualgames.squadmaster.application

import android.app.Application
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.onesignal.OneSignal
import com.orhanobut.hawk.Hawk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SquadMasterApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        Hawk.init(instance).build()
        MobileAds.initialize(this)

        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)
    }

    fun getContext(): Context {
        return applicationContext
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    companion object {
        val TAG: String = Application::class.java.simpleName
        const val ONESIGNAL_APP_ID: String = "10fa2c41-18c2-4b7f-bc47-97f520036635"

        @get:Synchronized
        var instance: SquadMasterApp? = null
            private set

    }
}